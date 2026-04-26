package com.neurixa.application.blog;

import com.neurixa.domain.blog.Category;
import com.neurixa.domain.blog.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CategoryCommandServiceTest {

    private CategoryRepository categoryRepository;
    private CategoryCommandService service;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        service = new CategoryCommandService(categoryRepository);
    }

    @Test
    void shouldCreateRootCategory() {
        Category result = service.create("Technology", null);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Technology");
        assertThat(result.getParentId()).isNull();
        assertThat(result.getSlug().getValue()).isEqualTo("technology");
        verify(categoryRepository).save(result);
    }

    @Test
    void shouldCreateChildCategory() {
        UUID parentId = UUID.randomUUID();

        Category result = service.create("Java", parentId);

        assertThat(result.getName()).isEqualTo("Java");
        assertThat(result.getParentId()).isNotNull();
        assertThat(result.getParentId().getValue()).isEqualTo(parentId);
        verify(categoryRepository).save(result);
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
