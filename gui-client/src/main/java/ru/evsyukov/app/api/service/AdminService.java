package ru.evsyukov.app.api.service;

import ru.evsyukov.app.api.dto.RestEmployee;
import ru.evsyukov.app.api.dto.RestProject;

public interface AdminService {

    void addEmployee(RestEmployee employee);

    void addProject(RestProject project);

    void deleteEmployee(RestEmployee employee);

    void deleteProject(RestProject project);
}
