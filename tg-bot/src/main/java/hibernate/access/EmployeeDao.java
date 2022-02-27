package hibernate.access;

import hibernate.entities.Employee;
import hibernate.entities.Project;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;

public class EmployeeDao {

    final private static SessionFactory factory;

    static {
        factory = new Configuration()
                .configure("hibernate_conf.cfg.xml")
                .buildSessionFactory();
    }

    public static List<String> getEmployeeNames() {
        List<String> result;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            result = session.createQuery("SELECT name From Employee", String.class).list();
            session.getTransaction().commit();
        }
        return result;
    }

    public static Employee getEmployeeByName(String name) {
        Employee employee;
        try (Session session = factory.getCurrentSession()) {
            session.beginTransaction();
            Query<Employee> query = session.createQuery("FROM Employee WHERE name=:name", Employee.class);
            query.setParameter("name", name);
            employee = query.getSingleResult();
            session.getTransaction().commit();
        }
        return employee;
    }
}
