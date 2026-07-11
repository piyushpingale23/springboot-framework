package api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Learner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String name;
    String email;
    String mobile;

    public Learner() {

    }

    public Learner(String email, Long id, String name, String mobile) {
        this.email = email;
        this.id = id;
        this.name = name;
        this.mobile = mobile;
    }


}
