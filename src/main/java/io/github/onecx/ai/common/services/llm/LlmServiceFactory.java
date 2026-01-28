package io.github.onecx.ai.common.services.llm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import gen.io.github.onecx.ai.rs.external.v1.model.ChatRequestDTOV1;
import io.github.onecx.ai.common.services.configuration.ConfigurationService;
import io.github.onecx.ai.domain.models.enums.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory that selects the appropriate LLM service based on the provider configuration.
 */
@Slf4j
@ApplicationScoped
public class LlmServiceFactory {

    @Inject
    OllamaLlmService ollamaLlmService;

    @Inject
    ConfigurationService configurationService;

    /**
     * Routes the chat request to the appropriate LLM service based on provider type.
     */
    public Response chat(ChatRequestDTOV1 chatRequestDTO) {
        var configuration = configurationService.findConfigurationsByRequestContext(chatRequestDTO.getRequestContext());
        if (configuration == null) {
            log.error("No configuration found for request context: {}", chatRequestDTO.getRequestContext());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No configuration found for the given request context")
                    .build();
        }
        AbstractLlmService service = getServiceForProvider(configuration.getProvider().getType());
        log.info("Routing chat request to {} service", configuration.getProvider().getType());
        return service.chat(configuration, chatRequestDTO);
    }

    /**
     * Returns the appropriate service for the given provider type.
     */
    private AbstractLlmService getServiceForProvider(ProviderType providerType) {
        return switch (providerType) {
            case OLLAMA -> ollamaLlmService;
            //            case OPENAI -> throw new UnsupportedOperationException("OpenAI service not yet implemented");
            //            case ANTHROPIC -> throw new UnsupportedOperationException("Anthropic service not yet implemented");
        };
    }
}
