package com.infnet.artigoEventos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/api/teste/healthcheck")
    public ResponseEntity<String> testeHealthCheck() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            return ResponseEntity.ok("OK rodando... :D");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("api indisponível... falha no banco de dados xP");
        }
    }

    @GetMapping(value = "/api/teste/deploy", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getDeployStatus() {
        Resource resource = new ClassPathResource("static/deploy.html");

        if (resource.exists()) {
            return ResponseEntity.ok().body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}