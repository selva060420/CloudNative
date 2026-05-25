package com.interview.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Demonstrates Mockito: mocking, stubbing, verification, argument captors, spy.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Mockito Demos")
class MockitoTest {

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
        java.util.List<String> realList = new java.util.ArrayList<>();
        java.util.List<String> spyList = spy(realList);

        spyList.add("one");
        spyList.add("two");
        assertEquals(2, spyList.size());

        doReturn(100).when(spyList).size();
        assertEquals(100, spyList.size());
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
