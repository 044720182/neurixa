package com.neurixa.boot.blog;

import com.neurixa.application.blog.ArticleCommandService;
import com.neurixa.application.blog.ArticleQueryService;
import com.neurixa.application.blog.CategoryCommandService;
import com.neurixa.application.blog.CommentCommandService;
import com.neurixa.application.blog.CommentQueryService;
import com.neurixa.application.blog.TagCommandService;
import com.neurixa.domain.blog.ArticleRepository;
import com.neurixa.domain.blog.CategoryRepository;
import com.neurixa.domain.blog.CommentRepository;
import com.neurixa.domain.blog.TagRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlogUseCaseConfiguration {

    @Bean
    public ArticleCommandService articleCommandService(ArticleRepository articleRepository) {
        return new ArticleCommandService(articleRepository);
    }

    @Bean
    public ArticleQueryService articleQueryService(ArticleRepository articleRepository) {
        return new ArticleQueryService(articleRepository);
    }

    @Bean
    public CategoryCommandService categoryCommandService(CategoryRepository categoryRepository) {
        return new CategoryCommandService(categoryRepository);
    }

    @Bean
    public TagCommandService tagCommandService(TagRepository tagRepository) {
        return new TagCommandService(tagRepository);
    }

    @Bean
    public CommentCommandService commentCommandService(CommentRepository commentRepository) {
        return new CommentCommandService(commentRepository);
    }

    @Bean
    public CommentQueryService commentQueryService(CommentRepository commentRepository) {
        return new CommentQueryService(commentRepository);
    }
}
