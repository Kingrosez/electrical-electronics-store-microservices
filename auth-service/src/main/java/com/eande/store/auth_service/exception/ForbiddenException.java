package com.eande.store.auth_service.exception;

public class ForbiddenException extends BaseCustomException {

    public ForbiddenException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode, message);
    }

    public ForbiddenException(String message, String errorCode, int statusCode, String userMessage) {
        super(message, errorCode, statusCode, userMessage);
    }

    // Factory methods
    public static ForbiddenException insufficientRole() {
        return new ForbiddenException(
                "Insufficient role permissions",
                "INSUFFICIENT_ROLE",
                403,
                "You don't have permission to perform this action"
        );
    }

    public static ForbiddenException insufficientPermission() {
        return new ForbiddenException(
                "Insufficient permissions",
                "INSUFFICIENT_PERMISSION",
                403,
                "You don't have permission to access this resource"
        );
    }

    public static ForbiddenException resourceNotOwned() {
        return new ForbiddenException(
                "Resource does not belong to user",
                "RESOURCE_NOT_OWNED",
                403,
                "You don't own this resource"
        );
    }

    public static ForbiddenException adminAccessRequired() {
        return new ForbiddenException(
                "Admin access required",
                "ADMIN_ACCESS_REQUIRED",
                403,
                "Admin privileges are required for this operation"
        );
    }
}