package com.eande.store.user_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    @PrePersist
    public void onCreated() {
        this.createdAt = LocalDate.now();
    }
    @PreUpdate
    public void onUpdated() {
        this.updatedAt = LocalDate.now();
    }
}
