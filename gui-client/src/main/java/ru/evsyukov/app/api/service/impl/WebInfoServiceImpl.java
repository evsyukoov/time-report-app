package ru.evsyukov.app.api.service.impl;

import ru.evsyukov.app.api.dto.Department;
import ru.evsyukov.app.api.mappers.DataMapper;
import ru.evsyukov.app.api.service.WebInfoService;
import ru.evsyukov.app.data.entity.Client;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.data.repository.ClientRepository;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.app.data.entity.Employee;
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
    public List<String> getEmployeesNames(boolean unused) {
        if (unused) {
            Set<String> registeredEmpls = clientRepository.getAllByRegisteredIs(true)
                    .stream()
                    .map(Client::getName)
                    .collect(Collectors.toSet());

            return employeeRepository.findAll()
                    .stream()
                    .map(Employee::getName)
                    .filter(name -> !registeredEmpls.contains(name))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return employeeRepository.findAll()
                .stream()
                .map(Employee::getName)
                .sorted()
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

    @Override
    public List<String> getProjects(boolean unused) {
        Set<String> usedProjects = reportDayRepository.findAll()
                .stream()
                .map(ReportDay::getProjects)
                .map(projects -> Arrays.asList(projects.split(Message.DELIMETR)))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        if (unused) {
            return projectsRepository.findAll()
                    .stream()
                    .map(Project::getProjectName)
                    .filter(proj -> !usedProjects.contains(proj))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return projectsRepository.findAll()
                .stream()
                .map(Project::getProjectName)
                .filter(usedProjects::contains)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public int dbUpdate() {
        List<Employee> empls = employeeRepository.findAll();
        AtomicInteger i = new AtomicInteger();
        empls.forEach(empl -> {
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

    @Override
    public void fixDbTestData() {
        List<String> currentDictProjects = projectsRepository.getAllProjectsNameSorted();
        int len = currentDictProjects.size();

        List<ReportDay> reportDays = reportDayRepository.findAll();
        reportDays.forEach(rd -> {
            StringBuilder sb = new StringBuilder();
            Arrays.stream(rd.getProjects().split(Message.DELIMETR)).forEach(proj -> {
                if (!currentDictProjects.contains(proj)) {
                    sb.append(currentDictProjects.get((int)(Math.random() * len))).append(Message.DELIMETR);
                } else {
                    sb.append(proj).append(Message.DELIMETR);
                }
            });
            rd.setProjects(sb.substring(0, sb.length() - 1));
        });
        List<ReportDay> copy = new ArrayList<>(reportDays);
        reportDayRepository.deleteAll();
        reportDayRepository.saveAll(copy);
    }

}
