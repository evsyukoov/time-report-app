package ru.evsyukov.app.api.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.evsyukov.app.api.dto.AutocompleteParams;
import ru.evsyukov.app.api.dto.Department;
import ru.evsyukov.app.api.service.WebInfoService;

import java.util.List;

@RestController
@CrossOrigin
@Slf4j
public class AutocompleteController {

    private final WebInfoService webInfoService;

    @Autowired
    public AutocompleteController(WebInfoService webInfoService) {
        this.webInfoService = webInfoService;
    }

    //эндпоинты для получения части списка по присланным вхождениям строки
    @PostMapping(path = "/autocomplete/get-projects")
    public List<String> getProjectsAutocomplete(@RequestBody AutocompleteParams params) {
        log.debug("POST request /report/get-projects-autocomplete");
        return webInfoService.getProjectsAutocomplete(params.getQuery());
    }

    @PostMapping(path = "/autocomplete/get-employees")
    public List<String> getEmployeesAutocomplete(@RequestBody AutocompleteParams params) {
        log.debug("POST request /report/get-employees-autocomplete");
        return webInfoService.getEmployeesNamesAutocomplete(params.getQuery());
    }

    @PostMapping(path = "/autocomplete/get-departments")
    public List<Department> getDepartmentsAutocomplete(@RequestBody AutocompleteParams params) {
        log.debug("POST request /report/get-employees-autocomplete");
        return webInfoService.getDepartmentsAutocomplete(params.getQuery());
    }
}
