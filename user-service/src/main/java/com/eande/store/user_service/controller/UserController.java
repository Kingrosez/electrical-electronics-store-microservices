package com.eande.store.user_service.controller;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.BulkRegistrationResponse;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.exception.BadRequestException;
import com.eande.store.user_service.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Slf4j
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true"
)
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("Received request to register user with email: {}", request.email());
        UserResponse userResponse = userService.registerUser(request);
        log.info("Successfully registered user with email: {}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/bulk-register")
    public ResponseEntity<BulkRegistrationResponse> registerUsersBulk( @RequestBody @NotEmpty List<@Valid RegisterRequest> requests) {
        log.info("Received request to register {} users", requests.size());
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Request list cannot be empty");
        }
        BulkRegistrationResponse response = userService.registerUsersBulk(requests);
        log.info("Bulk registration completed: {} successful, {} failed out of {} total",
                response.successfulRegistrations(), response.failedRegistrations(), response.totalProcessed());
        if (response.failedRegistrations() == 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") UUID userId) {

        log.info("Fetching user with id: {}", userId);

        UserResponse response = userService.getUserById(userId);

        log.info("Successfully fetched user with id: {}", userId);

        return ResponseEntity.ok(response);
    }

}
