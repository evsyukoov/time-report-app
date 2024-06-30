package ru.evsyukov.app.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.evsyukov.app.api.dto.output.OperationResponse;
import ru.evsyukov.app.api.dto.input.RestEmployee;
import ru.evsyukov.app.api.dto.input.RestProject;
import ru.evsyukov.app.api.dto.output.Status;
import ru.evsyukov.app.api.exception.BusinessException;
import ru.evsyukov.app.api.service.AdminService;

@RestController("/admin")
@RequestMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
@Slf4j
public class AdminController {

    private final ObjectMapper objectMapper;

    private final AdminService adminService;

    @Autowired
    public AdminController(ObjectMapper objectMapper,
                           AdminService adminService) {
        this.objectMapper = objectMapper;
        this.adminService = adminService;
    }

    @GetMapping(path = "/test")
    public ResponseEntity<?> test() {
        return null;
    }

    @PostMapping(path = "/employee/add")
    public ResponseEntity<OperationResponse> addEmployee(@RequestBody RestEmployee employee) throws JsonProcessingException {
        log.info("POST /admin/employee/add, body -  {}", objectMapper.writeValueAsString(employee));
        if (!isValidEmployee(employee)) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(Status.INCORRECT_INPUT).build());
        }
        try {
            adminService.addEmployee(employee);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(e.getReason()).build());
        } catch (Exception e) {
            log.error("Fatal Error - ", e);
            return ResponseEntity.internalServerError().body(OperationResponse.builder().status(Status.UNKNOWN_ERROR).build());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/project/add")
    public ResponseEntity<OperationResponse> addProject(@RequestBody RestProject project) throws JsonProcessingException {
        log.info("POST /admin/project/add, body -  {}", objectMapper.writeValueAsString(project));
        if (StringUtils.isEmpty(project.getProjectName())) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(Status.INCORRECT_INPUT).build());
        }
        try {
            adminService.addProject(project);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(e.getReason()).build());
        } catch (Exception e) {
            log.error("Fatal Error - ", e);
            return ResponseEntity.internalServerError().body(OperationResponse.builder().status(Status.UNKNOWN_ERROR).build());
        }
        return ResponseEntity.ok().build();
    }

    /**
     *
     * @param employee - сотрудник на удаление из системы
     * @return код ответа и статус
     * @throws JsonProcessingException
     */
    @DeleteMapping(path = "/employee/remove")
    public ResponseEntity<OperationResponse> deleteEmployee(@RequestBody RestEmployee employee) throws JsonProcessingException {
        log.info("POST /admin/employee/remove, body -  {}", objectMapper.writeValueAsString(employee));
        if (StringUtils.isEmpty(employee.getName())) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(Status.INCORRECT_INPUT).build());
        }
        try {
            adminService.deleteEmployee(employee);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(e.getReason()).build());
        } catch (Exception e) {
            log.error("Fatal Error - ", e);
            return ResponseEntity.internalServerError().body(OperationResponse.builder().status(Status.UNKNOWN_ERROR).build());
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/project/remove")
    public ResponseEntity<OperationResponse> deleteProject(@RequestBody RestProject project) {
        if (StringUtils.isEmpty(project.getProjectName())) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(Status.INCORRECT_INPUT).build());
        }
        try {
            adminService.deleteProject(project);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(e.getReason()).build());
        } catch (Exception e) {
            log.error("Fatal Error - ", e);
            return ResponseEntity.internalServerError().body(OperationResponse.builder().status(Status.UNKNOWN_ERROR).build());
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = "/employee/archive")
    public ResponseEntity<OperationResponse> archiveEmployee(@RequestBody RestEmployee employee) throws JsonProcessingException {
        log.info("PATCH /admin/employee/archive, body -  {}", objectMapper.writeValueAsString(employee));
        if (StringUtils.isEmpty(employee.getName())) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(Status.INCORRECT_INPUT).build());
        }
        try {
            adminService.archiveEmployee(employee);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(e.getReason()).build());
        } catch (Exception e) {
            log.error("Fatal Error - ", e);
            return ResponseEntity.internalServerError().body(OperationResponse.builder().status(Status.UNKNOWN_ERROR).build());
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping(path = "/project/archive")
    public ResponseEntity<OperationResponse> archiveProject(@RequestBody RestProject project) {
        if (StringUtils.isEmpty(project.getProjectName())) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(Status.INCORRECT_INPUT).build());
        }
        try {
            adminService.archiveProject(project);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(OperationResponse.builder().status(e.getReason()).build());
        } catch (Exception e) {
            log.error("Fatal Error - ", e);
            return ResponseEntity.internalServerError().body(OperationResponse.builder().status(Status.UNKNOWN_ERROR).build());
        }
        return ResponseEntity.ok().build();
    }

    private boolean isValidEmployee(RestEmployee employee) {
        return !StringUtils.isEmpty(employee.getName())
                && !StringUtils.isEmpty(employee.getPosition())
                && !StringUtils.isEmpty(employee.getDepartment())
                && !StringUtils.isEmpty(employee.getDepartmentShort())
                && employee.getName().split(" ").length == 3;
    }
}
