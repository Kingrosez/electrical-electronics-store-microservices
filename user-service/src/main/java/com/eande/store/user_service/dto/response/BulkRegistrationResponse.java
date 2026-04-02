package com.eande.store.user_service.dto.response;

import com.eande.store.user_service.dto.request.RegisterRequest;

import java.util.List;

public record BulkRegistrationResponse(
        int totalProcessed,
        int successfulRegistrations,
        int failedRegistrations,
        List<UserResponse> successDetails,
        List<FailedRegistration> failureDetails
) {
    public record FailedRegistration(
            RegisterRequest request,
            String errorMessage
    ) {
    }
}
