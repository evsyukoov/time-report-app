package ru.evsyukov.app.api.service;


import ru.evsyukov.app.api.dto.input.FiltersDto;

import java.io.ByteArrayOutputStream;


public interface DocGeneratorService {

    /**
     * Генерация стандартного отчета по сотрудникам (+ процентовки с различными параметрами)
     */
    ByteArrayOutputStream generateReport(FiltersDto dto) throws Exception;

    /**
     * Генерация excel-файла со списком сотрудник-дата его последнего отчета
     */
    ByteArrayOutputStream generateLastReports() throws Exception;
}
