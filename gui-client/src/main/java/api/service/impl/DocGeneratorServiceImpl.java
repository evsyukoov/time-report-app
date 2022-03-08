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
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.DateTimeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocGeneratorServiceImpl implements DocGeneratorService {

    private final ReportDayRepository daysRepository;

    public static int NUM_OF_ROWS = 4;

    public static int DEFAULT_WIDTH_DATE_COLUMN = 20;

    public static int K_WIDTH = 200;


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
        workbook.write(fos);
        workbook.close();
        fos.close();
    }

    private CellStyle initBackColor(HSSFColor.HSSFColorPredefined color, Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(color.getColor().getIndex());
        setBorders(cellStyle);
        return cellStyle;
    }

    private void setBorders(CellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
    }

    private CellStyle initBordersStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        setBorders(cellStyle);
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
            CellStyle cellStyle = cell.getCellStyle();
            setBorders(cellStyle);

            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.CORAL.getColor().getIndex());
            DataFormat df = sheet.getWorkbook().createDataFormat();
            cellStyle.setDataFormat(df.getFormat("yyyy-MM-dd"));
            dateCell.setCellStyle(cellStyle);
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
                sheet.setDefaultColumnStyle(i,
                        initBackColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT, sheet.getWorkbook()));
            } else {
                CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
                setBorders(cellStyle);
                sheet.setDefaultColumnStyle(i, cellStyle);
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

    private CellStyle initBoldAndBordersStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        return initBoldAndBordersStyle(style, workbook);
    }

    private CellStyle initBoldAndBordersStyle(CellStyle style, Workbook workbook) {
        Font fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        style.setFont(fontHeader);
        setBorders(style);
        return style;
    }

    private void fillList(Map<String, List<ReportDay>> reportList, Sheet sheet) {
        fillHeader(reportList, sheet);
        //по отделам
        int i = 1;
        for (Map.Entry<String, List<ReportDay>> entry : reportList.entrySet()) {
            String department = entry.getKey();
            List<ReportDay> days = entry.getValue();
            Row depRow = sheet.createRow(i++);
            CellStyle cellStyle = initBackColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN, sheet.getWorkbook());
            depRow.setRowStyle(cellStyle);
            Cell depCell = depRow.createCell(0);
            depCell.setCellValue(String.format("Отдел: %s", department));
            depCell.setCellStyle(initBoldAndBordersStyle(cellStyle, sheet.getWorkbook()));
            Map<Employee, List<ReportDay>> reportDaysOfEmployee = days.stream().collect(
                    Collectors.groupingBy(ReportDay::getEmployee,
                            Collectors.mapping(reportDay -> reportDay,
                                    Collectors.toList())));
            //по людям в отделе
            for (Map.Entry<Employee, List<ReportDay>> employeeListEntry : reportDaysOfEmployee.entrySet()) {
                Employee employee = employeeListEntry.getKey();
                List<ReportDay> reportDays = employeeListEntry.getValue();
                Row[] rows = createRowsForReportDays(sheet, i);
                Cell cell = rows[0].createCell(0, CellType.STRING);
                fillEmployeeReport(reportDays, rows, sheet);
                cell.setCellValue(employee.getName());
                cell.setCellStyle(initBoldAndBordersStyle(sheet.getWorkbook()));
                i += NUM_OF_ROWS;
            }
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
                cell.setCellStyle(initBoldAndBordersStyle(sheet.getWorkbook()));
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
