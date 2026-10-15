package com.example.agentic.url;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
public class UrlService {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final ShortUrlRepository repository;
    private final SecureRandom random = new SecureRandom();
    public UrlService(ShortUrlRepository repository) { this.repository = repository; }

    @Transactional
    public ShortUrl create(String rawUrl, String requestedAlias) {
        URI uri;
        try { uri = URI.create(rawUrl); } catch (RuntimeException e) { throw new IllegalArgumentException("A valid absolute URL is required"); }
        if (uri.getScheme() == null || !(uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) || uri.getHost() == null)
            throw new IllegalArgumentException("Only absolute HTTP/HTTPS URLs are supported");
        String code = requestedAlias == null || requestedAlias.isBlank() ? generateCode() : requestedAlias.trim();
        if (!code.matches("[A-Za-z0-9_-]{3,32}")) throw new IllegalArgumentException("Alias must be 3-32 characters: letters, numbers, _ or -");
        if (repository.existsByCode(code)) throw new IllegalArgumentException("Short code already exists");
        return repository.save(new ShortUrl(uri.toString(), code));
    }
    private String generateCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 7; i++) code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            if (!repository.existsByCode(code.toString())) return code.toString();
        }
        throw new IllegalStateException("Could not allocate a unique short code; retry the request");
    }
    @Transactional(readOnly = true)
    public ShortUrl find(String code) { return repository.findByCode(code).orElseThrow(() -> new UrlNotFoundException(code)); }
    @Transactional
    public ShortUrl resolveAndCount(String code) {
        ShortUrl result = repository.findByCode(code).orElseThrow(() -> new UrlNotFoundException(code));
        result.incrementClicks(); return result;
    }
    public Map<String, Object> analytics(String code) {
        ShortUrl url = find(code);
        return Map.of("code", url.getCode(), "originalUrl", url.getOriginalUrl(), "clicks", url.getClicks(), "createdAt", url.getCreatedAt().toString());
    }
}
