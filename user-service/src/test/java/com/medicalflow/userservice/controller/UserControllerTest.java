package com.medicalflow.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalflow.userservice.dto.UserProfileResponse;
import com.medicalflow.userservice.entity.Role;
import com.medicalflow.userservice.entity.User;
import com.medicalflow.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void getProfileReturnsUserProfile() throws Exception {
        User user = new User();
        user.setId(10L);
        user.setEmail("profile@example.com");
        user.setFullName("Profile User");
        user.setRole(Role.PATIENT);

        when(userService.findByEmail("profile@example.com")).thenReturn(user);

        mockMvc.perform(get("/users/profile")
                        .principal(new UsernamePasswordAuthenticationToken("profile@example.com", "password"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.fullName").value("Profile User"))
                .andExpect(jsonPath("$.role").value("PATIENT"));
    }

    @Test
    void getUserByIdReturnsUserProfile() throws Exception {
        User user = new User();
        user.setId(42L);
        user.setEmail("id@example.com");
        user.setFullName("Id User");
        user.setRole(Role.DOCTOR);

        when(userService.findById(anyLong())).thenReturn(user);

        mockMvc.perform(get("/users/42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.role").value("DOCTOR"));
    }

    @Test
    void getUserByEmailReturnsUserProfile() throws Exception {
        User user = new User();
        user.setId(55L);
        user.setEmail("email@example.com");
        user.setFullName("Email User");
        user.setRole(Role.ADMIN);

        when(userService.findByEmail(anyString())).thenReturn(user);

        mockMvc.perform(get("/users/by-email")
                        .param("email", "email@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
