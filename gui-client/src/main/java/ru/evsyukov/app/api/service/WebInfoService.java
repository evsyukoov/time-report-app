package ru.evsyukov.app.api.service;

import java.util.List;

public interface WebInfoService {

    List<String> getEmployeesNames(boolean unused);

    List<String> getDepartments();

    List<String> getProjects(boolean unused);

    int dbUpdate();

    void fixDbTestData();
}
