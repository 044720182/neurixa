package com.neurixa.core.exception;

/**
 * Exception thrown when an article is not found in the repository.
 * 
 * This is a specific domain exception for article lookup failures.
 * 
 * Example:
 * <pre>
 * throw new ArticleNotFoundException("Article not found")
 *     .withContext("articleId", articleId.getValue())
 *     .withContext("slug", slug);
 * </pre>
 */
public class ArticleNotFoundException extends DomainException {
    private static final String ERROR_CODE = "ARTICLE_NOT_FOUND";

    /**
     * Create an article not found exception.
     * 
     * @param message The human-readable error message
     */
    public ArticleNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Create an article not found exception with a cause.
     * 
     * @param message The human-readable error message
     * @param cause The underlying cause exception
     */
    public ArticleNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
