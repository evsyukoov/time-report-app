package api.service.impl;

import api.dto.FiltersDto;
import api.repository.ReportDayRepository;
import api.service.DocGeneratorService;
import hibernate.entities.Employee;
import hibernate.entities.ReportDay;
import messages.Message;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.DateTimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DocGeneratorServiceImpl implements DocGeneratorService {

    private ReportDayRepository daysRepository;

    public static int NUM_OF_ROWS = 4;


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
            fillList(reportList, sheet);
            workbook.write(fos);
        }
        fos.close();
        workbook.close();
    }

    private CellStyle initBackColor(HSSFColor.HSSFColorPredefined color, Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(color.getColor().getIndex());
        return cellStyle;
    }

    private void fillHeader(Map<String, List<ReportDay>> reportList, Sheet sheet) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("Фамилия");
        cell.setCellStyle(initBackColor(HSSFColor.HSSFColorPredefined.CORAL, sheet.getWorkbook()));
        LocalDate localDate = getFirstDate(reportList);
        int lenOfMonth = localDate.lengthOfMonth();
        int year = localDate.getYear();
        for (int i = 1; i <= lenOfMonth; i++) {
            Cell dateCell = row.createCell(i, CellType.STRING);
            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.CORAL.getColor().getIndex());
            DataFormat df = sheet.getWorkbook().createDataFormat();
            cellStyle.setDataFormat(df.getFormat("yyyy-MM-dd"));
            LocalDate nextDay = LocalDate.of(year, localDate.getMonth(), i);
            dateCell.setCellValue(nextDay);
            dateCell.setCellStyle(cellStyle);
        }
    }

    private LocalDate getFirstDate(Map<String, List<ReportDay>> reportList) {
        return DateTimeUtils.toLocalDate(
                reportList.values().stream()
                        .findFirst().get().get(0)
                        .getDate());
    }

    private void fillList(Map<String, List<ReportDay>> reportList, Sheet sheet) {
        fillHeader(reportList, sheet);
        //по отделам
        int i = 1;
        for (Map.Entry<String, List<ReportDay>> entry : reportList.entrySet()) {
            String department = entry.getKey();
            List<ReportDay> days = entry.getValue();
            Row depRow = sheet.createRow(i);
            Cell depCell = depRow.createCell(0);
            depCell.setCellValue(department);
            Map<Employee, List<ReportDay>> reportDaysOfEmployee = days.stream().collect(
                    Collectors.groupingBy(ReportDay::getEmployee,
                        Collectors.mapping(reportDay -> reportDay,
                            Collectors.toList())));
            i += NUM_OF_ROWS;
            //по людям в отделе
            for (Map.Entry<Employee, List<ReportDay>> employeeListEntry : reportDaysOfEmployee.entrySet()) {
                Employee employee = employeeListEntry.getKey();
                List<ReportDay> reportDays = employeeListEntry.getValue();
                Row row = sheet.createRow(i);
                Cell cell = row.createCell(0, CellType.STRING);
                cell.setCellValue(employee.getName());
                fillEmployeeReport(reportDays, i, sheet);
                i += NUM_OF_ROWS;
            }
            i -= NUM_OF_ROWS;

        }
    }

    private void fillEmployeeReport(List<ReportDay> reportDays, int rowNum, Sheet sheet) {
        for (ReportDay reportDay : reportDays) {
            int column = DateTimeUtils.toLocalDate(reportDay.getDate()).getDayOfMonth();
            AtomicInteger rowCounter = new AtomicInteger(rowNum);
            if (Objects.isNull(reportDay.getProjects())) {
                continue;
            }
            Arrays.stream(reportDay.getProjects().split(Message.DELIMETR))
                    .forEach(project -> {
                        Row row = sheet.createRow(rowCounter.get());
                        Cell cell = row.createCell(column, CellType.STRING);
                        cell.setCellValue(project);
                        rowCounter.incrementAndGet();
                    });
        }
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
