package ru.evsyukov.app.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.evsyukov.app.api.dto.OperationResponse;
import ru.evsyukov.app.api.dto.RestEmployee;
import ru.evsyukov.app.api.dto.RestProject;

@RestController(value = "/admin")
@CrossOrigin
@Slf4j
public class AdminController {

    @PostMapping("/employee/add")
    public ResponseEntity<OperationResponse> addEmployee(@RequestBody RestEmployee employee) {
        return null;
    }

    @PostMapping("/project/add")
    public ResponseEntity<OperationResponse> addProject(@RequestBody RestProject project) {
        return null;
    }

    @DeleteMapping("/employee/remove")
    public ResponseEntity<OperationResponse> deleteEmployee(@RequestBody RestEmployee employee) {
        return null;
    }

    @DeleteMapping("/project/remove")
    public ResponseEntity<OperationResponse> deleteProject(@RequestBody RestProject project) {
        return null;
    }
}
