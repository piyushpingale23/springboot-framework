package api.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long courseId;
    public String courseName;
    public String courseDescription;

    // One side always be the back referencing.
    // if we remove the 'mappedBy = "course"', it create separate joint table which redundant.
    @OneToMany(mappedBy = "course")
    public List<Cohorts> cohorts;

    public Course() {

    }

    public Course(Long courseId, String courseName, String courseDescription, List<Cohorts> cohorts) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.cohorts = cohorts;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public List<Cohorts> getCohorts() {
        return cohorts;
    }

    public void setCohorts(List<Cohorts> cohorts) {
        this.cohorts = cohorts;
    }
}
