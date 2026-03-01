package com.neurixa.application.blog;

import com.neurixa.domain.blog.Tag;
import com.neurixa.domain.blog.TagRepository;
import java.util.Objects;

public class TagCommandService {

    private final TagRepository tagRepository;

    public TagCommandService(TagRepository tagRepository) {
        this.tagRepository = Objects.requireNonNull(tagRepository);
    }

    public Tag create(String name) {
        Tag tag = Tag.create(name);
        tagRepository.save(tag);
        return tag;
    }
}
