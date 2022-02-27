package hibernate.access;

import hibernate.entities.Project;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.Query;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectsDao {
    final private static SessionFactory factory;

    static {
        factory = new Configuration()
                .configure("hibernate_conf.cfg.xml")
                .buildSessionFactory();
    }

    private static boolean isTableContainsProject(Session session, String proj) {
        String sql = String.format("select * from projects WHERE project_name like '%s'", proj);
        Query query = session.createSQLQuery(sql).addEntity(Project.class);
        List<Project> result = query.getResultList();
        return result != null && !result.isEmpty();
    }

    public static List<String> getAllProjectsNames() {
        List<Project> projects;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            projects = session.createQuery("from Project", Project.class).list();
            session.getTransaction().commit();
        }
        return projects.stream()
                .map(Project::getProjectName)
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<Project> getProjects() {
        List<Project> projects;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            projects = session.createQuery("from Project", Project.class).list();
            session.getTransaction().commit();
        }
        return projects.stream()
                .sorted(Comparator.comparing(p -> p.getProjectName().toUpperCase()))
                .collect(Collectors.toList());
    }

    public static Project getProjectById(String id) {
        Project project;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            project = session.createQuery("from Project WHERE id=:id", Project.class)
                                        .setParameter("id", Long.parseLong(id))
                                        .list()
                                        .stream()
                                        .findFirst()
                                        .orElse(null);
            session.getTransaction().commit();
        }
        return project;
    }
}
