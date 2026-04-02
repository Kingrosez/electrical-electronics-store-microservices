package com.eande.store.user_service.controller;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.BulkRegistrationResponse;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("Received request to register user with email: {}", request.email());
        UserResponse userResponse = userService.registerUser(request);
        log.info("Successfully registered user with email: {}", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

//    @PostMapping("/bulk-register")
//    public ResponseEntity<BulkRegistrationResponse> registerUsersBulk( @RequestBody List<@Valid RegisterRequest> requests) {
//        log.info("Received request to register {} users", requests.size());
//        BulkRegistrationResponse response = userService.registerUsersBulk(requests);
//        log.info("Bulk registration completed: {} successful, {} failed out of {} total",
//                response.successfulRegistrations(), response.failedRegistrations(), response.totalProcessed());
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

}
