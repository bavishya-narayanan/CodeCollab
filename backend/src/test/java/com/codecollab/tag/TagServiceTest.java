package com.codecollab.tag;

import com.codecollab.tag.dto.TagResponse;
import com.codecollab.tag.dto.TagSuggestionResponse;
import com.codecollab.tag.entity.Tag;
import com.codecollab.tag.mapper.TagMapper;
import com.codecollab.tag.repository.TagRepository;
import com.codecollab.tag.service.impl.TagServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock TagRepository tagRepository;
    @Mock TagMapper tagMapper;
    @InjectMocks TagServiceImpl tagService;

    @Test
    void resolveOrCreateTags_withExistingTag_doesNotCreateNew() {
        Tag existing = Tag.builder().id(1L).name("jwt").usageCount(5).build();
        when(tagRepository.findByNameIgnoreCase("jwt")).thenReturn(Optional.of(existing));

        Set<Tag> result = tagService.resolveOrCreateTags(Set.of("jwt"));

        assertThat(result).containsExactly(existing);
        verify(tagRepository, never()).save(any());
    }

    @Test
    void resolveOrCreateTags_withNewTag_createsIt() {
        when(tagRepository.findByNameIgnoreCase("newtag")).thenReturn(Optional.empty());
        Tag created = Tag.builder().id(2L).name("newtag").usageCount(0).build();
        when(tagRepository.save(any(Tag.class))).thenReturn(created);

        Set<Tag> result = tagService.resolveOrCreateTags(Set.of("newtag"));

        assertThat(result).hasSize(1);
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void resolveOrCreateTags_trimsToMaxTen() {
        // Create 12 tags — service should trim to 10
        Set<String> rawTags = Set.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l");
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Set<Tag> result = tagService.resolveOrCreateTags(rawTags);

        assertThat(result).hasSizeLessThanOrEqualTo(10);
    }

    @Test
    void normalization_convertsToCamelCaseToDashCase() {
        String normalized = Tag.normalize("JWT Auth");
        assertThat(normalized).isEqualTo("jwt-auth");
    }

    @Test
    void normalization_handlesSpecialChars() {
        assertThat(Tag.normalize("C++")).isEqualTo("c++");
        assertThat(Tag.normalize("  spring-boot  ")).isEqualTo("spring-boot");
        assertThat(Tag.normalize("C#")).isEqualTo("c#");
    }

    @Test
    void listTopTags_delegatesToRepository() {
        Tag tag = Tag.builder().id(1L).name("java").usageCount(10).build();
        Page<Tag> page = new PageImpl<>(List.of(tag));
        when(tagRepository.findTopTags(PageRequest.of(0, 5))).thenReturn(page);
        when(tagMapper.toResponse(tag)).thenReturn(new TagResponse(1L, "java", 10));

        List<TagResponse> result = tagService.listTopTags(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("java");
    }

    @Test
    void suggestTags_withBlankPrefix_returnsEmpty() {
        List<TagSuggestionResponse> result = tagService.suggestTags("  ", 10);

        assertThat(result).isEmpty();
        verify(tagRepository, never()).searchByPrefix(anyString(), any());
    }

    @Test
    void suggestTags_withValidPrefix_queriesRepo() {
        Tag tag = Tag.builder().id(1L).name("jwt").usageCount(5).build();
        when(tagRepository.searchByPrefix("jw", PageRequest.of(0, 5))).thenReturn(List.of(tag));
        when(tagMapper.toSuggestion(tag)).thenReturn(new TagSuggestionResponse("jwt", 5));

        List<TagSuggestionResponse> result = tagService.suggestTags("jw", 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("jwt");
    }
}
