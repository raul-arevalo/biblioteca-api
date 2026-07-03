package com.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaAPI() {

        return new OpenAPI()

                .info(new Info()

                        .title("Biblioteca API")

                        .version("1.0")

                        .description("API REST para la gestión de una biblioteca")

                        .contact(new Contact()

                                .name("Universidad de El Salvador")

                                .email("biblioteca@ues.edu.sv")));
    }

}
