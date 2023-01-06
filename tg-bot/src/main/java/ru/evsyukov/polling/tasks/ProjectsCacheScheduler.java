package ru.evsyukov.polling.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.evsyukov.app.data.repository.ProjectsRepository;

import java.util.List;

/**
 * Таска чтобы перекладывать данные по проектам из базы в кеш
 * Сделано поскольку обновления списка проектов в базе крайне редки
 * А обращения очень частые + обращения используются в Inline -
 * Каждая отправленная клиентом буква - обращение на бек за проектами -> фильтрация (поиск подстроки в строке) -> отдача отфильтрованного списка клиенту
 */
@EnableScheduling
@Component
@Slf4j
public class ProjectsCacheScheduler {

    private List<String> cacheProjects;

    private ProjectsRepository projectsRepository;

    @Autowired
    public ProjectsCacheScheduler(List<String> cacheProjects, ProjectsRepository projectsRepository) {
        this.cacheProjects = cacheProjects;
        this.projectsRepository = projectsRepository;
    }

    @Scheduled(fixedRateString = "${projects-cache-scheduler.period}")
    public void updateCache() {
        int size = cacheProjects.size();
        log.debug("Start update projects cache. Size: {}", size);
        List<String> newProjects = projectsRepository.getAllProjectsNameSorted();
        cacheProjects.clear();
        cacheProjects.addAll(newProjects);
        if (size != newProjects.size()) {
            log.info("Successfully update projects cache. Size: {}", cacheProjects.size());
        }

    }
}
