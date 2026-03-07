package com.neurixa.domain.blog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugTest {

    @Test
    void shouldGenerateLowercaseDashSeparatedSlug() {
        Slug slug = new Slug("Hello World!");
        assertThat(slug.getValue()).isEqualTo("hello-world");
    }
}
