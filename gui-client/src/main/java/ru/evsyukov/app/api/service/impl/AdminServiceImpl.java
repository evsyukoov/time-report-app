package ru.evsyukov.app.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.api.dto.RestEmployee;
import ru.evsyukov.app.api.dto.RestProject;
import ru.evsyukov.app.api.dto.Status;
import ru.evsyukov.app.api.exception.BusinessException;
import ru.evsyukov.app.api.mappers.DtoToDataMapper;
import ru.evsyukov.app.api.service.AdminService;
import ru.evsyukov.app.data.entity.Employee;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final ProjectsRepository projectsRepository;

    private final EmployeeRepository employeeRepository;

    private final DtoToDataMapper dtoToDataMapper;

    @Autowired
    public AdminServiceImpl(ProjectsRepository projectsRepository,
                            EmployeeRepository employeeRepository,
                            DtoToDataMapper dtoToDataMapper) {
        this.projectsRepository = projectsRepository;
        this.employeeRepository = employeeRepository;
        this.dtoToDataMapper = dtoToDataMapper;
    }

    @Override
    public void addEmployee(RestEmployee employee) {
        Employee empl = employeeRepository.getEmployeeByName(employee.getName());
        if (empl != null) {
            log.warn("Already contains employee with name {} at database", employee.getName());
            throw new BusinessException(Status.ALREADY_CONTAINS);
        }
        employeeRepository.save(dtoToDataMapper.restToDataEmployee(employee));
    }

    @Override
    public void addProject(RestProject project) {
        Project proj = projectsRepository.getProjectByProjectName(project.getProjectName());
        if (proj != null) {
            log.warn("Already contains project with name {} at database", project.getProjectName());
            throw new BusinessException(Status.ALREADY_CONTAINS);
        }
        projectsRepository.save(dtoToDataMapper.restToDataProject(project));
    }

    @Override
    public void deleteEmployee(RestEmployee employee) {

    }

    @Override
    public void deleteProject(RestProject project) {

    }
}
