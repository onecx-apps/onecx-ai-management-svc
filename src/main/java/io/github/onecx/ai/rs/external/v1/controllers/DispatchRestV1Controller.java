package io.github.onecx.ai.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import gen.io.github.onecx.ai.rs.external.v1.DispatchV1Api;
import gen.io.github.onecx.ai.rs.external.v1.model.ChatRequestDTOV1;
import io.github.onecx.ai.common.services.llm.LlmServiceFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DispatchRestV1Controller implements DispatchV1Api {

    @Inject
    LlmServiceFactory llmServiceFactory;

    @Override
    public Response chat(ChatRequestDTOV1 chatRequestDTOV1) {
        return llmServiceFactory.chat(chatRequestDTOV1);
    }
}
