package com.eande.store.auth_service.exception;

public class AccessDeniedException extends BaseCustomException {

    public AccessDeniedException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode, message);
    }

    public AccessDeniedException(String message, String errorCode, int statusCode, String userMessage) {
        super(message, errorCode, statusCode, userMessage);
    }

    // Factory methods
    public static AccessDeniedException insufficientRole() {
        return new AccessDeniedException(
                "Insufficient role permissions",
                "INSUFFICIENT_ROLE",
                403,
                "You don't have permission to perform this action"
        );
    }

    public static AccessDeniedException insufficientPermission() {
        return new AccessDeniedException(
                "Insufficient permissions",
                "INSUFFICIENT_PERMISSION",
                403,
                "You don't have permission to access this resource"
        );
    }

    public static AccessDeniedException resourceNotOwned() {
        return new AccessDeniedException(
                "Resource does not belong to user",
                "RESOURCE_NOT_OWNED",
                403,
                "You don't own this resource"
        );
    }

    public static AccessDeniedException adminAccessRequired() {
        return new AccessDeniedException(
                "Admin access required",
                "ADMIN_ACCESS_REQUIRED",
                403,
                "Admin privileges are required for this operation"
        );
    }
}