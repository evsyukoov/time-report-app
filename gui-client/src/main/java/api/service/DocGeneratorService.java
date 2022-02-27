package api.service;


import api.dto.FiltersDto;

import java.io.FileNotFoundException;

public interface DocGeneratorService {

    void generateXml(FiltersDto dto) throws Exception;
}
