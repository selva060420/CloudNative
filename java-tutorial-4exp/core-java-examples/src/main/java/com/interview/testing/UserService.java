package com.interview.testing;

import java.util.UUID;

public class UserService {
    private final UserRepository repo;
    private final EmailService emailService;

    public UserService(UserRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    public User register(String name, String email) {
        User user = new User(UUID.randomUUID().toString(), name, email);
        User saved = repo.save(user);
        emailService.sendWelcomeEmail(email, name);
        return saved;
    }

    public User getUser(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}
