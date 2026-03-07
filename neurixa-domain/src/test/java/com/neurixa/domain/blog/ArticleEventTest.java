package com.neurixa.domain.blog;

import com.neurixa.domain.blog.event.ArticlePublishedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleEventTest {

    @Test
    void shouldEmitArticlePublishedEvent() {
        Article article = Article.createDraft("Title", "Content", "Excerpt");
        article.publish();
        var events = article.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ArticlePublishedEvent.class);
    }
}
