package api.service.impl;

import api.dto.FiltersDto;
import api.helpers.styles.CellStyleHelper;
import api.helpers.styles.CellStyleType;
import api.repository.ReportDayRepository;
import api.service.DocGeneratorService;
import hibernate.entities.Employee;
import hibernate.entities.ReportDay;
import messages.Message;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import utils.DateTimeUtils;

import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocGeneratorServiceImpl implements DocGeneratorService {

    private final ReportDayRepository daysRepository;

    public static int NUM_OF_ROWS = 4;

    public static int DEFAULT_WIDTH_DATE_COLUMN = 20;

    public static int K_WIDTH = 200;

    private Map<CellStyleType, CellStyle> predefinedCellStyles;

    public DocGeneratorServiceImpl(ReportDayRepository daysRepository) {
        this.daysRepository = daysRepository;
    }

    @Override
    public ByteArrayOutputStream generateXml(FiltersDto dto) throws Exception {
        List<ReportDay> days;
        if (dto.getName() == null) {
            if (dto.getDateStart() == null && dto.getDateEnd() == null) {
                days = daysRepository.findAll();
            } else if (dto.getDateStart() == null && dto.getDateEnd() != null) {
                days = daysRepository.findReportDayByDateLessThanEqual(dto.getDateEnd());
            } else if (dto.getDateStart() != null && dto.getDateEnd() == null) {
                days = daysRepository.findReportDayByDateGreaterThanEqual(dto.getDateStart());
            } else {
                days = daysRepository
                        .findReportDayByDateGreaterThanEqualAndDateLessThanEqual(dto.getDateStart(), dto.getDateEnd());
            }
        } else {
            if (dto.getDateStart() == null && dto.getDateEnd() == null) {
                days = daysRepository.findReportDayByEmployeeName(dto.getName());
            } else if (dto.getDateStart() == null && dto.getDateEnd() != null) {
                days = daysRepository.findReportDayByDateLessThanEqualAndEmployeeName(dto.getDateEnd(), dto.getName());
            } else if (dto.getDateStart() != null && dto.getDateEnd() == null) {
                days = daysRepository.findReportDayByDateGreaterThanEqualAndEmployeeName(dto.getDateStart(), dto.getName());
            } else {
                days = daysRepository.findReportDayByDateGreaterThanEqualAndDateLessThanEqualAndEmployeeName(
                        dto.getDateStart(), dto.getDateEnd(), dto.getName());
            }
        }
        if (dto.getDepartment() != null) {
            days = days.stream()
                    .filter(reportDay -> Objects.equals(
                            reportDay.getEmployee().getDepartment(), dto.getDepartment()))
                    .collect(Collectors.toList());
        }
        if (days.isEmpty()) {
            return new ByteArrayOutputStream();
        }
        return createDoc(days);
    }

    private ByteArrayOutputStream createDoc(List<ReportDay> days) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HSSFWorkbook workbook = new HSSFWorkbook();
        predefinedCellStyles = CellStyleHelper.predefineCellStyles(workbook);
        Map<Month, LocalDate> dates = new TreeMap<>(getAllDates(days));
        for (Map.Entry<Month, LocalDate> entry : dates.entrySet()) {
            Month month = entry.getKey();
            LocalDate date = entry.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM yyyy");
            String listName = formatter.format(date);
            Sheet sheet = workbook.createSheet(listName);
            Map<String, List<ReportDay>> reportList = getExcelStructure(days, month);
            fillList(reportList, sheet);
            try {
                normilizeColumns(sheet, date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        workbook.write(baos);
        workbook.close();
        baos.close();
        return baos;
    }

    private void fillHeader(Map<String, List<ReportDay>> reportList, Sheet sheet) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("Фамилия");
        cell.setCellStyle(predefinedCellStyles.get(CellStyleType.SURNAME));

        LocalDate localDate = getFirstDate(reportList);
        int lenOfMonth = localDate.lengthOfMonth();
        int year = localDate.getYear();
        for (int i = 1; i <= lenOfMonth; i++) {
            Cell dateCell = row.createCell(i, CellType.STRING);
            dateCell.setCellStyle(predefinedCellStyles.get(CellStyleType.DATE));

            LocalDate nextDay = LocalDate.of(year, localDate.getMonth(), i);
            dateCell.setCellValue(nextDay);
        }
    }

    private void normilizeColumns(Sheet sheet, LocalDate localDate) {
        int lastRow = sheet.getLastRowNum();
        int column = -1;
        Month month = localDate.getMonth();
        int year = localDate.getYear();
        HashMap<Integer, Integer> columnWidthMap = new HashMap<>();
        for (int i = 0; i < lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            if (column == -1) {
                column = row.getLastCellNum();
            }
            for (int j = 0; j < column; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    continue;
                }
                Integer width = getCellContentLength(cell);
                if (columnWidthMap.get(j) == null || columnWidthMap.get(j) < width) {
                    columnWidthMap.put(j, width);
                }
            }
        }
        for (int i = 0; i < column; i++) {
            sheet.setColumnWidth(i, columnWidthMap.get(i) * 150);
            if (i != 0 && DateTimeUtils.isWeekend(LocalDate.of(year, month, i))) {
                sheet.setDefaultColumnStyle(i, predefinedCellStyles.get(CellStyleType.WEEKEND_COLUMN));
            } else {
                sheet.setDefaultColumnStyle(i, predefinedCellStyles.get(CellStyleType.DEFAULT_COLUMN));
            }
        }
    }


    private int getCellContentLength(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().getBytes().length;
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return DEFAULT_WIDTH_DATE_COLUMN;
        }
        return 0;
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
            Row depRow = sheet.createRow(i++);
            depRow.setRowStyle(predefinedCellStyles.get(CellStyleType.DEPARTMENT_ROW));

            Cell depCell = depRow.createCell(0);
            depCell.setCellValue(String.format("Отдел: %s", department));
            depCell.setCellStyle(predefinedCellStyles.get(CellStyleType.DEPARTMENT_ROW));

            Map<Employee, List<ReportDay>> reportDaysOfEmployee = days.stream().collect(
                    Collectors.groupingBy(ReportDay::getEmployee,
                            Collectors.mapping(reportDay -> reportDay,
                                    Collectors.toList())));
            //по людям в отделе
            for (Map.Entry<Employee, List<ReportDay>> employeeListEntry : reportDaysOfEmployee.entrySet()) {
                Employee employee = employeeListEntry.getKey();
                List<ReportDay> reportDays = employeeListEntry.getValue();
                Row[] rows = createRowsForReportDays(sheet, i);
                rows[0].setRowStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));
                Cell cell = rows[0].createCell(0, CellType.STRING);
                fillEmployeeReport(reportDays, rows, sheet);
                cell.setCellValue(employee.getName());
                cell.setCellStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));
                i += NUM_OF_ROWS;
            }
        }
    }

    private void setRowStyleCustom(Date date, Row row) {
        LocalDate localDate = DateTimeUtils.toLocalDate(date);
        for (int i = 0; i <= localDate.lengthOfMonth(); i++) {
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));
        }
    }

    private Row[] createRowsForReportDays(Sheet sheet, int rowNum) {
        Row[] rows = new Row[4];
        for (int i = 0; i < 4; i++) {
            rows[i] = sheet.createRow(rowNum++);
        }
        return rows;
    }

    private void fillEmployeeReport(List<ReportDay> reportDays, Row[] rows, Sheet sheet) {
        for (ReportDay reportDay : reportDays) {
            int column = DateTimeUtils.toLocalDate(reportDay.getDate()).getDayOfMonth();
            if (Objects.isNull(reportDay.getProjects())) {
                continue;
            }
            String[] projects = reportDay.getProjects().split(Message.DELIMETR);
            for (int i = 0; i < projects.length; i++) {
                Cell cell = rows[i].createCell(column, CellType.STRING);
                cell.setCellValue(projects[i]);
                if (i == 0) {
                    cell.setCellStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));
                } else {
                    cell.setCellStyle(predefinedCellStyles.get(CellStyleType.PROJECT_CELL));
                }
            }
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
