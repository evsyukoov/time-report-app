package api.service.impl;

import api.service.WebInfoService;
import data.repository.EmployeeRepository;
import data.repository.ProjectsRepository;
import data.repository.ReportDayRepository;
import hibernate.entities.Employee;
import hibernate.entities.ReportDay;
import messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class WebInfoServiceImpl implements WebInfoService {

    private EmployeeRepository employeeRepository;

    private ReportDayRepository reportDayRepository;

    private ProjectsRepository projectsRepository;

    @Autowired
    public WebInfoServiceImpl(EmployeeRepository employeeRepository, ReportDayRepository reportDayRepository, ProjectsRepository projectsRepository) {
        this.employeeRepository = employeeRepository;
        this.reportDayRepository = reportDayRepository;
        this.projectsRepository = projectsRepository;
    }

    @Override
    public List<String> getEmployeesNames() {
        return employeeRepository.findAll()
                .stream()
                .map(Employee::getName)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getDepartments() {
        return employeeRepository.findAll()
                .stream()
                .map(Employee::getDepartment)
                .distinct()
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
