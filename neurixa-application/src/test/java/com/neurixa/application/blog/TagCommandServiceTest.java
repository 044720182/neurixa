package com.neurixa.application.blog;

import com.neurixa.domain.blog.Tag;
import com.neurixa.domain.blog.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TagCommandServiceTest {

    private TagRepository tagRepository;
    private TagCommandService service;

    @BeforeEach
    void setUp() {
        tagRepository = mock(TagRepository.class);
        service = new TagCommandService(tagRepository);
    }

    @Test
    void shouldCreateTag() {
        Tag result = service.create("spring-boot");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("spring-boot");
        assertThat(result.getSlug().getValue()).isEqualTo("spring-boot");
        assertThat(result.isDeleted()).isFalse();
        verify(tagRepository).save(result);
    }

    @Test
    void shouldThrowWhenCreatingTagWithBlankName() {
        assertThatThrownBy(() -> service.create(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag name cannot be empty");
    }

    @Test
    void shouldThrowWhenCreatingTagWithNullName() {
        assertThatThrownBy(() -> service.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag name cannot be empty");
    }

    @Test
    void shouldThrowWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new TagCommandService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
