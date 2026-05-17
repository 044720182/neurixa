package com.neurixa.domain.blog.exception;

/**
 * Thrown when an article cannot be found by ID or slug.
 * Maps to HTTP 404 Not Found.
 */
public class ArticleNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ArticleNotFoundException(String message) {
        super(message);
    }
}
