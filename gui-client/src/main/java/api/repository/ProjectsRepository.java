package api.repository;

import hibernate.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectsRepository extends JpaRepository<Project, Long> {

    @Query("SELECT projectName FROM Project")
    List<String> getAllProjectsName();
}
