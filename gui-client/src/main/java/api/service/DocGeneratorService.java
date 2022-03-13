package api.service;


import api.dto.FiltersDto;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;


public interface DocGeneratorService {

    ByteArrayOutputStream generateXml(FiltersDto dto) throws Exception;
}
