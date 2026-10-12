
package com.anurag.ons.service;

import com.anurag.ons.entity.User;
import com.anurag.ons.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User createUser(User user) {
        return repo.save(user);
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return repo.findById(id);
    }

    public User updateUser(Long id, User user) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setPassword(user.getPassword());
        existing.setRole(user.getRole());

        return repo.save(existing);
    }

    public void deleteUser(Long id) {
        repo.deleteById(id);
    }

    public User login(String email, String password) {
        return repo.findByEmail(email)
                .filter(user -> user.getPassword() != null
                        && user.getPassword().equals(password))
                .orElse(null);
    }
}
