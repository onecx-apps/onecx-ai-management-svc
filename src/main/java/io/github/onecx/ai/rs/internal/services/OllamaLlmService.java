package io.github.onecx.ai.rs.internal.services;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingLanguageModel;
import gen.io.github.onecx.ai.rs.internal.model.*;
import io.github.onecx.ai.domain.criteria.AIContextSearchCriteria;
import io.github.onecx.ai.domain.daos.AIContextDAO;
import io.github.onecx.ai.domain.models.AIContext;
import io.github.onecx.ai.rs.internal.mappers.AIProviderMapper;
import io.github.onecx.ai.rs.internal.utils.ToolSpecificationUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class OllamaLlmService extends AbstractLlmService {

    @Inject
    AIContextDAO daoContext;

    @Inject
    AIProviderMapper mapperProvider;

    final static String DEFAULT_AI_CONTEXT_NAME = "default";

    @Override
    public Response generate(GenerateRequestDTO generateRequestDTO) {
        log.info("OllamaLlmService");

        Map<String, String> customHeaders = new HashMap<>();

        AIProviderDTO providerDTO = setUpProvider(generateRequestDTO.getAiContext());

        if (providerDTO.getApiKey() != null) {
            customHeaders.put("Authorization", providerDTO.getApiKey());
        }

        StreamingLanguageModel ollamaGenerate = OllamaStreamingLanguageModel.builder()
                .baseUrl(providerDTO.getLlmUrl())
                .modelName(providerDTO.getModelName())
                .customHeaders(customHeaders)
                .build();

        final String[] message = { "" };

        CompletableFuture<String> future = new CompletableFuture<>();

        ollamaGenerate.generate(generateRequestDTO.getAiRequest().getMessage(), new StreamingResponseHandler<String>() {
            @Override
            public void onNext(String token) {
                message[0] += token;
                System.out.println(token);
            }

            @Override
            public void onComplete(dev.langchain4j.model.output.Response<String> response) {

                System.out.println(message[0]);
                future.complete(response.toString());
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });
        future.join();

        AIResponseDTO responseDTO = new AIResponseDTO();
        responseDTO.setMessage(message[0]);
        return Response.ok(responseDTO).build();

    }

    @Override
    public Response chat(ChatRequestDTO chatRequestDTO) {
        final String[] message = { "" };
        Map<String, String> customHeaders = new HashMap<>();
        AIProviderDTO providerDTO = null;

        // checking ai-context -> if null then get the default
        if (chatRequestDTO.getAiContext() == null) {
            AIContextSearchCriteria searchCriteria = new AIContextSearchCriteria();
            searchCriteria.setPageSize(10);
            searchCriteria.setPageNumber(0);
            searchCriteria.setName(DEFAULT_AI_CONTEXT_NAME);

            Optional<AIContext> defaultAIContext = daoContext.findAIContextsByCriteria(searchCriteria).getStream()
                    .findFirst();

            if (defaultAIContext.isPresent()) {
                providerDTO = mapperProvider.map(defaultAIContext.get().getProvider());
            }
        } else {
            providerDTO = setUpProvider(chatRequestDTO.getAiContext());
        }

        // setting authorization
        if (providerDTO != null && providerDTO.getApiKey() != null) {
            customHeaders.put("Authorization", providerDTO.getApiKey());
        }

        List<ChatMessage> chatMessageList = fillChatHistory(chatRequestDTO.getConversation().getHistory());

        chatMessageList.add(new UserMessage(new StringBuilder()
                .append(chatRequestDTO.getChatMessage().getMessage())
                .toString()));

        ChatModel ollama = OllamaChatModel.builder()
                .baseUrl(providerDTO.getLlmUrl())
                .modelName(providerDTO.getModelName())
                .customHeaders(customHeaders)
                .timeout(Duration.ofSeconds(30L))
                .build();

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(chatMessageList)
                .toolSpecifications(ToolSpecificationUtil.retrieveToolSpecifications(chatRequestDTO.getAiContext()))
                .build();

        AiMessage response = ollama.chat(chatRequest).aiMessage();

        // MCP Clients
        List<McpClient> mcpClients = ToolSpecificationUtil.retrieveMcpClients(chatRequestDTO.getAiContext());
        if (response.hasToolExecutionRequests()) {
            var requests = response.toolExecutionRequests();
            Map<ToolExecutionRequest, Object> responses = new HashMap<>();
            log.info("Tool execution requests received: {}", requests.size());
            for (var req : requests) {
                for (var client : mcpClients) {
                    List<ToolSpecification> tools = client.listTools();
                    Optional<ToolSpecification> toolSpecOpt = tools.stream()
                            .filter(t -> t.name().equals(req.name()))
                            .findFirst();
                    if (toolSpecOpt.isPresent()) {
                        ToolSpecification toolSpec = toolSpecOpt.get();
                        var toolResponse = client.executeTool(req);
                        responses.put(req, toolResponse);
                        log.info("Tool {} executed with response: {}", req.name(), toolResponse);
                    } else {
                        log.warn("Tool specification for {} not found in MCP client {}", req.name(), client);
                    }
                }
            }
            log.info("All tool execution responses: {}", responses);
            List<ToolExecutionResultMessage> toolExecutionResultMessages = new ArrayList<>();
            for (var entry : responses.entrySet()) {
                ToolExecutionRequest execReq = entry.getKey();
                Object resp = entry.getValue();
                String resultContent = resp != null ? resp.toString() : "No response";
                ToolExecutionResultMessage resultMessage = ToolExecutionResultMessage.from(
                        execReq,
                        resultContent);
                toolExecutionResultMessages.add(resultMessage);
            }
            // All messages
            List<ChatMessage> allMessages = new ArrayList<>(chatMessageList);
            allMessages.addAll(toolExecutionResultMessages);
            // Send responses back to the model for final answer generation
            ChatRequest followUpRequest = ChatRequest.builder()
                    .messages(allMessages)
                    .toolSpecifications(ToolSpecificationUtil.retrieveToolSpecifications(chatRequestDTO.getAiContext()))
                    .build();
            response = ollama.chat(followUpRequest).aiMessage();
        }

        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setMessage(response.text());
        chatMessageDTO.setType(ChatMessageDTO.TypeEnum.ASSISTANT);
        chatMessageDTO.setCreationDate(new Date().getTime());

        return Response.ok(chatMessageDTO).build();
    }

    @Override
    public AIProviderDTO setUpProvider(AIContextDTO aiContextDTO) {
        if (aiContextDTO.getLlmProvider() != null) {
            return aiContextDTO.getLlmProvider();
        }
        return mapperProvider.map(daoContext.findById(aiContextDTO.getId()).getProvider());

    }

    public List<ChatMessage> fillChatHistory(List<ChatMessageDTO> history) {
        List<ChatMessage> chatMessageList = new ArrayList<>();

        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getType() == ChatMessageDTO.TypeEnum.USER) {
                chatMessageList.add(new UserMessage(history.get(i).getMessage()));
            } else if (history.get(i).getType() == ChatMessageDTO.TypeEnum.ASSISTANT) {
                chatMessageList.add(new AiMessage(history.get(i).getMessage()));
            } else if (history.get(i).getType() == ChatMessageDTO.TypeEnum.SYSTEM) {
                chatMessageList.add(new SystemMessage(history.get(i).getMessage()));
            }

        }

        return chatMessageList;
    }
}
