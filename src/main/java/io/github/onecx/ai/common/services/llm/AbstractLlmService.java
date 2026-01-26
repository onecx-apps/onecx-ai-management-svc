package io.github.onecx.ai.common.services.llm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import gen.io.github.onecx.ai.rs.external.v1.model.*;
import io.github.onecx.ai.common.models.DispatchConfig;
import io.github.onecx.ai.common.services.mcp.McpService;
import io.github.onecx.ai.common.services.mcp.McpToolRegistry;
import io.github.onecx.ai.domain.models.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLlmService {

    @Inject
    McpService mcpService;

    @Inject
    DispatchConfig dispatchConfig;

    public abstract Response chat(Configuration configuration, ChatRequestDTOV1 chatRequestDTO);

    /**
     * Creates a tool registry from the MCP servers defined in the context.
     */
    protected McpToolRegistry createToolRegistry(Configuration aiConfiguration) {
        return mcpService.createToolRegistry(aiConfiguration);
    }

    /**
     * Checks if the LLM response contains tool execution requests.
     */
    protected boolean hasToolExecutionRequests(ChatResponse response) {
        AiMessage aiMessage = response.aiMessage();
        return aiMessage != null
                && aiMessage.hasToolExecutionRequests()
                && aiMessage.toolExecutionRequests() != null
                && !aiMessage.toolExecutionRequests().isEmpty();
    }

    /**
     * Executes all tool requests from the LLM response and returns the results as messages.
     *
     * @param response The LLM response containing tool execution requests
     * @param toolRegistry The registry containing available tools
     * @return List of messages including the AI message and tool execution results
     */
    protected List<ChatMessage> executeToolRequests(ChatResponse response, McpToolRegistry toolRegistry) {
        List<ChatMessage> resultMessages = new ArrayList<>();

        AiMessage aiMessage = response.aiMessage();
        resultMessages.add(aiMessage);

        for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
            String toolName = toolRequest.name();

            log.info("LLM requested tool execution: {} with arguments: {}", toolName, toolRequest.arguments());

            var toolOpt = toolRegistry.findByName(toolName);
            if (toolOpt.isEmpty()) {
                log.error("Tool '{}' not found in registry", toolName);
                resultMessages.add(ToolExecutionResultMessage.from(
                        toolRequest,
                        "Error: Tool '" + toolName + "' not found"));
                continue;
            }

            String result = toolOpt.get().execute(toolRequest);
            log.info("Tool '{}' executed successfully", toolName);

            resultMessages.add(ToolExecutionResultMessage.from(toolRequest, result));
        }

        return resultMessages;
    }

    protected List<ChatMessage> mapToLangChainMessages(List<ChatMessageDTOV1> history) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        for (ChatMessageDTOV1 msg : history) {
            switch (msg.getType()) {
                case USER -> chatMessageList.add(new UserMessage(msg.getMessage()));
                case ASSISTANT -> chatMessageList.add(new AiMessage(msg.getMessage()));
                case SYSTEM -> chatMessageList.add(new SystemMessage(msg.getMessage()));
            }
        }
        return chatMessageList;
    }

    protected ChatMessageDTOV1 mapToChatMessageResponseDTO(String responseMessage) {
        ChatMessageDTOV1 chatMessageDTOV1 = new ChatMessageDTOV1();
        chatMessageDTOV1.setMessage(responseMessage);
        chatMessageDTOV1.setType(ChatMessageDTOV1.TypeEnum.ASSISTANT);
        chatMessageDTOV1.setCreationDate(new Date().getTime());
        return chatMessageDTOV1;
    }
}
