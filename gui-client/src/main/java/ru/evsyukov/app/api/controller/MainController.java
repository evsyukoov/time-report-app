package ru.evsyukov.app.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import ru.evsyukov.app.api.dto.FiltersDto;
import ru.evsyukov.app.api.service.DocGeneratorService;
import ru.evsyukov.app.api.service.WebInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@Slf4j
public class MainController {

    private final DocGeneratorService docGenerator;

    private final WebInfoService webInfoService;

    private final ObjectMapper om;

    private static final String CUSTOM_ERROR_UUID_HEADER = "Error-uuid";

    private static final String ACCESS_CONTROL_HEADER = "Access-Control-Expose-Headers";


    @Autowired
    public MainController(DocGeneratorService docGenerator,
                          WebInfoService webInfoService,
                          ObjectMapper objectMapper) {
        this.docGenerator = docGenerator;
        this.webInfoService = webInfoService;
        this.om = objectMapper;
    }

    @PostMapping(path = "/report/get-report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void getByName(@RequestBody FiltersDto requestDto, HttpServletResponse response) {
        try {
            log.info("POST request /report/get-report, body: {}", om.writeValueAsString(requestDto));
            ByteArrayOutputStream baos = docGenerator.generateXml(requestDto);
            response.setHeader("Content-disposition", "attachment;filename=report.xls");
            response.setContentType("application/vnd.ms-excel");
            if (baos.size() == 0) {
                response.getOutputStream().write(new byte[0]);
            } else {
                response.getOutputStream().write(baos.toByteArray());
            }
            response.getOutputStream().flush();
            response.getOutputStream().close();
            log.info("Successfully return response to front page");
        } catch (Exception e) {
            UUID error = UUID.randomUUID();
            log.error("{}. Request failed with error: ", error, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setHeader(CUSTOM_ERROR_UUID_HEADER, error.toString());
            response.setHeader(ACCESS_CONTROL_HEADER, CUSTOM_ERROR_UUID_HEADER);
        }
    }

    @GetMapping(path = "/report/get-employees")
    public List<String> getEmployesNames() {
        log.info("GET request /report/get-employees");
        return webInfoService.getEmployeesNames();
    }

    @GetMapping(path = "/report/get-departments")
    public List<String> getDepartments() {
        log.info("GET request /report/get-departments");
        return webInfoService.getDepartments();
    }

//    @GetMapping(path="/dbUpdate")
//    public ResponseEntity<String> dbUpdate() {
//        int count = webInfoService.dbUpdate();
//        return ResponseEntity.ok(String.format("\"count\":\"%d\"", count));
//    }

//    @GetMapping(path="/report/fixDb")
//    public ResponseEntity<String> fixDb() {
//        try {
//            webInfoService.fixDbTestData();
//            return ResponseEntity.ok("ok");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
}
