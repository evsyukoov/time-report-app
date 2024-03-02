package ru.evsyukov.app.api.service;

import ru.evsyukov.app.api.dto.input.RestEmployee;
import ru.evsyukov.app.api.dto.input.RestProject;

public interface AdminService {

    void addEmployee(RestEmployee employee);

    void addProject(RestProject project);

    void deleteEmployee(RestEmployee employee, boolean approve);

    void deleteProject(RestProject project);
}
