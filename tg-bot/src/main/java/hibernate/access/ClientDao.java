package hibernate.access;

import hibernate.entities.Client;
import messages.Message;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import stateMachine.State;

import java.util.Date;
import java.util.List;

import java.time.LocalDateTime;

public class ClientDao {

    final private static SessionFactory factory;

    static {
        factory = new Configuration()
                .configure("hibernate_conf.cfg.xml")
                .buildSessionFactory();
    }


    public static Client getClient(final long id) {
        Client result;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            result = session.get(Client.class, id);
            session.getTransaction().commit();
        }
        return result;
    }

    public static Client createClient(final long id, final State state) {
        Client client;
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client = new Client();
            client.setUid(id);
            client.setState(state.ordinal());
            session.save(client);
            session.getTransaction().commit();
        }
        return client;
    }

    public static void updateState(Client client,  final int state) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setState(state);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static void updateDate(Client client,  final int state, LocalDateTime dateTime) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setState(state);
            client.setDateTime(dateTime);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static void updateStates(Client client, final int state) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setState(state);
            client.setDateTime(null);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static void updateName(Client client,
                                  final int current, final String name) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setState(current);
            client.setName(name);
            client.setRegistered(true);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static void clearClient(Client client) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setProject(null);
            client.setDateTime(null);
            client.setExtraProjects(null);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static void updateProject(Client client,
                                     final int state,
                                     final List<String> projects,
                                     LocalDateTime date) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setState(state);
            client.setProject(projects.get(0));
            if (projects.size() > 1) {
                client.setExtraProjects(String.join(Message.DELIMETR,
                                projects.subList(1, projects.size())));
            }
            client.setDateTime(date);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static void updateClientVacationInfo(Client client, final int state, Date start, Date end, boolean onVacation) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setOnVacation(onVacation);
            client.setStartVacation(start);
            client.setEndVacation(end);
            client.setState(state);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static void updateClientVacationInfo(Client client, boolean onVacation) {
        try(Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            client.setOnVacation(onVacation);
            session.update(client);
            session.getTransaction().commit();
        }
    }

    public static List<Client> getClientsWithVacations() {
        List<Client> result;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<Client> query = session.createQuery("FROM Client " +
                    "WHERE startVacation IS NOT NULL AND endVacation IS NOT NULL", Client.class);
            result = query.getResultList();
            session.getTransaction().commit();
        }
        return result;
    }
}
