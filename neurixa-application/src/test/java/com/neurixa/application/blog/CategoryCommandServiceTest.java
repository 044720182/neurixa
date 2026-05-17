package com.neurixa.application.blog;

import com.neurixa.domain.blog.Category;
import com.neurixa.domain.blog.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryCommandServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryCommandService service;

    @Test
    void shouldCreateRootCategory() {
        Category result = service.create("Technology", null);

        assertThat(result.getName()).isEqualTo("Technology");
        assertThat(result.getParentId()).isNull();
        assertThat(result.getSlug().getValue()).isEqualTo("technology");
        verify(categoryRepository).save(argThat(c -> c.getName().equals("Technology") && c.getParentId() == null));
    }

    @Test
    void shouldCreateChildCategory() {
        UUID parentId = UUID.randomUUID();

        Category result = service.create("Java", parentId);

        assertThat(result.getName()).isEqualTo("Java");
        assertThat(result.getParentId()).isNotNull();
        assertThat(result.getParentId().getValue()).isEqualTo(parentId);
        verify(categoryRepository).save(argThat(c -> c.getParentId() != null));
    }

    @Test
    void shouldThrowWhenCreatingCategoryWithBlankName() {
        assertThatThrownBy(() -> service.create("", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name cannot be empty");
    }

    @Test
    void shouldThrowWhenCreatingCategoryWithNullName() {
        assertThatThrownBy(() -> service.create(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name cannot be empty");
    }

    @Test
    void shouldThrowWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new CategoryCommandService(null))
                .isInstanceOf(NullPointerException.class);
    }
}
