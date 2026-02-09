package io.github.onecx.ai.domain.criteria;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ProviderSearchCriteria {

    private String name;

    private String description;

    private String llmUrl;

    private String modelName;

    private Integer pageNumber;

    private Integer pageSize;

}
