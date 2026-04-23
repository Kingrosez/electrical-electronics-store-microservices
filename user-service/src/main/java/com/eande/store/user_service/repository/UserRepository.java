package com.eande.store.user_service.repository;

import com.eande.store.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @Query("SELECT u.email FROM User u WHERE u.email IN :emails")
    List<String> findExistingEmails(Set<String> emails);

    @Query("SELECT u.phone FROM User u WHERE u.phone IN :phones")
    List<String> findExistingPhones(Set<String> phones);

}
