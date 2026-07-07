package com.bardock.discordvoicebot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Discord Listener Voice Bot API")
                        .version("v1.0")
                        .description("REST API for consulting guild members' voice time statistics"));
    }

}
