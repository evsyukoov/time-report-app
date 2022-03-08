package api.controller;

import api.dto.FiltersDto;
import api.service.DocGeneratorService;
import api.service.WebInfoService;
import api.service.impl.WebInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
public class MainController {

    private DocGeneratorService docGenerator;

    private WebInfoService webInfoService;

    @Autowired
    public MainController(DocGeneratorService docGenerator, WebInfoService webInfoService) {
        this.docGenerator = docGenerator;
        this.webInfoService = webInfoService;
    }

    @PostMapping(path = "/report/get-report")
    public void getByName(@RequestBody FiltersDto requestDto) throws Exception {
        docGenerator.generateXml(requestDto);
    }

    @GetMapping(path = "/report/get-employees")
    public List<String> getEmployesNames() throws Exception {
        return webInfoService.getEmployeesNames();
    }

    @GetMapping(path = "/report/get-departments")
    public List<String> getDepartments() throws Exception {
        return webInfoService.getDepartments();
    }
}
