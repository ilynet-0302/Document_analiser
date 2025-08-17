package com.example.Document_analiser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Document Analyzer API",
                description = "Upload documents and ask context-based questions",
                contact = @Contact(name = "API Support", email = "support@example.com")
        )
)
public class DocumentAnaliserApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentAnaliserApplication.class, args);
	}

}