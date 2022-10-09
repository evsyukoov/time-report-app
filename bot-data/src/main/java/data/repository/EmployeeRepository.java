package data.repository;

import hibernate.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAll();

    @Query("SELECT name FROM Employee order by name ASC")
    List<String> getAllEmployeeNames();

    Employee getEmployeeByName(String name);
}
