package ru.evsyukov.app.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.evsyukov.app.api.dto.input.FiltersDto;
import ru.evsyukov.app.api.helpers.common.TextUtil;
import ru.evsyukov.app.api.helpers.styles.CellStyleHelper;
import ru.evsyukov.app.api.helpers.styles.CellStyleType;
import ru.evsyukov.app.api.service.DocGeneratorService;
import ru.evsyukov.app.data.entity.Employee;
import ru.evsyukov.app.data.entity.Project;
import ru.evsyukov.app.data.entity.ReportDay;
import ru.evsyukov.app.data.repository.EmployeeRepository;
import ru.evsyukov.app.data.repository.ProjectsRepository;
import ru.evsyukov.app.data.repository.ReportDayRepository;
import ru.evsyukov.utils.helpers.DateTimeUtils;
import ru.evsyukov.utils.messages.Message;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocGeneratorServiceImpl implements DocGeneratorService {

    private final ReportDayRepository daysRepository;

    private final ProjectsRepository projectsRepository;

    private final EmployeeRepository employeeRepository;

    private int numOfRows;

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
    public ByteArrayOutputStream generateXml(FiltersDto dto) throws Exception {
        List<ReportDay> days;
        numOfRows = 8;
        // если отчет не ограничен по датам, то формирование происходит долго, будем брать за текущий год,
        // если нужны старые года, то есть фильтры
        if (dto.getDateStart() == null && dto.getDateEnd() == null) {
            Year year = Year.now();
            dto.setDateStart(DateTimeUtils.fromLocalDate(LocalDate.of(year.getValue(), Month.JANUARY, 1)));
            dto.setDateEnd(DateTimeUtils.fromLocalDate(LocalDate.of(year.getValue(), Month.DECEMBER, 31)));
        }
        if (dto.getName() == null) {
            if (dto.getDateStart() == null && dto.getDateEnd() != null) {
                days = daysRepository.findReportDayByDateLessThanEqual(dto.getDateEnd());
            } else if (dto.getDateStart() != null && dto.getDateEnd() == null) {
                days = daysRepository.findReportDayByDateGreaterThanEqual(dto.getDateStart());
            } else {
                days = daysRepository
                        .findReportDayByDateGreaterThanEqualAndDateLessThanEqual(dto.getDateStart(), dto.getDateEnd());
            }
        } else {
            if (dto.getDateStart() == null && dto.getDateEnd() != null) {
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
        if (dto.isWaitForAllProjectsReport()) {
            numOfRows = 1;
            return createEmployeeAllProjectsDoc(days);
        }
        //если сотрудник не выбран
        ExtraOptions extraOptions = new ExtraOptions(dto.isWaitForProjectReport(), dto.isWaitForEmployeeReport(), dto.isWaitForDepartmentsReport());
        //разные виды отчетов (ТЗ)
        // тут отчет по всем сотрудникам и сколько времени каждый потратил на объект
        List<ReportDay> originalDays = Collections.emptyList();
        if (extraOptions.waitForProjectReport) {
            originalDays = cloneDays(days);
            days = days.stream()
                    .filter(reportDay ->
                            reportDay.getProjects().contains(dto.getProject()))
                    .peek(reportDay -> {
                        //оставляем только 1 выбранный проект
                        String projects = reportDay.getProjects();
                        int projIndex = projects.indexOf(dto.getProject());
                        String proj = projects.substring(projIndex, projIndex + dto.getProject().length());
                        reportDay.setProjects(proj);
                    })
                    .collect(Collectors.toList());
            numOfRows = 1;
            extraOptions.remainingProject = dto.getProject();
            //тут по сути такой же отчет, но нужно оставить конкретного сотрудника и объект не меняя соотношение ко всем объектам
            // (поэтому нужно оставить для расчета все объекты, исключая объекты уже из Excel)
            //TODO рефакторинг по завершению всех хотелок
        }
        if (days.isEmpty()) {
            log.info("No report days at Database");
            return new ByteArrayOutputStream();
        }
        log.info("Successfully get report days from Database {}", days);
        return createDoc(days, originalDays, dto.getName(), extraOptions);
    }

    private List<ReportDay> cloneDays(List<ReportDay> days) throws CloneNotSupportedException {
        List<ReportDay> res = new ArrayList<>();
        for (ReportDay reportDay : days) {
            res.add((ReportDay) reportDay.clone());
        }
        return res;
    }

    // общий отчет без учета месяца
    private void createCommonProjectsPercentReport(List<ReportDay> days, Sheet sheet) {
        Map<String, List<ReportDay>> byEmployees = groupingByEmployees(days);
        List<String> employees = new ArrayList<>(byEmployees.keySet());
        List<Row> rows = createEmployeesColumn(sheet, employees, 1);
        Row firstRow = sheet.createRow(0);
        Cell nameCell = firstRow.createCell(1, CellType.STRING);
        nameCell.setCellValue("Всего");
        sheet.setDefaultColumnStyle(1, CellStyleHelper.predefineBasicColumnStyle(sheet.getWorkbook()));
        int colNumber = 1;
        for (int i = 0; i < employees.size(); i++) {
            long employeeReportDays = byEmployees.get(employees.get(i)).size();
            long allEmployeesDays = days.size();
            double percent = employeeReportDays * 100.0 / allEmployeesDays;

            Row currentRow = rows.get(i);
            Cell cell = currentRow.createCell(colNumber, CellType.NUMERIC);
            cell.setCellValue(percent);
        }
    }

    //общий отчет по месяцам
    private void createProjectsPercentReport(List<ReportDay> days, Sheet sheet) {
        // группируем сотрудник - отчетные дни
        Map<String, List<ReportDay>> byEmployees = groupingByEmployees(days);
        List<String> employees = new ArrayList<>(byEmployees.keySet());
        List<Row> rows = createEmployeesColumn(sheet, employees, 1);
        Map<Month, CellStyle> colorMap = CellStyleHelper.predefineMonthColumnsStyle(sheet.getWorkbook());
        Row firstRow = sheet.createRow(0);
        int colNumber = 1;
        Set<Month> months = findAllMonths(days);
        //идем по сотрудникам
        for (Month month : months) {
            sheet.setDefaultColumnStyle(colNumber, colorMap.get(month));
            Cell monthCell = firstRow.createCell(colNumber, CellType.STRING);
            monthCell.setCellValue(getMonthName(month));
            for (int i = 0; i < employees.size(); i++) {
                long employeeReportDays = byEmployees.get(employees.get(i)).stream()
                        .filter(rd -> DateTimeUtils.toLocalDate(rd.getDate()).getMonth() == month).count();
                long allEmployeesDays = days.stream().filter(rd -> DateTimeUtils.toLocalDate(rd.getDate()).getMonth() == month).count();
                double percent = employeeReportDays * 100.0 / allEmployeesDays;

                Row currentRow = rows.get(i);
                Cell cell = currentRow.createCell(colNumber, CellType.NUMERIC);
                cell.setCellValue(percent);
            }
            colNumber++;
        }
    }

    private void createDepartmentPercentReport(List<ReportDay> days, Sheet sheet) {
        List<String> projects = projectsRepository.getAllProjectsSorted()
                .stream()
                .map(Project::getProjectName)
                .collect(Collectors.toList());

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
        List<String> projects = projectsRepository.getAllProjectsSorted()
                .stream()
                .map(Project::getProjectName)
                .collect(Collectors.toList());
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

    private void createEmployeeAllProjectsPercentReport(Map<String, List<ReportDay>> departmentDays, Sheet sheet, Month month) throws CloneNotSupportedException {
        List<String> allMonthsDays = departmentDays.values().stream().flatMap(Collection::stream)
                .filter(d -> DateTimeUtils.toLocalDate(d.getDate()).getMonth() == month)
                .map(ReportDay::getProjects).map(proj -> proj.split(Message.DELIMETR))
                .flatMap(Arrays::stream)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        fillHeader(sheet, allMonthsDays);
        //по отделам
        int row = 1;
        for (Map.Entry<String, List<ReportDay>> entry : departmentDays.entrySet()) {
            String department = entry.getKey();
            List<ReportDay> days = entry.getValue();
            Row depRow = sheet.createRow(row++);
            fillDepartmentRow(depRow, allMonthsDays.size(), department);

            Map<Employee, Set<ReportDay>> reportDaysOfEmployee = days.stream().collect(
                    Collectors.groupingBy(ReportDay::getEmployee,
                            Collectors.mapping(reportDay -> reportDay,
                                    Collectors.toSet())));

            //находим сотрудника который на этом проекте работал
            for (Map.Entry<Employee, Set<ReportDay>> employeeListEntry : reportDaysOfEmployee.entrySet()) {
                Employee employee = employeeListEntry.getKey();
                Row currentRow = sheet.createRow(row);
                Cell emplCell = currentRow.createCell(0, CellType.STRING);
                emplCell.setCellValue(employee.getName());
                emplCell.setCellStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));

                for (String project : allMonthsDays) {
                    Map<String, Double> map = initPercentMap(employeeListEntry.getValue().stream().sorted(Comparator.comparing(ReportDay::getDate)).collect(Collectors.toList()));

                    Cell cell = currentRow.createCell(1 + allMonthsDays.indexOf(project), CellType.NUMERIC);
                    cell.setCellValue(map.get(project) == null ? 0 : map.get(project));
                    cell.setCellStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));
                }
                row++;
            }
        }
    }

    //отчет в разрезе сотрудник - процент потраченный сотрудником на этот объект / время потраченное этим сотрудником на все объекты
    private void createEmployeeProjectPercentReport(List<ReportDay> days, Sheet sheet, String project) {
        Map<String, List<ReportDay>> byEmployees = groupingByEmployees(days);
        List<String> employees = new ArrayList<>(byEmployees.keySet());
        List<Row> rows = createEmployeesColumn(sheet, employees, 1);
        Map<Month, CellStyle> colorMap = CellStyleHelper.predefineMonthColumnsStyle(sheet.getWorkbook());
        Row firstRow = sheet.createRow(0);
        int colNumber = 1;
        Set<Month> months = findAllMonths(days);
        //идем по сотрудникам
        for (Month month : months) {
            sheet.setDefaultColumnStyle(colNumber, colorMap.get(month));
            Cell monthCell = firstRow.createCell(colNumber, CellType.STRING);
            monthCell.setCellValue(getMonthName(month));
            List<ReportDay> currentMonthDays = days.stream().filter(day -> DateTimeUtils.toLocalDate(day.getDate()).getMonth() == month).collect(Collectors.toList());
            for (int i = 0; i < employees.size(); i++) {
                String employee = employees.get(i);
                Map<String, Double> map = initPercentMap(currentMonthDays.stream().filter(day -> day.getEmployee().getName().equals(employee)).collect(Collectors.toList()));

                Row currentRow = rows.get(i);
                Cell cell = currentRow.createCell(colNumber, CellType.NUMERIC);
                cell.setCellValue(map.get(project) == null ? 0 : map.get(project));
            }
            colNumber++;
        }
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
        lastRow.createCell(columnNumber, CellType.NUMERIC).setCellValue(checkResult);
    }

    private void normalizeColumn(int projectsCount, int columnNumber, Sheet sheet) {
        for (int i = 1; i < projectsCount + 1; i++) {
            Cell cell = sheet.getRow(i).getCell(columnNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell.getCellType() == CellType.BLANK) {
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(0);
            }
        }
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

    private List<Row> createEmployeesColumn(Sheet sheet, List<String> employees, int firstRow) {
        List<Row> rows = new ArrayList<>();
        String maxStr = employees.stream().max(Comparator.comparingInt(String::length)).get();
        sheet.setColumnWidth(0, maxStr.getBytes().length * 150);
        for (int i = 0; i < employees.size(); i++) {
            Row depRow = sheet.createRow(i + firstRow);
            rows.add(depRow);
            Cell projectCell = depRow.createCell(0);
            projectCell.setCellValue(employees.get(i));
        }
        return rows;
    }

    private ByteArrayOutputStream createEmployeeAllProjectsDoc(List<ReportDay> days) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HSSFWorkbook workbook = new HSSFWorkbook();
        predefinedCellStyles = CellStyleHelper.predefineCellStyles(workbook);
        Map<Integer, List<ReportDay>> datesByYear = new TreeMap<>(groupingByYear(days));
        for (Map.Entry<Integer, List<ReportDay>> oneYearDaysEntry : datesByYear.entrySet()) {
            Map<Month, LocalDate> dates = new TreeMap<>(getAllDates(oneYearDaysEntry.getValue()));
            for (Map.Entry<Month, LocalDate> entry : dates.entrySet()) {
                Month month = entry.getKey();
                LocalDate date = entry.getValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM yyyy");
                String listName = formatter.format(date);
                Sheet sheet = workbook.createSheet(listName);
                Map<String, List<ReportDay>> reportList = getExcelStructure(oneYearDaysEntry.getValue(), month);
                createEmployeeAllProjectsPercentReport(reportList, sheet, month);
                normilizeColumns(sheet);
            }
        }
        workbook.write(baos);
        workbook.close();
        baos.close();
        return baos;
    }

    private ByteArrayOutputStream createDoc(List<ReportDay> days, List<ReportDay> originalDays, String employeeName, ExtraOptions extraOptions) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HSSFWorkbook workbook = new HSSFWorkbook();
        predefinedCellStyles = CellStyleHelper.predefineCellStyles(workbook);
        Map<Integer, List<ReportDay>> datesByYear = new TreeMap<>(groupingByYear(days));
        Map<Integer, List<ReportDay>> datesByYearOriginal = new TreeMap<>(groupingByYear(originalDays));
        for (Map.Entry<Integer, List<ReportDay>> oneYearDaysEntry : datesByYear.entrySet()) {
            Map<Month, LocalDate> dates = new TreeMap<>(getAllDates(oneYearDaysEntry.getValue()));
            for (Map.Entry<Month, LocalDate> entry : dates.entrySet()) {
                Month month = entry.getKey();
                LocalDate date = entry.getValue();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM yyyy");
                String listName = formatter.format(date);
                Sheet sheet = workbook.createSheet(listName);
                Map<String, List<ReportDay>> reportList = getExcelStructure(oneYearDaysEntry.getValue(), month);
                fillList(reportList, sheet);
                normilizeColumns(sheet);
            }
            if (extraOptions.isWaitForEmployeeReport() && employeeName != null) {
                log.info("Start generate employee percent report");
                createEmployeePercentReport(oneYearDaysEntry.getValue(), workbook.createSheet(
                        TextUtil.getShortName(employeeName) + "%, " + oneYearDaysEntry.getKey() + " год"));
            }
            if (extraOptions.isWaitForDepartmentsReport()) {
                log.info("Start generate department percent report");
                createDepartmentPercentReport(oneYearDaysEntry.getValue(), workbook.createSheet(
                        String.format("По отделам, %%, %s год", oneYearDaysEntry.getKey())));
            }
            if (extraOptions.isWaitForProjectReport()) {
                log.info("Start generate projects percent report");
                createProjectsPercentReport(oneYearDaysEntry.getValue(), workbook.createSheet(
                        String.format("По людям, %% проект, %s год", oneYearDaysEntry.getKey())));
                createCommonProjectsPercentReport(oneYearDaysEntry.getValue(), workbook.createSheet(
                        String.format("По людям, %% проект(всего), %s год", oneYearDaysEntry.getKey())));
                //нужно соотношение ко всем объектам сотрудника
                createEmployeeProjectPercentReport(datesByYearOriginal.get(oneYearDaysEntry.getKey()),
                        workbook.createSheet(String.format("По людям, %% от всех, %s год", oneYearDaysEntry.getKey())),
                                extraOptions.remainingProject);
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

    private void fillHeader(Sheet sheet, List<String> uniqueMonthProjects) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("Фамилия");
        cell.setCellStyle(predefinedCellStyles.get(CellStyleType.PERCENT_PROJECT));

        int i = 1;
        for (String project : uniqueMonthProjects) {
            Cell projectHeaderCell = row.createCell(i++, CellType.STRING);
            projectHeaderCell.setCellStyle(predefinedCellStyles.get(CellStyleType.PERCENT_PROJECT));
            projectHeaderCell.setCellValue(project);
        }
    }

    private void normilizeColumns(Sheet sheet) {
        int lastRow = sheet.getLastRowNum();
        int column = -1;
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
        }
    }

    private void addBorders(CellStyle cellStyle) {
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
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

    private void fillDepartmentRow(Row depRow, int rowLen, String department) {
        Cell depCell = depRow.createCell(0);
        depCell.setCellValue(String.format("Отдел: %s", department));
        depCell.setCellStyle(predefinedCellStyles.get(CellStyleType.DEPARTMENT_ROW));
        for (int i = 1; i <= rowLen; i++) {
            Cell emptyCell = depRow.createCell(i);
            emptyCell.setCellValue("");
            emptyCell.setCellStyle(predefinedCellStyles.get(CellStyleType.DEPARTMENT_ROW));
        }
    }

    private void fillList(Map<String, List<ReportDay>> reportList, Sheet sheet) {
        fillHeader(reportList, sheet);
        //по отделам
        int i = 1;
        for (Map.Entry<String, List<ReportDay>> entry : reportList.entrySet()) {
            String department = entry.getKey();
            List<ReportDay> days = entry.getValue();
            LocalDate monthDate = DateTimeUtils.toLocalDate(days.get(0).getDate());
            Row depRow = sheet.createRow(i++);
            fillDepartmentRow(depRow, monthDate.lengthOfMonth(), department);

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
                fillEmployeeReport(reportDays, rows, monthDate);
                cell.setCellValue(employee.getName());
                cell.setCellStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));
                i += numOfRows;
            }
        }
    }

    private Row[] createRowsForReportDays(Sheet sheet, int rowNum) {
        Row[] rows = new Row[numOfRows];
        for (int i = 0; i < numOfRows; i++) {
            rows[i] = sheet.createRow(rowNum++);
        }
        return rows;
    }

    private void fillCell(Cell cell, Date date, String value) {
        cell.setCellValue(value);
        if (DateTimeUtils.isWeekend(date)) {
            cell.setCellStyle(predefinedCellStyles.get(CellStyleType.WEEKEND_COLUMN));
        } else  {
            cell.setCellStyle(predefinedCellStyles.get(CellStyleType.PROJECT_CELL));
        }
    }

    private void fillCell(Cell cell, Date date, String value, int rowNum) {
        if (rowNum == 0) {
            if (DateTimeUtils.isWeekend(date)) {
                cell.setCellStyle(predefinedCellStyles.get(CellStyleType.WEEKEND_COLUMN));
            } else {
                cell.setCellStyle(predefinedCellStyles.get(CellStyleType.MAIN_PROJECT_ROW));
            }
            cell.setCellValue(value);
        } else {
            fillCell(cell, date, value);
        }
    }

    private void fillCell(Cell cell, LocalDate date, String value, int rowNum) {
        fillCell(cell, DateTimeUtils.fromLocalDate(date), value, rowNum);
    }

    private void fillCell(Cell cell, LocalDate date, String value) {
        fillCell(cell, DateTimeUtils.fromLocalDate(date), value);
    }

    private void fillEmployeeReport(List<ReportDay> reportDays, Row[] rows, LocalDate monthDate) {
        int monthLen = monthDate.lengthOfMonth();
        for (int day = 1; day <= monthLen; day++) {
            final int dayOfMonth = day;
            ReportDay reportDay = reportDays.stream()
                    .filter(rd -> DateTimeUtils.toLocalDate(rd.getDate()).getDayOfMonth() == dayOfMonth)
                    .findFirst()
                    .orElse(null);
            if (reportDay != null) {
                if (Objects.isNull(reportDay.getProjects())) {
                    continue;
                }
                String[] projects = reportDay.getProjects().split(Message.DELIMETR);
                for (int i = 0; i < numOfRows; i++) {
                    if (i >= projects.length) {
                        Cell cell = rows[i].createCell(dayOfMonth, CellType.STRING);
                        fillCell(cell, reportDay.getDate(), "");
                        continue;
                    }
                    Cell cell = rows[i].createCell(dayOfMonth, CellType.STRING);
                    fillCell(cell, reportDay.getDate(), projects[i], i);
                }
            } else {
                for (int i = 0; i < numOfRows; i++) {
                    Cell cell = rows[i].createCell(dayOfMonth, CellType.STRING);
                    fillCell(cell, LocalDate.of(monthDate.getYear(), monthDate.getMonth(), day), "", i);
                }
            }
        }
    }

    private Map<Integer, List<ReportDay>> groupingByYear(List<ReportDay> days) {
        return days.stream()
                .collect(Collectors.groupingBy(reportDay -> DateTimeUtils.toLocalDate(reportDay.getDate()).getYear(),
                        Collectors.mapping(reportDay -> reportDay, Collectors.toList())));
    }

    private Set<Month> findAllMonths(List<ReportDay> days) {
        Set<Month> months = new TreeSet<>(Enum::compareTo);
        months.addAll(days.stream().map(day -> DateTimeUtils.toLocalDate(day.getDate()).getMonth()).collect(Collectors.toSet()));
        return months;
    }

    //сотрудник - список отчетных дней по месяцам
    private Map<String, List<ReportDay>> groupingByEmployees(List<ReportDay> days) {
        final Map<String, List<ReportDay>> res = new TreeMap<>(String::compareTo);
        res.putAll(days.stream()
                .collect(Collectors.groupingBy(reportDay -> reportDay.getEmployee().getName(),
                        Collectors.mapping(reportDay -> reportDay, Collectors.toList()))));
        return res;
    }

    private Map<Month, LocalDate> getAllDates(List<ReportDay> days) {
        return days.stream().map(ReportDay::getDate)
                .map(DateTimeUtils::toLocalDate)
                .collect(Collectors
                        .toMap(LocalDate::getMonth, localDate -> localDate,
                                (k1, k2) -> k1.isBefore(k2) ? k1 : k2));
    }

    private Set<String> getCurrentMonthProjects(List<ReportDay> days, Month month) {
        return days.stream().filter(d -> DateTimeUtils.toLocalDate(d.getDate()).getMonth() == month)
                .map(ReportDay::getProjects).map(proj -> proj.split(Message.DELIMETR))
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
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

    private static class ExtraOptions {
        boolean waitForEmployeeReport;
        boolean waitForDepartmentsReport;
        boolean waitForProjectReport;
        boolean removeProjects;
        String remainingProject;

        public ExtraOptions(boolean waitForProjectReport,
                            boolean waitForEmployeeReport,
                            boolean waitForDepartmentsReport) {
            this.waitForEmployeeReport = waitForEmployeeReport;
            this.waitForDepartmentsReport = waitForDepartmentsReport;
            this.waitForProjectReport = waitForProjectReport;
        }

        public boolean isWaitForEmployeeReport() {
            return waitForEmployeeReport;
        }

        public boolean isWaitForDepartmentsReport() {
            return waitForDepartmentsReport;
        }

        public boolean isWaitForProjectReport() {
            return waitForProjectReport;
        }

        public boolean isRemoveProjects() {
            return removeProjects;
        }

        public String getRemainingProject() {
            return remainingProject;
        }
    }
}
