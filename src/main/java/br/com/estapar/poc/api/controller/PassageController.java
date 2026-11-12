package br.com.estapar.poc.api.controller;

import br.com.estapar.poc.api.dto.PassageResponse;
import br.com.estapar.poc.api.dto.RegisterPassageRequest;
import br.com.estapar.poc.service.PassageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/passages")
public class PassageController {

    private final PassageService passageService;

    public PassageController(PassageService passageService) {
        this.passageService = passageService;
    }

    @PostMapping
    public ResponseEntity<PassageResponse> registerPassage(@Valid @RequestBody RegisterPassageRequest request) {
        PassageResponse response = passageService.registerPassage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tag/{tagCode}")
    public ResponseEntity<List<PassageResponse>> listByTagCode(@PathVariable String tagCode) {
        return ResponseEntity.ok(passageService.listByTagCode(tagCode));
    }
}
