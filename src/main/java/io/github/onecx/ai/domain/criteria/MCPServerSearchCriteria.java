package io.github.onecx.ai.domain.criteria;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class MCPServerSearchCriteria {

    private String name;

    private String description;

    private String url;

    private Integer pageNumber;

    private Integer pageSize;

}
