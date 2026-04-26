package com.neurixa.boot.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurixa.application.blog.ArticleCommandService;
import com.neurixa.application.blog.ArticleQueryService;
import com.neurixa.config.security.JwtAuthenticationEntryPoint;
import com.neurixa.config.security.JwtAuthenticationFilter;
import com.neurixa.config.security.JwtTokenProvider;
import com.neurixa.config.security.SecurityConfig;
import com.neurixa.config.security.TokenBlacklistService;
import com.neurixa.domain.blog.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlogArticleController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class BlogArticleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ArticleCommandService articleCommandService;
    @MockBean ArticleQueryService articleQueryService;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean TokenBlacklistService tokenBlacklistService;
    @MockBean UserDetailsService userDetailsService;

    // ── POST /api/blog/articles ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void createArticle_success_returns200() throws Exception {
        Article article = Article.createDraft("My Title", "Content here", "Short excerpt");
        when(articleCommandService.createDraft("My Title", "Content here", "Short excerpt"))
                .thenReturn(article);

        mockMvc.perform(post("/api/blog/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "My Title",
                                "content", "Content here",
                                "excerpt", "Short excerpt"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Title"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createArticle_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/blog/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "My Title",
                                "content", "Content",
                                "excerpt", "Excerpt"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createArticle_domainThrows_returns500() throws Exception {
        when(articleCommandService.createDraft(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Title cannot be empty."));

        mockMvc.perform(post("/api/blog/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "",
                                "content", "Content",
                                "excerpt", "Excerpt"))))
                .andExpect(status().isInternalServerError());
    }

    // ── GET /api/blog/articles ────────────────────────────────────────────────

    @Test
    @WithMockUser
    void listArticles_returns200WithPage() throws Exception {
        Article a1 = Article.createDraft("Post 1", "Content 1", "Excerpt 1");
        Article a2 = Article.createDraft("Post 2", "Content 2", "Excerpt 2");
        when(articleQueryService.listPublished(0, 10)).thenReturn(List.of(a1, a2));
        when(articleQueryService.countPublished()).thenReturn(2L);

        mockMvc.perform(get("/api/blog/articles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listArticles_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/blog/articles"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/blog/articles/{slug} ─────────────────────────────────────────

    @Test
    @WithMockUser
    void getArticleBySlug_found_returns200() throws Exception {
        Article article = Article.createDraft("Hello World", "Content", "Excerpt");
        String slug = article.getSlug().getValue();
        when(articleQueryService.getBySlug(slug)).thenReturn(article);

        mockMvc.perform(get("/api/blog/articles/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hello World"))
                .andExpect(jsonPath("$.slug").value(slug));
    }

    @Test
    @WithMockUser
    void getArticleBySlug_notFound_returns500() throws Exception {
        when(articleQueryService.getBySlug("not-found"))
                .thenThrow(new IllegalArgumentException("Article not found."));

        mockMvc.perform(get("/api/blog/articles/{slug}", "not-found"))
                .andExpect(status().isInternalServerError());
    }

    // ── PUT /api/blog/articles/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateArticle_success_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        Article updated = Article.createDraft("Updated Title", "Updated content", "Updated excerpt");
        when(articleCommandService.update(eq(id), anyString(), anyString(), anyString()))
                .thenReturn(updated);

        mockMvc.perform(put("/api/blog/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Updated Title",
                                "content", "Updated content",
                                "excerpt", "Updated excerpt"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateArticle_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/blog/articles/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "T", "content", "C", "excerpt", "E"))))
                .andExpect(status().isUnauthorized());
    }

    // ── DELETE /api/blog/articles/{id} ────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteArticle_success_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(articleCommandService).delete(id);

        mockMvc.perform(delete("/api/blog/articles/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void deleteArticle_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/blog/articles/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteArticle_publishedArticle_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new IllegalStateException("Cannot delete a published article directly (must archive first)."))
                .when(articleCommandService).delete(id);

        mockMvc.perform(delete("/api/blog/articles/{id}", id))
                .andExpect(status().isInternalServerError());
    }

    // ── POST /api/blog/articles/{id}/publish ──────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void publishArticle_success_returns200() throws Exception {
        Article article = Article.createDraft("Title", "Content", "Excerpt");
        article.publish();
        UUID id = article.getArticleId().getValue();
        when(articleCommandService.publish(id)).thenReturn(article);

        mockMvc.perform(post("/api/blog/articles/{id}/publish", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void publishArticle_notFound_returns500() throws Exception {
        UUID id = UUID.randomUUID();
        when(articleCommandService.publish(id))
                .thenThrow(new IllegalArgumentException("Article not found."));

        mockMvc.perform(post("/api/blog/articles/{id}/publish", id))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void publishArticle_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/blog/articles/{id}/publish", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /api/blog/articles/{id}/restore ──────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void restoreArticle_success_returns200() throws Exception {
        Article article = Article.createDraft("Title", "Content", "Excerpt");
        UUID id = article.getArticleId().getValue();
        when(articleCommandService.restore(id)).thenReturn(article);

        mockMvc.perform(post("/api/blog/articles/{id}/restore", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void restoreArticle_asUser_stillAllowed() throws Exception {
        // BlogArticleController has no @PreAuthorize — any authenticated user can call it
        Article article = Article.createDraft("Title", "Content", "Excerpt");
        UUID id = article.getArticleId().getValue();
        when(articleCommandService.restore(id)).thenReturn(article);

        mockMvc.perform(post("/api/blog/articles/{id}/restore", id))
                .andExpect(status().isOk());
    }
}
