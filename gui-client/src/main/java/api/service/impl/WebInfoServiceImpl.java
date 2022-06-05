package api.service.impl;

import api.repository.EmployeeRepository;
import api.service.WebInfoService;
import hibernate.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class WebInfoServiceImpl implements WebInfoService {

    private EmployeeRepository employeeRepository;

    @Autowired
    public WebInfoServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<String> getEmployeesNames() {
        return employeeRepository.findAll()
                .stream().map(Employee::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getDepartments() {
        return employeeRepository.findAll()
                .stream()
                .map(Employee::getDepartment)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public int dbUpdate() {
        List<Employee> empls = employeeRepository.findAll();
        AtomicInteger i = new AtomicInteger();
        empls.stream().forEach(empl -> {
            if (empl.getDepartmentShort() != null) {
                return ;
            }
            if (empl.getDepartment().equals("Отдел инженерно-геологических изысканий")) {
                empl.setDepartmentShort("ИГИ");
            } else if (empl.getDepartment().equals("Отдел инженерно-геодезических изысканий")) {
                empl.setDepartmentShort("ИГДИ");
            } else if (empl.getDepartment().equals("Отдел инженерно-экологических изысканий")) {
                empl.setDepartmentShort("ИЭИ");
            } else if (empl.getDepartment().equals("Отдел инженерно-геофизических изысканий")) {
                empl.setDepartmentShort("ИГФИ");
            } else if (empl.getDepartment().equals("Отдел инженерно-гидрометеорологических изысканий")) {
                empl.setDepartmentShort("ИГМИ");
            } else if (empl.getDepartment().equals("Управление комплексных инженерных изысканий")) {
                empl.setDepartmentShort("УКИИ");
            } else {
                empl.setDepartmentShort(empl.getDepartment());
            }
            i.getAndIncrement();
        });
        employeeRepository.saveAll(empls);
        return i.get();
    }
}
