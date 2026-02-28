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
import java.util.Set;
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
                .roles(request.roles())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(userRepository.save(user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id,
                                           @Valid @RequestBody UpdateUserRequest request) {
        return userRepository.findById(id).map(user -> {
            if (!user.getEmail().equals(request.email())
                    && userRepository.existsByEmail(request.email())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).<UserDTO>build();
            }
            user.setFullName(request.fullName());
            user.setEmail(request.email());
            user.setRoles(request.roles());
            return ResponseEntity.ok(toDTO(userRepository.save(user)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setRoles(u.getRoles());
        return dto;
    }

    public record CreateUserRequest(
            String username,
            String fullName,
            String email,
            String passwordHash,
            Set<UserRole> roles
    ) {}

    public record UpdateUserRequest(
            String fullName,
            String email,
            Set<UserRole> roles
    ) {}
}
