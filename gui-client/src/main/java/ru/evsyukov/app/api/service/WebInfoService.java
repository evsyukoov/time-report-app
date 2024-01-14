package ru.evsyukov.app.api.service;

import ru.evsyukov.app.api.dto.Department;

import java.util.List;

public interface WebInfoService {

    List<String> getEmployeesNames(boolean unused);

    List<String> getEmployeesNamesAutocomplete(String query);

    List<Department> getDepartments();

    List<String> getProjects(boolean unused);

    List<String> getProjectsAutocomplete(String query);

    List<Department> getDepartmentsAutocomplete(String query);

    int dbUpdate();

    void fixDbTestData();
}
