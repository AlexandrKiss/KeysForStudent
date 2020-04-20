package ua.kiev.prog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.kiev.prog.models.CustomUser;

public interface UserRepository extends JpaRepository<CustomUser, Long> {
    CustomUser findByUserID(long id);
    int countByAdmin(boolean val);
}
