package api.controller;

import api.dto.FiltersDto;
import api.service.DocGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;

@RestController
public class MainController {

    private DocGeneratorService docGenerator;

    @Autowired
    public MainController(DocGeneratorService docGenerator) {
        this.docGenerator = docGenerator;
    }

    @PostMapping(path = "/report/get-report")
    public void getByName(@RequestBody FiltersDto requestDto) throws Exception {
        docGenerator.generateXml(requestDto);
    }
}
