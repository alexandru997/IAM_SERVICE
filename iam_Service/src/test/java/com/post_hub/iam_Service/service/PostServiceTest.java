package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.kafka.service.KafkaMessageService;
import com.post_hub.iam_Service.mapper.PostMapper;
import com.post_hub.iam_Service.model.dto.post.PostDTO;
import com.post_hub.iam_Service.model.enteties.Post;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.request.post.PostRequest;
import com.post_hub.iam_Service.repositories.PostRepository;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.service.impl.PostServiceImpl;
import com.post_hub.iam_Service.utils.APIUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private APIUtils apiUtils;
    @Mock
    private KafkaMessageService kafkaMessageService;

    @InjectMocks
    private PostServiceImpl postService;

    private Post testPost;
    private PostDTO testPostDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("TestUser");

        testPost = new Post();
        testPost.setId(1);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setUser(testUser);

        testPostDTO = new PostDTO();
        testPostDTO.setId(1);
        testPostDTO.setTitle("Test Post");
        testPostDTO.setContent("Test Content");
    }

    @Test
    void getById_PostExists_ReturnsPostDTO() {
        when(postRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(testPost));
        when(postMapper.toPostDTO(testPost)).thenReturn(testPostDTO);

        PostDTO result = postService.getById(1).getPayload();

        assertNotNull(result);
        assertEquals(testPostDTO.getId(), result.getId());
        assertEquals(testPostDTO.getTitle(), result.getTitle());

        verify(postRepository, times(1)).findByIdAndDeletedFalse(1);
        verify(postMapper, times(1)).toPostDTO(testPost);
    }

    @Test
    void getByID_PostNotFound_ThrowsException() {
        when(postRepository.findByIdAndDeletedFalse(999)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> postService.getById(999));

        assertTrue(exception.getMessage().contains("not found"));

        verify(postRepository, times(1)).findByIdAndDeletedFalse(999);
        verify(postMapper, never()).toPostDTO(any(Post.class));
    }


    @Test
    void createPost_OK() {
        PostRequest request = new PostRequest("New Title", "New Content", 100);

        when(apiUtils.getUserIdFromAuthentication()).thenReturn(testUser.getId());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(postMapper.createPost(request, testUser, testUser.getUsername())).thenReturn(testPost);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(postMapper.toPostDTO(testPost)).thenReturn(testPostDTO);

        PostDTO result = postService.createPost(request).getPayload();

        assertNotNull(result);
        assertEquals(testPostDTO.getId(), result.getId());
        assertEquals(testPostDTO.getContent(), result.getContent());

        verify(apiUtils, times(1)).getUserIdFromAuthentication();
        verify(userRepository, times(1)).findById(testUser.getId());
        verify(postRepository, times(1)).save(any(Post.class));
        verify(postMapper, times(1)).toPostDTO(any(Post.class));
        verify(kafkaMessageService, times(1)).sendPostCreatedMessage(testUser.getId(), testPost.getId());

    }
}
