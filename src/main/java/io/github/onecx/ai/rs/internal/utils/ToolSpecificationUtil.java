package io.github.onecx.ai.rs.internal.utils;

import java.util.List;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import gen.io.github.onecx.ai.rs.internal.model.AIContextDTO;

public class ToolSpecificationUtil {

    public static List<ToolSpecification> retrieveToolSpecifications(AIContextDTO context) {
        // TODO: implement later
        List<McpClient> mcpClients = retrieveMcpClients(context);
        McpClient mcpClient = mcpClients.get(0);
        return mcpClient.listTools();
    }

    public static List<McpClient> retrieveMcpClients(AIContextDTO context) {
        McpTransport transport = StreamableHttpMcpTransport.builder()
                .url("https://onecx-docs-ai-dev.dev.one-cx.org/mcp")
                .logRequests(true) // if you want to see the traffic in the log
                .logResponses(true)
                .build();
        McpClient mcpClient = DefaultMcpClient.builder()
                .key("MyMCPClient")
                .transport(transport)
                .build();
        return List.of(mcpClient);
    }
}
