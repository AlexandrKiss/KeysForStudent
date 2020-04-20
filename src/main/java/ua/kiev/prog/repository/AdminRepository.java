package ua.kiev.prog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.kiev.prog.models.AdminUser;

public interface AdminRepository extends JpaRepository<AdminUser, Long> {
    AdminUser findByUserID(long id);
    AdminUser findById(long id);
    AdminUser findByAdmin(boolean admin);
}
