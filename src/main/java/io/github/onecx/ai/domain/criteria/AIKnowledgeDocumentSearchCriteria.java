package io.github.onecx.ai.domain.criteria;

import io.github.onecx.ai.domain.models.AIKnowledgeDocument.DocumentStatusType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class AIKnowledgeDocumentSearchCriteria {
    private String name;
    private String documentRefId;
    private DocumentStatusType status;
    private Integer pageNumber;
    private Integer pageSize;
}
