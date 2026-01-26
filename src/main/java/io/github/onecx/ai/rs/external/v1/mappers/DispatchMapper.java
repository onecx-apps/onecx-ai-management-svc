package io.github.onecx.ai.rs.external.v1.mappers;

import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface DispatchMapper {

    //    @Mapping(target = "removeMcpServersItem", ignore = true)
    //    @Mapping(target = "llmProvider", source = "provider")
    //    ContextDTOV1 map(Configuration item);
    //
    //    MCPServerDTOV1 map(MCPServer mcpServer);
    //
    //    ProviderDTOV1 map(Provider provider);
    //
    //  @Mapping(target = "tenantId", ignore = true)
    //  @Mapping(target = "persisted", ignore = true)
    //  @Mapping(target = "controlTraceabilityManual", ignore = true)
    //  Provider mapProvider(ProviderDTOV1 providerDTO);
    //
    //  @Mapping(target = "tenantId", ignore = true)
    //  @Mapping(target = "persisted", ignore = true)
    //  @Mapping(target = "description", ignore = true)
    //  @Mapping(target = "controlTraceabilityManual", ignore = true)
    //  MCPServer mapMCPServer(MCPServerDTOV1 mcpServerDTO);
}
