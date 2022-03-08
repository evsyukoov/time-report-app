package api.service.impl;

import api.repository.EmployeeRepository;
import api.service.WebInfoService;
import hibernate.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
}
