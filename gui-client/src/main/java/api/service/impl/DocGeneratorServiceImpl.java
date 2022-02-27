package api.service.impl;

import api.dto.FiltersDto;
import api.repository.ReportDayRepository;
import api.service.DocGeneratorService;
import hibernate.entities.ReportDay;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.DateTimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocGeneratorServiceImpl implements DocGeneratorService {

    private ReportDayRepository daysRepository;


    @Autowired
    public DocGeneratorServiceImpl(ReportDayRepository daysRepository) {
        this.daysRepository = daysRepository;
    }

    @Override
    public void generateXml(FiltersDto dto) throws Exception {
        List<ReportDay> days;
        if (dto.getName() == null) {
            days = daysRepository
                    .findReportDayByDateAfterAndDateBefore(dto.getDateStart(), dto.getDateEnd());
        } else {
            days = daysRepository.findReportDayByDateAfterAndDateBeforeAndEmployeeName(
                    dto.getDateStart(), dto.getDateEnd(), dto.getName());
        }
        createDoc(days);
    }

    private void createDoc(List<ReportDay> days) throws Exception {
        FileOutputStream fos = new FileOutputStream(
                new File("./src/main/resources/Report.xls"));
        Workbook workbook = new HSSFWorkbook();
        Map<Month, LocalDate> dates = getAllDates(days);
        for (Map.Entry<Month, LocalDate> entry : dates.entrySet()) {
            Month month = entry.getKey();
            LocalDate date = entry.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM yyyy");
            String listName = formatter.format(date);
            Sheet sheet = workbook.createSheet(listName);
            Map<String, List<ReportDay>> reportList = getExcelStructure(days, month);
//            Row row = sheet.createRow(0);
//            Cell cell = row.createCell(0, CellType.STRING);
//            cell.setCellValue("TEST");
            workbook.write(fos);
        }
        fos.close();
        workbook.close();
    }

    private Map<Month, LocalDate> getAllDates(List<ReportDay> days) {
        return days.stream().map(ReportDay::getDate)
                .map(DateTimeUtils::toLocalDate)
                .collect(Collectors
                        .toMap(LocalDate::getMonth, localDate -> localDate,
                                (k1, k2) -> k1.isBefore(k2) ? k1 : k2));
    }

    //отдел - отчеты за 1 месяц
    private Map<String, List<ReportDay>> getExcelStructure(List<ReportDay> days, Month month) {
        return days.stream()
                .filter(reportDay ->
                        DateTimeUtils.toLocalDate(reportDay.getDate()).getMonth().compareTo(month) >= 0
                                && DateTimeUtils.toLocalDate(reportDay.getDate()).getMonth().compareTo(month.plus(1)) < 0)
                .collect(Collectors.groupingBy(reportDay -> reportDay.getEmployee().getDepartment(),
                        Collectors.mapping(reportDay -> reportDay, Collectors.toList())));

    }
}
