package com.deploytrack.controller;

import com.deploytrack.dto.UserDTO;
import com.deploytrack.enums.UserRole;
import com.deploytrack.model.User;
import com.deploytrack.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User user = User.builder()
                .username(request.username())
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(request.passwordHash())
                .role(request.role())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(userRepository.save(user)));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDTO> updateRole(@PathVariable Long id, @RequestParam UserRole role) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role);
            return ResponseEntity.ok(toDTO(userRepository.save(user)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        return dto;
    }

    public record CreateUserRequest(
            String username,
            String fullName,
            String email,
            String passwordHash,
            UserRole role
    ) {}
}
