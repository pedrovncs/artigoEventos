package com.infnet.artigoEventos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @GetMapping //nao sei pq esse nao funciona
    public ResponseEntity<Map<String, String>> apiRoot() {
        return ResponseEntity.ok(Map.of("status", "Server rodando..."));
    }

    @GetMapping("/teste")
    public String hello() {
        return ":D";
    }
}