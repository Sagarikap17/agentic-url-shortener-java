package com.example.agentic.url;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "short_urls", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class ShortUrl {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 2048) private String originalUrl;
    @Column(nullable = false, unique = true, length = 32) private String code;
    @Column(nullable = false) private long clicks = 0;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected ShortUrl() {}
    public ShortUrl(String originalUrl, String code) { this.originalUrl = originalUrl; this.code = code; }
    public Long getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getCode() { return code; }
    public long getClicks() { return clicks; }
    public void incrementClicks() { this.clicks++; }
    public Instant getCreatedAt() { return createdAt; }
}
