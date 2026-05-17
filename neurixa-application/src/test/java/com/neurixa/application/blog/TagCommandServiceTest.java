package com.neurixa.application.blog;

import com.neurixa.domain.blog.Tag;
import com.neurixa.domain.blog.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagCommandServiceTest {

    @Mock
    TagRepository tagRepository;

    @InjectMocks
    TagCommandService service;

    @Test
    void shouldCreateTag() {
        Tag result = service.create("spring-boot");

        assertThat(result.getName()).isEqualTo("spring-boot");
        assertThat(result.getSlug().getValue()).isEqualTo("spring-boot");
        assertThat(result.isDeleted()).isFalse();
        verify(tagRepository).save(argThat(t -> t.getName().equals("spring-boot")));
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
