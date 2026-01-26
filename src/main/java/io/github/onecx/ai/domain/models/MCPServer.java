package io.github.onecx.ai.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import io.github.onecx.ai.domain.models.enums.ExecutionPolicy;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MCP_SERVER")
public class MCPServer extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "URL")
    private String url;

    @Column(name = "API_KEY")
    private String apiKey;

    @Column(name = "EXECUTION_POLICY")
    @Enumerated(EnumType.STRING)
    private ExecutionPolicy executionPolicy;
}
