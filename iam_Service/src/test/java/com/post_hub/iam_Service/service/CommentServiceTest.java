package com.post_hub.iam_Service.service;

import com.post_hub.iam_Service.mapper.CommentMapper;
import com.post_hub.iam_Service.model.dto.comment.CommentDTO;
import com.post_hub.iam_Service.model.enteties.Comment;
import com.post_hub.iam_Service.model.enteties.Post;
import com.post_hub.iam_Service.model.enteties.User;
import com.post_hub.iam_Service.model.exeption.NotFoundException;
import com.post_hub.iam_Service.model.request.comment.CommentRequest;
import com.post_hub.iam_Service.repositories.CommentRepository;
import com.post_hub.iam_Service.repositories.PostRepository;
import com.post_hub.iam_Service.repositories.UserRepository;
import com.post_hub.iam_Service.service.impl.CommentServiceImpl;
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
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private APIUtils apiUtils;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment tesComment;
    private CommentDTO testCommentDTO;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("TestUser");

        testPost = new Post();
        testPost.setId(1);
        testPost.setTitle("TestPost");

        tesComment = new Comment();
        tesComment.setId(1);
        tesComment.setMessage("Test Comment");
        tesComment.setPost(testPost);
        tesComment.setUser(testUser);

        testCommentDTO = new CommentDTO();
        testCommentDTO.setId(1);
        testCommentDTO.setMessage("Test Comment");
    }


    @Test
    void getCommentById_CommentExists_ReturnsCommentDTO() {
        when(commentRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(tesComment));
        when(commentMapper.toDto(tesComment)).thenReturn(testCommentDTO);

        CommentDTO result = commentService.getCommentById(1).getPayload();

        assertNotNull(result);
        assertEquals(testCommentDTO.getId(), result.getId());
        assertEquals(testCommentDTO.getMessage(), result.getMessage());

        verify(commentRepository, times(1)).findByIdAndDeletedFalse(1);
        verify(commentMapper, times(1)).toDto(tesComment);
    }

    @Test
    void getCommentById_CommentNotFound_ThrowsException() {
        when(commentRepository.findByIdAndDeletedFalse(999)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> commentService.getCommentById(999));

        assertTrue(exception.getMessage().contains("not found"));

        verify(commentRepository, times(1)).findByIdAndDeletedFalse(999);
        verify(commentMapper, never()).toDto(any(Comment.class));
    }
    @Test
    void createComment_OK() {
        CommentRequest request = new CommentRequest(1, "New comment");

        when(apiUtils.getUserIdFromAuthentication()).thenReturn(testUser.getId());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(postRepository.findByIdAndDeletedFalse(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentMapper.createComment(request, testUser, testPost)).thenReturn(tesComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(tesComment);
        when(commentMapper.toDto(tesComment)).thenReturn(testCommentDTO);

        CommentDTO result = commentService.createComment(request).getPayload();

        assertNotNull(result);
        assertEquals(testCommentDTO.getMessage(), result.getMessage());

        verify(apiUtils, times(1)).getUserIdFromAuthentication();
        verify(userRepository, times(1)).findById(testUser.getId());
        verify(postRepository, times(1)).findByIdAndDeletedFalse(testPost.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(commentMapper, times(1)).toDto(any(Comment.class));
    }
}
