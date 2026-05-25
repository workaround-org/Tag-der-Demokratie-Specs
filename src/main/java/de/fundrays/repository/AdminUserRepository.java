package de.fundrays.repository;

import de.fundrays.model.AdminUser;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class AdminUserRepository implements PanacheRepository<AdminUser> {

    public Optional<AdminUser> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public boolean isLastAdmin() {
        return count() == 1;
    }
}
