package data.repository;

import hibernate.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, Long> {

    Project getProjectById(long id);

    @Query("SELECT projectName FROM Project ORDER BY UPPER(projectName) ASC")
    List<String> getAllProjectsNameSorted();
}
