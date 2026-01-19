package com.kanban.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanban.dto.BoardDTO;
import com.kanban.dto.CreateBoardRequest;
import com.kanban.model.User;
import com.kanban.repository.BoardRepository;
import com.kanban.security.JwtAuthenticationFilter;
import com.kanban.security.JwtUtil;
import com.kanban.service.BoardService;
import com.kanban.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BoardController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardService boardService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private BoardRepository boardRepository;

    // Mock security dependencies required by SecurityConfig
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private BoardDTO testBoardDTO;
    private CreateBoardRequest createRequest;

    @BeforeEach
    void setUp() {
        testBoardDTO = BoardDTO.builder()
                .id(1L)
                .name("Test Board")
                .description("Test Description")
                .workspaceId(1L)
                .createdBy(1L)
                .build();

        createRequest = new CreateBoardRequest();
        createRequest.setName("Test Board");
        createRequest.setDescription("Test Description");
        createRequest.setWorkspaceId(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateBoard_Success() throws Exception {
        when(permissionService.getCurrentUser()).thenReturn(User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .build());
        when(boardService.createBoard(any(CreateBoardRequest.class))).thenReturn(testBoardDTO);

        mockMvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Board"));

        verify(boardService, times(1)).createBoard(any(CreateBoardRequest.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetBoardById_Success() throws Exception {
        when(boardService.getBoardById(1L)).thenReturn(testBoardDTO);

        mockMvc.perform(get("/boards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Board"));

        verify(boardService, times(1)).getBoardById(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetBoardsByWorkspaceId_Success() throws Exception {
        List<BoardDTO> boards = Arrays.asList(testBoardDTO);
        when(boardService.getBoardsByWorkspaceId(1L)).thenReturn(boards);

        mockMvc.perform(get("/boards/workspace/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Board"));

        verify(boardService, times(1)).getBoardsByWorkspaceId(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateBoard_Success() throws Exception {
        when(permissionService.getCurrentUser()).thenReturn(User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .build());
        when(boardService.updateBoard(anyLong(), any(CreateBoardRequest.class))).thenReturn(testBoardDTO);

        mockMvc.perform(put("/boards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(boardService, times(1)).updateBoard(anyLong(), any(CreateBoardRequest.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteBoard_Success() throws Exception {
        when(permissionService.getCurrentUser()).thenReturn(User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .build());
        when(boardService.getBoardById(1L)).thenReturn(testBoardDTO);
        doNothing().when(boardService).deleteBoard(1L);

        mockMvc.perform(delete("/boards/1"))
                .andExpect(status().isNoContent());

        verify(boardService, times(1)).deleteBoard(1L);
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }
}

