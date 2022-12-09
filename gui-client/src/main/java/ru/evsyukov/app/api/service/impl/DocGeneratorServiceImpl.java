package ru.evsyukov.app.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import ru.evsyukov.app.api.dto.FiltersDto;
import ru.evsyukov.app.api.helpers.common.TextUtil;
import ru.evsyukov.app.api.helpers.styles.CellStyleHelper;
import ru.evsyukov.app.api.helpers.styles.CellStyleType;
import ru.evsyukov.app.api.service.DocGeneratorService;
import ru.evsyukov.app.data.entity.Employee;
import ru.evsyukov.app.data.entity.ReportDay;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.evsyukov.utils.helpers.DateTimeUtils;
import ru.evsyukov.utils.messages.Message;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocGeneratorServiceImpl implements DocGeneratorService {

    private final ReportDayRepository daysRepository;

    private final ProjectsRepository projectsRepository;

    private final EmployeeRepository employeeRepository;

    public static int NUM_OF_ROWS = 8;

    public static int DEFAULT_WIDTH_DATE_COLUMN = 20;

    public static int K_WIDTH = 200;

    private static final Logger LOGGER = LoggerFactory.getLogger(DocGeneratorService.class);


    private Map<CellStyleType, CellStyle> predefinedCellStyles;

    public DocGeneratorServiceImpl(ReportDayRepository daysRepository, ProjectsRepository projectsRepository, EmployeeRepository employeeRepository) {
        this.daysRepository = daysRepository;
        this.projectsRepository = projectsRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public ByteArrayOutputStream generateXml(FiltersDto dto) {
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
            log.info("No report days at Database");
            return new ByteArrayOutputStream();
        }
        log.info("Successfully get report days from Database {}", days);
        return createDoc(days, dto.getName(), dto.isWaitForDepartmentsReport(), dto.isWaitForEmployeeReport());
    }

    private void createDepartmentPercentReport(List<ReportDay> days, Sheet sheet) {
        List<String> projects = projectsRepository.getAllProjectsNameSorted();
        List<Row> rows = createProjectsColumn(sheet, projects, 2);
        Map<Month, CellStyle> colorMap = CellStyleHelper.predefineMonthColumnsStyle(sheet.getWorkbook());
        Map<Month, List<ReportDay>> reportMap = getExcelMonthDaysStructure(days);
        Row firstRow = sheet.createRow(0);
        Row secondRow = sheet.createRow(1);
        Row lastRow = sheet.createRow(projects.size() + 2);
        List<String> departments = getUniqueDepartments();
        AtomicInteger colNumber = new AtomicInteger(1);
        reportMap.forEach(((month, reportDays) -> {
            Map<String, Map<String, Double>> projs = mappingToDepartmentReportDays(reportDays);
            int interval = colNumber.get() + departments.size() - 1;
            for (int i = colNumber.get(); i <= interval; i++) {
                firstRow.createCell(i, CellType.STRING).setCellValue(getMonthName(month));
                Cell cell = secondRow.createCell(i, CellType.STRING);
                String shortNameDepartment = departments.get(i - colNumber.get());
                cell.setCellValue(shortNameDepartment);
                Map<String, Double> entry = projs.get(shortNameDepartment);
                sheet.setDefaultColumnStyle(i, colorMap.get(month));
                if (entry != null) {
                    for (Map.Entry<String, Double> stringLoEntry : entry.entrySet()) {
                        int rowIndex = findProjectIndex(stringLoEntry.getKey(), projects);
                        Row row = rows.get(rowIndex);
                        row.createCell(i, CellType.STRING).setCellValue(stringLoEntry.getValue());
                    }
                }
                normalizeColumn(2, projects.size(), i, sheet, lastRow);
            }
            CellRangeAddress addresses = new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), colNumber.get(), interval);
            sheet.addMergedRegion(addresses);
            colNumber.addAndGet(departments.size());
        }));

    }

    private Map<String, Map<String, Double>> mappingToDepartmentReportDays(List<ReportDay> days) {
        Map<String, List<ReportDay>> map = days.stream().collect(Collectors
                .groupingBy(rd -> rd.getEmployee().getDepartmentShort(), Collectors.toList()));

        Map<String, Map<String, Double>> projects = new HashMap<>();
        map.forEach((dep, reports) -> {
            projects.put(dep, initPercentMap(reports));
        });
        return projects;
    }

    private List<String> getUniqueDepartments() {
        return employeeRepository.findAll().stream()
                .map(Employee::getDepartmentShort)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<String, Double> initPercentMap(List<ReportDay> days) {
        int currentMonthLong = days.size();
        HashMap<String, Double> percentMap = new HashMap<>();
        days.forEach(reportDay -> {
            List<String> currentDayProjects = List.of(reportDay.getProjects().split(Message.DELIMETR));
            int currentDayProjectsCount = currentDayProjects.size();
            currentDayProjects.forEach(project -> percentMap.put(project,
                    percentMap.containsKey(project) ? (percentMap.get(project) + 100.0 / currentDayProjectsCount / currentMonthLong)
                            : 100.0 / currentDayProjectsCount / currentMonthLong));
        });
        return percentMap;
    }

    private void createEmployeePercentReport(List<ReportDay> days, Sheet sheet) {
        List<String> projects = projectsRepository.getAllProjectsNameSorted();
        List<Row> rows = createProjectsColumn(sheet, projects, 1);
        Map<Month, CellStyle> colorMap = CellStyleHelper.predefineMonthColumnsStyle(sheet.getWorkbook());
        Map<CellStyleType, CellStyle> styleMap = CellStyleHelper.predefineCellStyles(sheet.getWorkbook());
        Map<Month, List<ReportDay>> reportMap = getExcelMonthDaysStructure(days);
        Row firstMonthsRow = sheet.createRow(0);
        Row lastRow = sheet.createRow(projects.size() + 1);
        reportMap.forEach((month, reports) -> {
            CellStyle cellStyle = colorMap.get(month);
            sheet.setDefaultColumnStyle(month.getValue(), cellStyle);
            firstMonthsRow.createCell(month.getValue(), CellType.STRING)
                    .setCellValue(getMonthName(month));

            Map<String, Double> percentMap = initPercentMap(reports);

            percentMap.forEach((proj, percent) -> {
                int rowNumber = findProjectIndex(proj, projects);
                Row row = rows.get(rowNumber);
                row.createCell(month.getValue(), CellType.NUMERIC).setCellValue(percent);
            });
            normalizeColumn(1, projects.size(), month.getValue(), sheet, lastRow);
        });
    }

    private void normalizeColumn(int startRow, int projectsCount, int columnNumber, Sheet sheet, Row lastRow) {
        double checkResult = 0;
        for (int i = startRow; i < projectsCount + startRow; i++) {
            Cell cell = sheet.getRow(i).getCell(columnNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell.getCellType() == CellType.BLANK) {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(0);
            } else {
                checkResult += cell.getNumericCellValue();
            }
        }
        if (checkResult != 100) {
            log.warn("Something wrong with Excel Data.");
        }
        lastRow.createCell(columnNumber, CellType.NUMERIC).setCellValue(checkResult);
    }

    private String getMonthName(Month month) {
        return month.getDisplayName(
                TextStyle.FULL_STANDALONE, new Locale("ru"));
    }

    // находим порядковый номер строки
    private int findProjectIndex(String project, List<String> projects) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).equals(project)) {
                return i;
            }
        }
        return -1;
    }

    private List<Row> createProjectsColumn(Sheet sheet, List<String> projects, int firstRow) {
        List<Row> rows = new ArrayList<>();
        String maxStr = projects.stream().max(Comparator.comparingInt(String::length)).get();
        sheet.setColumnWidth(0, maxStr.getBytes().length * 150);
        for (int i = 0; i < projects.size(); i++) {
            Row depRow = sheet.createRow(i + firstRow);
            rows.add(depRow);
            Cell projectCell = depRow.createCell(0);
            projectCell.setCellValue(projects.get(i));
        }
        return rows;
    }

    private ByteArrayOutputStream createDoc(List<ReportDay> days, String employeeName, boolean waitForDepartmentsReport, boolean waitForEmployeeReport) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HSSFWorkbook workbook = new HSSFWorkbook();
        predefinedCellStyles = CellStyleHelper.predefineCellStyles(workbook);
        Map<Month, LocalDate> dates = new TreeMap<>(getAllDates(days));
        try {
            for (Map.Entry<Month, LocalDate> entry : dates.entrySet()) {
                Month month = entry.getKey();
                LocalDate date = entry.getValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM yyyy");
                String listName = formatter.format(date);
                Sheet sheet = workbook.createSheet(listName);
                Map<String, List<ReportDay>> reportList = getExcelStructure(days, month);
                fillList(reportList, sheet);
                normilizeColumns(sheet, date);
            }
            if (waitForEmployeeReport && employeeName != null) {
                log.info("Start generate employee percent report");
                createEmployeePercentReport(days, workbook.createSheet(
                        TextUtil.getShortName(employeeName) + "%"));
            }
            if (waitForDepartmentsReport) {
                log.info("Start generate department percent report");
                createDepartmentPercentReport(days, workbook.createSheet(
                        "По отделам, %"));
            }
            workbook.write(baos);
            workbook.close();
            baos.close();
        } catch (Exception e) {
            log.error("Fatal error when generate report", e);
        }
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

    private Row[] createRowsForReportDays(Sheet sheet, int rowNum) {
        Row[] rows = new Row[NUM_OF_ROWS];
        for (int i = 0; i < NUM_OF_ROWS; i++) {
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
                        DateTimeUtils.toLocalDate(reportDay.getDate()).getMonth().compareTo(month) == 0)
                .collect(Collectors.groupingBy(reportDay -> reportDay.getEmployee().getDepartment(),
                        Collectors.mapping(reportDay -> reportDay, Collectors.toList())));

    }

    // для одного сотрудника, разбивка его дней по месяцам (для кейса отчет по 1 сотруднику)
    private Map<Month, List<ReportDay>> getExcelMonthDaysStructure(List<ReportDay> days) {
        return new TreeMap<>(days.stream()
                .collect(Collectors
                        .groupingBy(day -> DateTimeUtils.toLocalDate(day.getDate()).getMonth(),
                                Collectors.mapping(reportDay -> reportDay, Collectors.toList()))));
    }
}
