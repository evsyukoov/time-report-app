package hibernate.access;

import hibernate.entities.Client;
import hibernate.entities.Notification;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import stateMachine.State;
import utils.Utils;

import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationDao {

    final private static SessionFactory factory;

    static {
        factory = new Configuration()
                .configure("hibernate_conf.cfg.xml")
                .buildSessionFactory();
    }

    public static void saveClientOption(LocalDateTime dateTime, long uid) {
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Notification notification = new Notification();
            notification.setUid(uid);
            notification.setNextFireTime(dateTime);
            session.saveOrUpdate(notification);
            session.getTransaction().commit();
        }
    }

    public static void updateFireTime(long uid) {
        LocalDateTime nextFireTime = getNextFireTime(uid);
        // не обновляем если не выставлено получение обновлений и если дата оповещения уже стоит на след день
        if (nextFireTime == null || nextFireTime.toLocalDate().
                isAfter(LocalDateTime.now().toLocalDate())) {
            return;
        }
        saveClientOption(nextFireTime.plusHours(24), uid);
    }


    private static LocalDateTime getNextFireTime(long uid) {
        LocalDateTime nextFireTime;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<LocalDateTime> query = session.createQuery("SELECT nextFireTime FROM Notification" +
                    " WHERE uid = :uid", LocalDateTime.class);
            query.setParameter("uid", uid);
            if (Utils.isEmpty(query.getResultList())) {
                return null;
            }
            nextFireTime = query.getResultList().get(0);
            session.getTransaction().commit();
        }
        return nextFireTime;
    }

    // получаем клиентов, которым пора получить сообщение
    public static List<Client> getClients(LocalDateTime time) {
        List<Client> result;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<Notification> query = session.createQuery("FROM Notification " +
                    "WHERE nextFireTime <= :time", Notification.class);
            query.setParameter("time", time);

            List<Notification> notificationList = query.getResultList();
            notificationList.forEach(notification -> notification.
                    setNextFireTime(notification.getNextFireTime().plusHours(24)));

            result = notificationList
                    .stream()
                    .map(Notification::getClient)
                    .collect(Collectors.toList());
            session.getTransaction().commit();

        }
        return result;
    }
}
