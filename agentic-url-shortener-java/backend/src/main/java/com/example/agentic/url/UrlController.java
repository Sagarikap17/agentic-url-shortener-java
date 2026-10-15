package com.example.agentic.url;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class UrlController {
    private final UrlService service;
    public UrlController(UrlService service) { this.service = service; }
    public record CreateUrlRequest(@NotBlank @Size(max=2048) String url, @Size(max=32) String alias) {}
    @PostMapping("/urls")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateUrlRequest request) {
        ShortUrl saved = service.create(request.url(), request.alias());
        return ResponseEntity.created(URI.create("/" + saved.getCode())).body(Map.of("code", saved.getCode(), "originalUrl", saved.getOriginalUrl(), "shortUrl", "http://localhost:8080/" + saved.getCode(), "createdAt", saved.getCreatedAt().toString()));
    }
    @GetMapping("/urls/{code}/analytics") public Map<String, Object> analytics(@PathVariable String code) { return service.analytics(code); }
    @ExceptionHandler(UrlNotFoundException.class) public ResponseEntity<Map<String,String>> notFound(UrlNotFoundException e) { return ResponseEntity.status(404).body(Map.of("error", e.getMessage())); }
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
}
