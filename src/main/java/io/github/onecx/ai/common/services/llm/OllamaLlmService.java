package io.github.onecx.ai.common.services.llm;

import java.time.Duration;
import java.util.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import gen.io.github.onecx.ai.rs.external.v1.model.ChatRequestDTOV1;
import io.github.onecx.ai.common.services.mcp.McpToolRegistry;
import io.github.onecx.ai.domain.models.Configuration;
import io.github.onecx.ai.domain.models.Provider;
import io.quarkiverse.langchain4j.jaxrsclient.JaxRsHttpClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class OllamaLlmService extends AbstractLlmService {

    @Override
    public Response chat(Configuration configuration, ChatRequestDTOV1 chatRequestDTO) {
        log.info("OllamaLlmService.chat()");

        // Resolve configuration by queryContext
        Provider provider = configuration.getProvider();
        // Build message list: history (if present) + current message
        List<ChatMessage> messages = new ArrayList<>();

        if (chatRequestDTO.getConversation() != null
                && chatRequestDTO.getConversation().getHistory() != null
                && !chatRequestDTO.getConversation().getHistory().isEmpty()) {
            messages.addAll(mapToLangChainMessages(chatRequestDTO.getConversation().getHistory()));
        }

        messages.add(new UserMessage(chatRequestDTO.getChatMessage().getMessage()));

        // Build custom headers for authentication
        Map<String, String> customHeaders = new HashMap<>();
        if (provider.getApiKey() != null) {
            customHeaders.put("Authorization", provider.getApiKey());
        }

        // Build the Ollama model
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(provider.getLlmUrl())
                .modelName(provider.getModelName())
                .customHeaders(customHeaders)
                .timeout(Duration.ofSeconds(dispatchConfig.providerConfig().timeout()))
                .logRequests(dispatchConfig.providerConfig().logRequests())
                .logResponses(dispatchConfig.providerConfig().logResponse())
                .httpClientBuilder(new JaxRsHttpClientBuilderFactory().create())
                .build();

        // Create tool registry from MCP servers (if configured)
        McpToolRegistry toolRegistry = createToolRegistry(configuration);
        try {
            List<ToolSpecification> toolSpecifications = toolRegistry.getToolSpecifications();

            // Build initial chat request
            ChatRequest.Builder chatRequestBuilder = ChatRequest.builder()
                    .messages(messages);

            if (!toolSpecifications.isEmpty()) {
                log.info("Using {} tool(s) for this request", toolSpecifications.size());
                chatRequestBuilder.toolSpecifications(toolSpecifications);
            }

            ChatRequest chatRequest = chatRequestBuilder.build();
            ChatResponse chatResponse = model.chat(chatRequest);

            // Handle tool execution loop
            int iterations = 0;
            while (hasToolExecutionRequests(chatResponse) && iterations < dispatchConfig.mcpConfig().maxIterations()) {
                iterations++;
                log.info("Tool execution iteration {}", iterations);

                // Execute tools and get result messages
                List<ChatMessage> toolResultMessages = executeToolRequests(chatResponse, toolRegistry);
                messages.addAll(toolResultMessages);

                // Send follow-up request with tool results
                ChatRequest followUpRequest = ChatRequest.builder()
                        .messages(messages)
                        .toolSpecifications(toolSpecifications)
                        .build();

                chatResponse = model.chat(followUpRequest);
            }

            if (iterations >= dispatchConfig.mcpConfig().maxIterations()) {
                log.warn("Reached maximum tool execution iterations ({})", dispatchConfig.mcpConfig().maxIterations());
            }

            // Get final response text
            String responseMessage = chatResponse.aiMessage().text();
            var responseDTO = mapToChatMessageResponseDTO(responseMessage);
            return Response.ok(responseDTO).build();
        } finally {
            toolRegistry.close();
        }
    }
}
