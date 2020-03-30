package ua.kiev.prog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.kiev.prog.models.CustomMessage;


public interface MessageRepository extends JpaRepository<CustomMessage, Long> {
    CustomMessage findById(long id);

}
