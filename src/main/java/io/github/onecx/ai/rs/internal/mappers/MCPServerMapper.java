package io.github.onecx.ai.rs.internal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.ai.rs.internal.model.*;
import io.github.onecx.ai.domain.criteria.MCPServerSearchCriteria;
import io.github.onecx.ai.domain.models.MCPServer;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface MCPServerMapper {

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    MCPServer create(CreateMCPServerRequestDTO createMCPServerRequestDTO);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    MCPServer map(MCPServerDTO mcpServerDTO);

    MCPServerDTO map(MCPServer mcpServer);

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    void update(@MappingTarget MCPServer mcpServer, UpdateMCPServerRequestDTO updateMCPServerRequestDTO);

    MCPServerSearchCriteria mapCriteria(MCPServerSearchCriteriaDTO criteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    MCPServerPageResultDTO mapPageResult(PageResult<MCPServer> result);
}
