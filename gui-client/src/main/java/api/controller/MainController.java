package api.controller;

import api.dto.FiltersDto;
import api.service.DocGeneratorService;
import api.service.WebInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin
public class MainController {

    private DocGeneratorService docGenerator;

    private WebInfoService webInfoService;

    @Autowired
    public MainController(DocGeneratorService docGenerator, WebInfoService webInfoService) {
        this.docGenerator = docGenerator;
        this.webInfoService = webInfoService;
    }

    @PostMapping(path = "/report/get-report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void getByName(@RequestBody FiltersDto requestDto, HttpServletResponse response) throws Exception {
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
    }

    @GetMapping(path = "/report/get-employees")
    public List<String> getEmployesNames() throws Exception {
        return webInfoService.getEmployeesNames();
    }

    @GetMapping(path = "/report/get-departments")
    public List<String> getDepartments() throws Exception {
        return webInfoService.getDepartments();
    }

    @GetMapping(path="/dbUpdate")
    public ResponseEntity<String> dbUpdate() {
        int count = webInfoService.dbUpdate();
        return ResponseEntity.ok(String.format("\"count\":\"%d\"", count));
    }
}
