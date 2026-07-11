package api.entity;

import jakarta.persistence.*;

@Entity
public class Cohorts {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long cohortId;
    public String cohortName;
    public String cohortDescription;

    // Many side always awns the relationship for database normalization
    @ManyToOne
    public Course course;

    public Cohorts() {

    }
    public Cohorts(Long cohortId, String cohortName, String cohortDescription, Course course) {
        this.cohortId = cohortId;
        this.cohortName = cohortName;
        this.cohortDescription = cohortDescription;
        this.course = course;
    }

    public Long getCohortId() {
        return cohortId;
    }

    public void setCohortId(Long cohortId) {
        this.cohortId = cohortId;
    }

    public String getCohortName() {
        return cohortName;
    }

    public void setCohortName(String cohortName) {
        this.cohortName = cohortName;
    }

    public String getCohortDescription() {
        return cohortDescription;
    }

    public void setCohortDescription(String cohortDescription) {
        this.cohortDescription = cohortDescription;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

}
