package ru.evsyukov.app.api.service;

import ru.evsyukov.app.api.dto.input.RestEmployee;
import ru.evsyukov.app.api.dto.input.RestProject;

public interface AdminService {

    void addEmployee(RestEmployee employee);

    void addProject(RestProject project);

    void deleteEmployee(RestEmployee employee);

    void deleteProject(RestProject project);

    void archiveEmployee(RestEmployee employee);

    void archiveProject(RestProject project);
}
