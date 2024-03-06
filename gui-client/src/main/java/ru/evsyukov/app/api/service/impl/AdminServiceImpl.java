package ru.evsyukov.app.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.evsyukov.app.api.dto.input.RestEmployee;
import ru.evsyukov.app.api.dto.input.RestProject;
import ru.evsyukov.app.api.dto.output.Status;
import ru.evsyukov.app.api.exception.BusinessException;
import ru.evsyukov.app.api.mappers.DataMapper;
import ru.evsyukov.app.api.service.AdminService;
import ru.evsyukov.app.data.entity.Employee;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.data.entity.ReportDay;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final ProjectsRepository projectsRepository;

    private final EmployeeRepository employeeRepository;

    private final ReportDayRepository reportDayRepository;

    private final DataMapper dataMapper;

    @Autowired
    public AdminServiceImpl(ProjectsRepository projectsRepository,
                            EmployeeRepository employeeRepository,
                            ReportDayRepository reportDayRepository,
                            DataMapper dataMapper) {
        this.projectsRepository = projectsRepository;
        this.employeeRepository = employeeRepository;
        this.dataMapper = dataMapper;
        this.reportDayRepository = reportDayRepository;
    }

    @Override
    public void addEmployee(RestEmployee employee) {
        Employee empl = employeeRepository.getEmployeeByNameIgnoreCase(employee.getName());
        if (empl != null) {
            log.warn("Already contains employee with name {} at database", employee.getName());
            throw new BusinessException(Status.ALREADY_CONTAINS);
        }
        employeeRepository.save(dataMapper.restToDataEmployee(employee));
    }

    @Override
    public void addProject(RestProject project) {
        Project proj = projectsRepository.getProjectByProjectNameIgnoreCase(project.getProjectName());
        if (proj != null) {
            log.warn("Already contains project with name {} at database", project.getProjectName());
            throw new BusinessException(Status.ALREADY_CONTAINS);
        }
        projectsRepository.save(dataMapper.restToDataProject(project));
    }

    @Override
    public void deleteEmployee(RestEmployee employee) {
        List<ReportDay> days = reportDayRepository.findReportDayByEmployeeName(employee.getName());
        if (!CollectionUtils.isEmpty(days)) {
            log.warn("Нельзя удалить сотрудника {}, у которого есть отчеты", employee.getName());
            throw new BusinessException(Status.IMPOSSIBLE_DELETE);
        }
        employeeRepository.deleteEmployeeByName(employee.getName());
    }

    @Override
    public void deleteProject(RestProject project) {
        List<ReportDay> days = reportDayRepository.findReportDaysByProjectsContains(project.getProjectName());
        if (!CollectionUtils.isEmpty(days)) {
            log.warn("Нельзя удалить объект {}, на который ссылаются отчеты", project.getProjectName());
            throw new BusinessException(Status.IMPOSSIBLE_DELETE);
        }
        projectsRepository.deleteProjectsByProjectName(project.getProjectName());
    }

    @Override
    public void archiveEmployee(RestEmployee employee) {
        Optional<Employee> dbEmployeeOpt = employeeRepository.findByName(employee.getName());
        dbEmployeeOpt.ifPresentOrElse(e -> {
            e.setArchived(true);
            employeeRepository.save(e);
        }, () ->  {
            log.warn("Не найден сотрудник которого пытаются перенести в архив");
            throw new BusinessException(Status.NOT_FOUND);
        });
    }

    @Override
    public void archiveProject(RestProject project) {
        Optional<Project> dbProjectOpt = projectsRepository.findByProjectName(project.getProjectName());
        dbProjectOpt.ifPresentOrElse(p -> {
            p.setArchived(true);
            projectsRepository.save(p);
        }, () -> {
            log.warn("Не найден проект с таким названием");
            throw new BusinessException(Status.NOT_FOUND);
        });
    }
}
