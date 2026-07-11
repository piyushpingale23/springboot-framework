package api.repository;

import api.entity.Learner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerRepository extends JpaRepository<Learner, Long> {

    List<Learner> findByName(String name);

    Optional<Learner> findByEmail(String email);

    Learner findByNameAndEmail(String name, String email);
}
