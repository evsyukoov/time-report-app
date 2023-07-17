package ru.evsyukov.app.api.service;

import ru.evsyukov.app.api.dto.Department;

import java.util.List;

public interface WebInfoService {

    List<String> getEmployeesNames(boolean unused);

    List<Department> getDepartments();

    List<String> getProjects(boolean unused);

    int dbUpdate();

    void fixDbTestData();
}
