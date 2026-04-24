package com.eande.store.auth_service.exception;

public class ServiceUnavailableException extends BaseCustomException {

    private final String serviceName;

    public ServiceUnavailableException(String serviceName) {
        super(
                String.format("%s service is unavailable", serviceName),
                "SERVICE_UNAVAILABLE",
                503,
                String.format("%s service is temporarily unavailable. Please try again later.", serviceName)
        );
        this.serviceName = serviceName;
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(
                String.format("%s service is unavailable", serviceName),
                "SERVICE_UNAVAILABLE",
                503,
                String.format("%s service is temporarily unavailable. Please try again later.", serviceName),
                cause
        );
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    // Factory methods
    public static ServiceUnavailableException userServiceDown() {
        return new ServiceUnavailableException("User Service");
    }

    public static ServiceUnavailableException cacheServiceDown() {
        return new ServiceUnavailableException("Cache Service");
    }

    public static ServiceUnavailableException databaseUnavailable() {
        return new ServiceUnavailableException("Database");
    }
}