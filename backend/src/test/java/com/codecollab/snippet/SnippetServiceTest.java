package com.codecollab.snippet;

import com.codecollab.snippet.dto.CreateSnippetRequest;
import com.codecollab.snippet.dto.UpdateSnippetRequest;
import com.codecollab.snippet.entity.Snippet;
import com.codecollab.snippet.entity.SnippetLanguage;
import com.codecollab.snippet.exception.SnippetAccessDeniedException;
import com.codecollab.snippet.exception.SnippetNotFoundException;
import com.codecollab.snippet.mapper.SnippetMapper;
import com.codecollab.snippet.repository.SnippetRepository;
import com.codecollab.snippet.service.impl.SnippetServiceImpl;
import com.codecollab.tag.entity.Tag;
import com.codecollab.tag.service.TagService;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.codecollab.ast.service.AstAnalysisService;
import com.codecollab.ast.repository.AstResultRepository;
import com.codecollab.vote.repository.VoteRepository;
import com.codecollab.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnippetServiceTest {

    @Mock
    private SnippetRepository snippetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagService tagService;

    @Mock
    private AstAnalysisService astAnalysisService;

    @Mock
    private AstResultRepository astResultRepository;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private CommentRepository commentRepository;

    private SnippetMapper snippetMapper;

    @InjectMocks
    private SnippetServiceImpl snippetService;

    @BeforeEach
    void setUp() {
        SnippetMapper realMapper = new SnippetMapper(voteRepository, commentRepository);
        snippetMapper = org.mockito.Mockito.spy(realMapper);
        snippetService = new SnippetServiceImpl(snippetRepository, userRepository, snippetMapper, tagService, astAnalysisService, astResultRepository, redis, voteRepository, commentRepository);
    }

    @Test
    void create_throwsEntityNotFoundIfOwnerMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        CreateSnippetRequest req = new CreateSnippetRequest("title", "desc", "code", "java", null);
        assertThrows(EntityNotFoundException.class, () -> snippetService.create(1L, req));
    }

    @Test
    void create_throwsIllegalStateExceptionIfOwnerBanned() {
        User user = new User();
        user.setStatus(UserStatus.BANNED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        CreateSnippetRequest req = new CreateSnippetRequest("title", "desc", "code", "java", null);
        assertThrows(IllegalStateException.class, () -> snippetService.create(1L, req));
    }

    @Test
    void create_withTags_resolvesTagsAndIncrementsUsage() {
        User user = new User();
        user.setId(1L);
        user.setStatus(UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Snippet snippet = Snippet.builder()
                .id(1L).owner(user).title("title").code("code").language(SnippetLanguage.JAVA)
                .build();
        when(snippetRepository.save(any(Snippet.class))).thenReturn(snippet);

        Set<String> requestTags = Set.of("spring", "java");
        Tag tag1 = Tag.builder().id(10L).name("spring").build();
        Tag tag2 = Tag.builder().id(11L).name("java").build();
        when(tagService.resolveOrCreateTags(requestTags)).thenReturn(Set.of(tag1, tag2));

        CreateSnippetRequest req = new CreateSnippetRequest("title", "desc", "code", "java", requestTags);
        snippetService.create(1L, req);

        verify(snippetMapper).fromCreate(req, user);
        verify(tagService).resolveOrCreateTags(requestTags);
        verify(tagService).incrementTagUsage(Set.of(10L, 11L));
        // Verify save was called twice (once for snippet, once for tags relation)
        verify(snippetRepository, times(2)).save(any(Snippet.class));
    }

    @Test
    void getById_throwsSnippetNotFoundIfMissing() {
        when(snippetRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());
        assertThrows(SnippetNotFoundException.class, () -> snippetService.getById(1L, 2L));
    }

    @Test
    void getById_incrementsViewWhenViewerNotOwner() {
        User owner = new User();
        owner.setId(1L);
        Snippet snippet = Snippet.builder().id(100L).owner(owner).title("title").language(SnippetLanguage.JAVA).build();
        
        when(snippetRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(snippet));
        
        snippetService.getById(100L, 2L); // Viewer is 2

        verify(snippetRepository).incrementViewCount(100L);
    }

    @Test
    void getById_doesNotIncrementWhenViewerIsOwner() {
        User owner = new User();
        owner.setId(1L);
        Snippet snippet = Snippet.builder().id(100L).owner(owner).title("title").language(SnippetLanguage.JAVA).build();
        
        when(snippetRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(snippet));
        
        snippetService.getById(100L, 1L); // Viewer is 1 (owner)

        verify(snippetRepository, never()).incrementViewCount(any());
    }

    @Test
    void update_throwsSnippetAccessDeniedIfNotOwner() {
        User owner = new User();
        owner.setId(1L);
        Snippet snippet = Snippet.builder().id(100L).owner(owner).title("title").language(SnippetLanguage.JAVA).build();
        
        when(snippetRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(snippet));

        UpdateSnippetRequest req = new UpdateSnippetRequest("new", null, null, null, null);
        assertThrows(SnippetAccessDeniedException.class, () -> snippetService.update(100L, 2L, req));
    }

    @Test
    void update_withTagChanges_managesUsageCountsCorrectly() {
        User owner = new User();
        owner.setId(1L);
        
        Tag oldTag1 = Tag.builder().id(10L).name("java").build();
        Tag oldTag2 = Tag.builder().id(11L).name("spring").build();
        
        Snippet snippet = Snippet.builder().id(100L).owner(owner).title("title").language(SnippetLanguage.JAVA)
                .tags(new HashSet<>(Set.of(oldTag1, oldTag2))) // Mutable set
                .build();
                
        when(snippetRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(snippet));
        when(snippetRepository.save(snippet)).thenReturn(snippet);

        // We replace "spring" with "hibernate", keeping "java"
        Set<String> newTagNames = Set.of("java", "hibernate");
        Tag newTag3 = Tag.builder().id(12L).name("hibernate").build();
        when(tagService.resolveOrCreateTags(newTagNames)).thenReturn(Set.of(oldTag1, newTag3));

        UpdateSnippetRequest req = new UpdateSnippetRequest("new", null, null, null, newTagNames);
        snippetService.update(100L, 1L, req);

        // Should decrement removed tag "spring" (ID: 11)
        verify(tagService).decrementTagsForSnippet(Set.of(11L));
        // Should increment newly added tag "hibernate" (ID: 12)
        verify(tagService).incrementTagUsage(Set.of(12L));
        
        assertThat(snippet.getTags()).containsExactlyInAnyOrder(oldTag1, newTag3);
    }

    @Test
    void update_clearingTags_decrementsAllOldTags() {
        User owner = new User();
        owner.setId(1L);
        Tag tag1 = Tag.builder().id(10L).name("java").build();
        Snippet snippet = Snippet.builder().id(100L).owner(owner).title("title").language(SnippetLanguage.JAVA)
                .tags(new HashSet<>(Set.of(tag1)))
                .build();
                
        when(snippetRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(snippet));
        when(snippetRepository.save(snippet)).thenReturn(snippet);

        UpdateSnippetRequest req = new UpdateSnippetRequest(null, null, null, null, Set.of());
        snippetService.update(100L, 1L, req);

        verify(tagService).decrementTagsForSnippet(Set.of(10L));
        verify(tagService, never()).incrementTagUsage(any());
        assertThat(snippet.getTags()).isEmpty();
    }

    @Test
    void delete_softDeletesAndDecrementsTags() {
        User owner = new User();
        owner.setId(1L);
        Tag tag1 = Tag.builder().id(10L).name("java").build();
        Snippet snippet = Snippet.builder().id(100L).owner(owner).title("title").language(SnippetLanguage.JAVA)
                .tags(new HashSet<>(Set.of(tag1)))
                .build();
        
        when(snippetRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(snippet));

        snippetService.delete(100L, 1L);

        verify(snippetRepository).save(snippet);
        verify(tagService).decrementTagsForSnippet(Set.of(10L));
        assertThat(snippet.isDeleted()).isTrue();
    }

    @Test
    void listByOwner_delegatesToRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(snippetRepository.findAllByOwnerIdAndDeletedFalse(eq(1L), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of()));

        snippetService.listByOwner(1L, PageRequest.of(0, 10));

        verify(snippetRepository).findAllByOwnerIdAndDeletedFalse(eq(1L), any(PageRequest.class));
    }
}
