package hibernate.access;

import hibernate.entities.Client;
import hibernate.entities.Project;
import hibernate.entities.ReportDay;
import messages.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import utils.DateTimeUtils;
import utils.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportDaysDao {
    final private static SessionFactory factory;

    static {
        factory = new Configuration()
                .configure("hibernate_conf.cfg.xml")
                .buildSessionFactory();
    }

    public static boolean isReportToday(Client client) {
        boolean result;
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<ReportDay> query = session.createQuery("from ReportDay WHERE date = :date AND uid = :uid", ReportDay.class);
            Date now = DateTimeUtils.fromLocalDate(LocalDate.now());
            query.setParameter("date", now);
            query.setParameter("uid", client.getUid());
            result = !Utils.isEmpty(query.getResultList());
        }
        return result;
    }

    public static List<LocalDate> getClientsReportDates(Client client, int depth) {
        List<LocalDate> days;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<Date> query = session.createQuery("SELECT date FROM ReportDay WHERE uid = :uid AND date BETWEEN :startDate AND :endDate",
                    Date.class);
            LocalDate current = DateTimeUtils.toLocalDate(new Date());
            query.setParameter("uid", client.getUid());
            query.setParameter("startDate", DateTimeUtils.fromLocalDate(current.minusDays(depth)));
            query.setParameter("endDate", DateTimeUtils.fromLocalDate(current.minusDays(1)));
            days = query.getResultList().stream().map(date -> DateTimeUtils.toLocalDate(date)).collect(Collectors.toList());
            session.getTransaction().commit();
        }
        return days;
    }

    public static void saveOrUpdate(Client client, String description) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<ReportDay> query = session.createQuery("from ReportDay WHERE uid=:uid AND date=:date", ReportDay.class);

            query.setParameter("date", DateTimeUtils.fromLocalDate(client.getDateTime().toLocalDate()));
            query.setParameter("uid", client.getUid());

            List<ReportDay> dayList = query.getResultList();
            if (!dayList.isEmpty()) {
                ReportDay rd = dayList.get(0);
                rd.setProjects(getFinalProjects(client));
                session.update(rd);
            } else {
                ReportDay rd = new ReportDay();
                rd.setEmployee(EmployeeDao.getEmployeeByName(client.getName()));
                rd.setProjects(getFinalProjects(client));
                rd.setUid(client.getUid());
                // время ставим по МСК
                rd.setDate(DateTimeUtils.fromLocalDate(client.getDateTime().toLocalDate()));
                session.save(rd);
            }
            session.getTransaction().commit();
        }
    }

    private static String getFinalProjects(Client client) {
        Set<Project> projects = new HashSet<>();
        projects.add(ProjectsDao.getProjectById(client.getProject()));
        Set<Project> extraProjects = null;
        if (client.getExtraProjects() != null) {
            extraProjects = getProjectsFromIds(client.getExtraProjects());
        }
        if (extraProjects != null && !extraProjects.isEmpty()) {
            projects.addAll(extraProjects);
        }
        return projects.stream().map(Project::getProjectName)
                .collect(Collectors.joining(Message.DELIMETR));
    }

    private static Set<Project> getProjectsFromIds(String extraProjects) {
        return Arrays.stream(extraProjects.split(Message.DELIMETR))
                .map(ProjectsDao::getProjectById)
                .collect(Collectors.toSet());
    }

    public static void saveOrUpdate(Client client, Date date) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<ReportDay> query = session.createQuery("from ReportDay WHERE uid=:uid AND date=:date", ReportDay.class);

            query.setParameter("date", date);
            query.setParameter("uid", client.getUid());

            List<ReportDay> dayList = query.getResultList();
            if (!dayList.isEmpty()) {
                ReportDay rd = dayList.get(0);
                rd.setProjects(getFinalProjects(client));
                session.update(rd);
            } else {
                ReportDay rd = new ReportDay();
                rd.setEmployee(EmployeeDao.getEmployeeByName(client.getName()));
                rd.setProjects(getFinalProjects(client));
                rd.setUid(client.getUid());
                // время ставим по МСК
                rd.setDate(date);
                session.save(rd);
            }
            session.getTransaction().commit();
        }
    }
}
