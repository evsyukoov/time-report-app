package ru.evsyukov.app.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import ru.evsyukov.app.api.dto.output.Department;
import ru.evsyukov.app.api.dto.output.Employee;
import ru.evsyukov.app.api.dto.output.Project;
import ru.evsyukov.app.api.mappers.DataMapper;
import ru.evsyukov.app.api.service.WebInfoService;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.app.data.entity.ReportDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.utils.messages.Message;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class WebInfoServiceImpl implements WebInfoService {

    private EmployeeRepository employeeRepository;

    private ReportDayRepository reportDayRepository;

    private ProjectsRepository projectsRepository;

    private ClientRepository clientRepository;

    private final DataMapper dataMapper;

    @Autowired
    public WebInfoServiceImpl(EmployeeRepository employeeRepository,
                              ReportDayRepository reportDayRepository,
                              ProjectsRepository projectsRepository,
                              ClientRepository clientRepository,
                              DataMapper dataMapper) {
        this.employeeRepository = employeeRepository;
        this.reportDayRepository = reportDayRepository;
        this.projectsRepository = projectsRepository;
        this.clientRepository = clientRepository;
        this.dataMapper = dataMapper;
    }

    @Override
    public List<Employee> getEmployees() {
        var actualEmployees = employeeRepository.getAllEmployeesActual().stream()
                .map(e -> new Employee(e.getName(), true, e.isArchived())).collect(Collectors.toList());
        var nonActualEmployees = employeeRepository.getAllEmployeesNonActual().stream()
                .map(e -> new Employee(e.getName(), false, e.isArchived())).collect(Collectors.toList());
        actualEmployees.addAll(nonActualEmployees);
        return actualEmployees.stream()
                .sorted(Comparator.comparing(Employee::getEmployeeName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Project> getProjects() {
        var actualProjects = projectsRepository.getAllActualProjects().stream()
                .map(proj -> new Project(proj.getProjectName(), true, proj.isArchived())).collect(Collectors.toList());
        var nonActualProjects = projectsRepository.getAllNotActualProjects().stream()
                .map(proj -> new Project(proj.getProjectName(), false, proj.isArchived())).collect(Collectors.toList());
        actualProjects.addAll(nonActualProjects);
        return actualProjects.stream()
                .sorted(Comparator.comparing(Project::getProjectName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Department> getDepartments() {
        return employeeRepository.findAll()
                .stream()
                .map(dataMapper::employeeToDepartment)
                .distinct()
                .collect(Collectors.toList());
    }

}
