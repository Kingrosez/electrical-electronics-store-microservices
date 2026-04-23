package com.eande.store.user_service.exception;

import com.eande.store.user_service.dto.request.RegisterRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    @PostMapping("/validation")
    public String validation(@RequestBody @jakarta.validation.Valid RegisterRequest request) {
        return "OK";
    }

    @GetMapping("/illegal")
    public String illegal() {
        throw new IllegalArgumentException("Invalid argument");
    }

    @GetMapping("/bad-request")
    public String badRequest() {
        throw new BadRequestException("Bad request error");
    }

    @GetMapping("/conflict")
    public String conflict() {
        throw new ResourceAlreadyExistsException("Already exists");
    }

    @GetMapping("/generic")
    public String generic() {
        throw new RuntimeException("Something went wrong");
    }
}
