package ru.evsyukov.app.api.service;


import ru.evsyukov.app.api.dto.input.FiltersDto;

import java.io.ByteArrayOutputStream;


public interface DocGeneratorService {

    ByteArrayOutputStream generateXml(FiltersDto dto) throws Exception;
}
