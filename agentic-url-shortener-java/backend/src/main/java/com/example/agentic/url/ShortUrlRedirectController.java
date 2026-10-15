package com.example.agentic.url;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ShortUrlRedirectController {
    private final UrlService service;

    public ShortUrlRedirectController(UrlService service) {
        this.service = service;
    }

    @GetMapping("/{code}")
    public RedirectView redirect(@PathVariable String code) {
        ShortUrl url = service.resolveAndCount(code);
        RedirectView view = new RedirectView(url.getOriginalUrl());
        view.setStatusCode(HttpStatus.FOUND);
        return view;
    }

    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }
}
