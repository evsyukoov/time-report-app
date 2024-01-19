package ru.evsyukov.app.data.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import ru.evsyukov.app.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, Long> {

    Project getProjectById(long id);

    Project getProjectByProjectName(String name);

    Project getProjectByProjectNameIgnoreCase(String name);

    @Query("SELECT projectName FROM Project ORDER BY UPPER(projectName) ASC")
    List<String> getAllProjectsNameSorted();

    @Query("SELECT projectName FROM Project ORDER BY UPPER(projectName) ASC")
    @Cacheable("projects")
    List<String> getAllProjectsNameSortedFromCache();

    List<Project> findByOrderByProjectNameAsc();

    @Transactional
    void deleteProjectsByProjectName(String name);
}
