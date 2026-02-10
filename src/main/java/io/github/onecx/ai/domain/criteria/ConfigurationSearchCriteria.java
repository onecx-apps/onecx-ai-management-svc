package io.github.onecx.ai.domain.criteria;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class ConfigurationSearchCriteria {

    private String name;

    private String description;

    private Integer pageNumber;

    private Integer pageSize;

}
