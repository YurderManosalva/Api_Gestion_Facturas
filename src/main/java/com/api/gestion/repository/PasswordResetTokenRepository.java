package com.api.gestion.repository;

import com.api.gestion.model.PasswordResetToken;
import com.api.gestion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByUser(User user);

    PasswordResetToken findByToken(String token);
}
