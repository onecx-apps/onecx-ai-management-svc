package io.github.onecx.ai.common.services.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;

/**
 * Represents a tool provided by an MCP server.
 */
public record McpTool(
        String serverUrl,
        ToolSpecification toolSpecification,
        McpClient mcpClient) {

    public String toolName() {
        return toolSpecification.name();
    }

    /**
     * Executes this tool with the given arguments.
     */
    public String execute(ToolExecutionRequest request) {
        ToolExecutionResult result = mcpClient.executeTool(request);
        return result.resultText();
    }
}
