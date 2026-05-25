package com.interview.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Demonstrates Mockito: mocking, stubbing, verification, argument captors, spy.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito Demos")
class MockitoTest {

    // --- Domain classes ---

    interface UserRepository {
        Optional<User> findById(String id);
        User save(User user);
        List<User> findAll();
    }

    interface EmailService {
        void sendWelcomeEmail(String to, String name);
    }

    record User(String id, String name, String email) {}

    static class UserService {
        private final UserRepository repo;
        private final EmailService emailService;

        UserService(UserRepository repo, EmailService emailService) {
            this.repo = repo;
            this.emailService = emailService;
        }

        User register(String name, String email) {
            User user = new User(java.util.UUID.randomUUID().toString(), name, email);
            User saved = repo.save(user);
            emailService.sendWelcomeEmail(email, name);
            return saved;
        }

        User getUser(String id) {
            return repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found: " + id));
        }
    }

    // --- Tests ---

    @Mock UserRepository userRepo;
    @Mock EmailService emailService;
    @InjectMocks UserService userService;

    @Test
    @DisplayName("stubbing — when/thenReturn")
    void shouldReturnUserWhenFound() {
        User expected = new User("1", "Selva", "selva@test.com");
        when(userRepo.findById("1")).thenReturn(Optional.of(expected));

        User result = userService.getUser("1");

        assertEquals("Selva", result.name());
    }

    @Test
    @DisplayName("stubbing — throw exception")
    void shouldThrowWhenUserNotFound() {
        when(userRepo.findById("999")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getUser("999"));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("verify — method was called")
    void shouldSendWelcomeEmailOnRegistration() {
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.register("Selva", "selva@ericsson.com");

        verify(emailService, times(1)).sendWelcomeEmail("selva@ericsson.com", "Selva");
        verify(userRepo).save(any(User.class));
    }

    @Test
    @DisplayName("ArgumentCaptor — capture and assert arguments")
    void shouldCaptureEmailArguments() {
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.register("Selva", "selva@ericsson.com");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWelcomeEmail(emailCaptor.capture(), nameCaptor.capture());

        assertEquals("selva@ericsson.com", emailCaptor.getValue());
        assertEquals("Selva", nameCaptor.getValue());
    }

    @Test
    @DisplayName("spy — partial mock with real behavior")
    void shouldUseSpyForPartialMocking() {
        List<String> realList = new java.util.ArrayList<>();
        List<String> spyList = spy(realList);

        spyList.add("one");
        spyList.add("two");
        assertEquals(2, spyList.size()); // Real behavior

        // Override specific method
        doReturn(100).when(spyList).size();
        assertEquals(100, spyList.size()); // Stubbed behavior
    }

    @Test
    @DisplayName("verify — never called")
    void shouldNotSendEmailIfSaveFails() {
        when(userRepo.save(any())).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class,
                () -> userService.register("Selva", "selva@test.com"));

        verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
    }
}
