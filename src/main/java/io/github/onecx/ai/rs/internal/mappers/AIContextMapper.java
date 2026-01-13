package io.github.onecx.ai.rs.internal.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.ai.rs.internal.model.*;
import io.github.onecx.ai.domain.criteria.AIContextSearchCriteria;
import io.github.onecx.ai.domain.models.AIContext;
import io.github.onecx.ai.domain.models.AIKnowledgeDatabase;
import io.github.onecx.ai.domain.models.AIKnowledgeUrl;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface AIContextMapper {

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aiKnowledgeDocuments", ignore = true)
    @Mapping(target = "aiKnowledgeUrls", ignore = true)
    @Mapping(target = "aiKnowledgeDbs", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "knowledgebase", ignore = true)
    @Mapping(target = "aiKnowledgeVectorDb", ignore = true)
    @Mapping(source = "llmProvider", target = "provider")
    @Mapping(target = "provider.controlTraceabilityManual", ignore = true)
    @Mapping(target = "provider.modificationCount", ignore = true)
    @Mapping(target = "provider.persisted", ignore = true)
    @Mapping(target = "provider.id", ignore = true)
    @Mapping(target = "provider.tenantId", ignore = true)
    AIContext createAIContext(CreateAIContextRequestDTO dto);

    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "knowledgebase", ignore = true)
    @Mapping(target = "aiKnowledgeVectorDb", ignore = true)
    @Mapping(target = "aiKnowledgeUrls", ignore = true)
    @Mapping(target = "aiKnowledgeDbs", ignore = true)
    @Mapping(target = "aiKnowledgeDocuments", ignore = true)
    @Mapping(source = "llmProvider", target = "provider")
    @Mapping(target = "provider.controlTraceabilityManual", ignore = true)
    @Mapping(target = "provider.modificationCount", ignore = true)
    @Mapping(target = "provider.persisted", ignore = true)
    @Mapping(target = "provider.tenantId", ignore = true)
    AIContext updateAIContext(UpdateAIContextRequestDTO dto,
            @MappingTarget AIContext entity);

    @Mapping(target = "vectorDb", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "removeDocumentsItem", ignore = true)
    @Mapping(target = "urls", ignore = true)
    @Mapping(target = "removeUrlsItem", ignore = true)
    @Mapping(target = "dbs", ignore = true)
    @Mapping(target = "removeDbsItem", ignore = true)
    @Mapping(source = "provider", target = "llmProvider")
    @Mapping(target = "llmProvider.modificationCount", ignore = true)
    AIContextDTO map(AIContext aiContext);

    @Mapping(target = "description", ignore = true)
    AIContextSearchCriteria map(AIContextSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    AIContextPageResultDTO mapPage(PageResult<AIContext> page);

    List<AIContextDTO> mapContextList(List<AIContext> items);

    @Named("AIKnowlegeUrlHashSetToList")
    static List<AIKnowledgeUrlDTO> mapKnowledgeUrlDtoList(Set<AIKnowledgeUrl> value) {
        List<AIKnowledgeUrlDTO> aiKnowledgeUrlDTOs = new ArrayList<>();
        if (value != null) {
            for (AIKnowledgeUrl aiKnowledgeUrl : value) {
                AIKnowledgeUrlDTO dto = new AIKnowledgeUrlDTO();

                dto.setId(aiKnowledgeUrl.getId());
                dto.setUrl(aiKnowledgeUrl.getUrl());
                dto.setName(aiKnowledgeUrl.getName());
                dto.setDescription(aiKnowledgeUrl.getDescription());

                aiKnowledgeUrlDTOs.add(dto);
            }
        }
        return aiKnowledgeUrlDTOs;
    }

    @Named("AIKnowledgeDatabaseHashSetToList")
    static List<AIKnowledgeDatabaseDTO> mapKnowledgeDatabaseDtoList(Set<AIKnowledgeDatabase> knowledgeDatabases) {
        List<AIKnowledgeDatabaseDTO> aiKnowledgeDatabaseDTOs = new ArrayList<>();
        if (knowledgeDatabases != null) {
            for (AIKnowledgeDatabase aiKnowledgeDatabase : knowledgeDatabases) {
                AIKnowledgeDatabaseDTO dto = new AIKnowledgeDatabaseDTO();

                dto.setId(aiKnowledgeDatabase.getId());
                dto.setName(aiKnowledgeDatabase.getName());
                dto.setDescription(aiKnowledgeDatabase.getDescription());
                dto.setDb(aiKnowledgeDatabase.getDb());
                dto.setUser(aiKnowledgeDatabase.getUsr());

                aiKnowledgeDatabaseDTOs.add(dto);
            }
        }
        return aiKnowledgeDatabaseDTOs;
    }
}
