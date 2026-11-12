package br.com.estapar.poc.api.controller;

import br.com.estapar.poc.api.dto.CreateTagRequest;
import br.com.estapar.poc.api.dto.RechargeTagRequest;
import br.com.estapar.poc.api.dto.TagResponse;
import br.com.estapar.poc.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<TagResponse> activateTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = tagService.activateTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{tagCode}")
    public ResponseEntity<TagResponse> getByTagCode(@PathVariable String tagCode) {
        return ResponseEntity.ok(tagService.getByTagCode(tagCode));
    }

    @PostMapping("/{tagCode}/recharge")
    public ResponseEntity<TagResponse> rechargeBalance(
            @PathVariable String tagCode,
            @Valid @RequestBody RechargeTagRequest request) {
        return ResponseEntity.ok(tagService.rechargeBalance(tagCode, request));
    }

    @PatchMapping("/{tagCode}/block")
    public ResponseEntity<TagResponse> blockTag(@PathVariable String tagCode) {
        return ResponseEntity.ok(tagService.blockTag(tagCode));
    }
}
