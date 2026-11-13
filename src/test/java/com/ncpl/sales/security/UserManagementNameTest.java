package com.ncpl.sales.security;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserManagementNameTest {
    private UserService users;
    private MockMvc mvc;

    @Before public void setup() {
        users = mock(UserService.class);
        when(users.getAllUsers()).thenReturn(Collections.emptyList());
        when(users.saveUser(any(User.class))).thenAnswer(call -> call.getArgument(0));
        when(users.updateUser(any(User.class))).thenAnswer(call -> call.getArgument(0));
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", users);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test public void creatingWithoutTheRemovedNameFieldUsesUsername() throws Exception {
        mvc.perform(post("/api/users").param("user_name", "new_user").param("password", "password123")
                .param("role", "SALES").param("enabled", "true")
                .param("number", "8197563637").param("emailId", "user@example.test"))
                .andExpect(status().isCreated());
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).saveUser(saved.capture());
        assertEquals("new_user", saved.getValue().getName());
        assertEquals("8197563637", saved.getValue().getNumber());
    }

    private User existingUser() {
        User user = new User();
        user.setId(7L); user.setUsername("existing_user"); user.setName("Existing Name");
        user.setPassword("stored-password"); user.setEnabled(true);
        when(users.getUserByUserId(7L)).thenReturn(user);
        return user;
    }

    private MockHttpServletRequestBuilder editRequest() {
        return put("/api/users/7").param("username", "existing_user")
                .param("role", "SALES").param("enabled", "true")
                .param("number", "8197563637").param("emailId", "user@example.test");
    }

    @Test public void editingWithoutNamePreservesTheStoredNameAndPassword() throws Exception {
        User user = existingUser();
        mvc.perform(editRequest()).andExpect(status().isOk());
        verify(users).updateUser(user);
        assertEquals("Existing Name", user.getName());
        assertEquals("stored-password", user.getPassword());
        assertEquals("8197563637", user.getNumber());
    }

    @Test public void existingClientsCanStillProvideAName() throws Exception {
        User user = existingUser();
        mvc.perform(editRequest().param("name", "Provided Name")).andExpect(status().isOk());
        assertEquals("Provided Name", user.getName());
    }
}
