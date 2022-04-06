package api.helpers.styles;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

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
}
