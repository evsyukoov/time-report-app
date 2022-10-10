package ru.evsyukov.app.api.helpers.styles;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.time.Month;
import java.util.EnumMap;
import java.util.Map;

public class CellStyleHelper {

    public static Map<CellStyleType, CellStyle> predefineCellStyles(Workbook workbook) {

        Map<CellStyleType, CellStyle> map = new EnumMap<>(CellStyleType.class);
        map.put(CellStyleType.SURNAME, CellStyleBuilder.builder()
                .initCellStyle(workbook)
                .foregroundColor(HSSFColor.HSSFColorPredefined.CORAL.getIndex())
                .fillPattern(FillPatternType.SOLID_FOREGROUND)
                .allBorders(BorderStyle.THIN)
                .build());

        DataFormat df = workbook.createDataFormat();
        map.put(CellStyleType.DATE, CellStyleBuilder.builder()
                .initCellStyle(workbook)
                .fillPattern(FillPatternType.SOLID_FOREGROUND)
                .foregroundColor(HSSFColor.HSSFColorPredefined.CORAL.getIndex())
                .dataFormat(df.getFormat("yyyy-MM-dd"))
                .build());

        map.put(CellStyleType.DEFAULT_COLUMN, CellStyleBuilder.builder()
                .initCellStyle(workbook)
                .allBorders(BorderStyle.THIN)
                .build());

        map.put(CellStyleType.WEEKEND_COLUMN, CellStyleBuilder.builder().initCellStyle(workbook)
                .allBorders(BorderStyle.THIN)
                .fillPattern(FillPatternType.SOLID_FOREGROUND)
                .foregroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex())
                .build());

        map.put(CellStyleType.DEPARTMENT_ROW, CellStyleBuilder.builder()
                .initCellStyle(workbook)
                .foregroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex())
                .fillPattern(FillPatternType.SOLID_FOREGROUND)
                .allBorders(BorderStyle.THIN)
                .boldFont(workbook.createFont())
                .build());

        map.put(CellStyleType.MAIN_PROJECT_ROW, CellStyleBuilder.builder()
                .initCellStyle(workbook)
                .foregroundColor(HSSFColor.HSSFColorPredefined.AQUA.getIndex())
                .fillPattern(FillPatternType.SOLID_FOREGROUND)
                .allBorders(BorderStyle.THIN)
                .boldFont(workbook.createFont())
                .build());

        map.put(CellStyleType.PROJECT_CELL, CellStyleBuilder.builder()
                .initCellStyle(workbook)
                .allBorders(BorderStyle.THIN)
                .build());

        return map;
    }

    public static Map<Month, CellStyle> predefineMonthColumnsStyle(Workbook workbook) {
        Map<Month, CellStyle> map = new EnumMap<>(Month.class);

        map.put(Month.JANUARY, initCellStyleColumn(HSSFColor.HSSFColorPredefined.AQUA, workbook));
        map.put(Month.FEBRUARY, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIGHT_BLUE, workbook));
        map.put(Month.MARCH, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE, workbook));
        map.put(Month.APRIL, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE, workbook));
        map.put(Month.MAY, initCellStyleColumn(HSSFColor.HSSFColorPredefined.PINK, workbook));
        map.put(Month.JUNE, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW, workbook));
        map.put(Month.JULY, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIGHT_GREEN, workbook));
        map.put(Month.AUGUST, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LAVENDER, workbook));
        map.put(Month.SEPTEMBER, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE, workbook));
        map.put(Month.OCTOBER, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIGHT_ORANGE, workbook));
        map.put(Month.NOVEMBER, initCellStyleColumn(HSSFColor.HSSFColorPredefined.SEA_GREEN, workbook));
        map.put(Month.DECEMBER, initCellStyleColumn(HSSFColor.HSSFColorPredefined.LIME, workbook));
        return map;
    }
    
    private static CellStyle initCellStyleColumn(HSSFColor.HSSFColorPredefined color, Workbook workbook) {
        return CellStyleBuilder.builder()
                .initCellStyle(workbook)
                .foregroundColor(color.getIndex())
                .fillPattern(FillPatternType.SOLID_FOREGROUND)
                .allBorders(BorderStyle.THIN)
                .boldFont(workbook.createFont())
                .build();
    }
}
