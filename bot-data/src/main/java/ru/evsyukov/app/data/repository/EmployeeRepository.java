package ru.evsyukov.app.data.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import ru.evsyukov.app.data.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAll();

    @Query("FROM Employee")
    @Cacheable("employees")
    List<Employee> findAllFromCache();

    @Query("SELECT name FROM Employee order by name ASC")
    List<String> getAllEmployeeNames();

    @Query("SELECT name FROM Employee e " +
            "WHERE EXISTS " +
            "(SELECT 1 FROM ReportDay rd WHERE rd.employee.id = e.id)")
    List<String> getAllEmployeeNamesActual();

    @Query("SELECT name FROM Employee e " +
            "WHERE NOT EXISTS " +
            "(SELECT 1 FROM ReportDay rd WHERE rd.employee.id = e.id)")
    List<String> getAllEmployeeNamesNonActual();

    Employee getEmployeeByName(String name);

    Employee getEmployeeByNameIgnoreCase(String name);

    @Transactional
    long deleteEmployeeByName(String name);
}
