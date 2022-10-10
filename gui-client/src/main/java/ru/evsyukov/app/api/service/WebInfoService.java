package ru.evsyukov.app.api.service;

import java.util.List;

public interface WebInfoService {

    List<String> getEmployeesNames();

    List<String> getDepartments();

    int dbUpdate();

    void fixDbTestData();
}
