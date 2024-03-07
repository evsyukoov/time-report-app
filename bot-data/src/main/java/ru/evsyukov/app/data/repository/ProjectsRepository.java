package ru.evsyukov.app.data.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import ru.evsyukov.app.data.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectsRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectName(String name);

    Project getProjectById(long id);

    Project getProjectByProjectName(String name);

    Project getProjectByProjectNameIgnoreCase(String name);

    @Query("SELECT p FROM Project p ORDER BY UPPER(p.projectName) ASC")
    List<Project> getAllProjectsSorted();

    // проекты задействованные хотя бы в 1 отчете
    //TODO !!!! менять структуру БД, ReportDay должен ссылаться на таблицу проектов OneToMany
    @Query("SELECT p FROM Project p " +
            "WHERE EXISTS " +
            "(SELECT 1 FROM ReportDay rd WHERE rd.projects LIKE CONCAT('%', p.projectName, '%'))")
    List<Project> getAllActualProjects();

    // проекты незадействованные ни в одном отчете или помещенные на удаление
    @Query("SELECT p FROM Project p " +
            "WHERE NOT EXISTS " +
            "(SELECT 1 FROM ReportDay rd WHERE rd.projects LIKE CONCAT('%', p.projectName, '%'))")
    List<Project> getAllNotActualProjects();

    List<Project> findByOrderByProjectNameAsc();

    @Transactional
    void deleteProjectsByProjectName(String name);
}
