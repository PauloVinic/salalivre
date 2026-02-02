package br.com.fiap.salalivre.interfaces.api.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SalaLivre API",
                version = "v1"
        )
)
public class OpenApiConfig {
}
