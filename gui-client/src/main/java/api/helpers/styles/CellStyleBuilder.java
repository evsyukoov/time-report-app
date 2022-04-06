package api.helpers.styles;

import org.apache.poi.ss.usermodel.*;

public class CellStyleBuilder {

    CellStyle cellStyle;

    public static Builder builder() {
        return new CellStyleBuilder().new Builder();
    }

    public class Builder {

        public Builder initCellStyle(Workbook workbook) {
            cellStyle = workbook.createCellStyle();
            return this;
        }

        public Builder dataFormat(short i) {
            cellStyle.setDataFormat(i);
            return this;
        }

        public Builder borderLeft(BorderStyle borderStyle) {
            cellStyle.setBorderLeft(borderStyle);
            return this;
        }

        public Builder borderRight(BorderStyle borderStyle) {
            cellStyle.setBorderRight(borderStyle);
            return this;
        }

        public Builder borderTop(BorderStyle borderStyle) {
            cellStyle.setBorderTop(borderStyle);
            return this;
        }

        public Builder borderBottom(BorderStyle borderStyle) {
            cellStyle.setBorderBottom(borderStyle);
            return this;
        }

        public Builder allBorders(BorderStyle borderStyle) {
            return borderLeft(borderStyle)
                    .borderRight(borderStyle)
                    .borderTop(borderStyle)
                    .borderBottom(borderStyle);
        }

        public Builder foregroundColor(short i) {
            cellStyle.setFillForegroundColor(i);
            return this;
        }

        public Builder backgroundColor(short i) {
            cellStyle.setFillBackgroundColor(i);
            return this;
        }

        public Builder bottomBorderColor(short i) {
            cellStyle.setBottomBorderColor(i);
            return this;
        }

        public Builder fillPattern(FillPatternType type) {
            cellStyle.setFillPattern(type);
            return this;
        }

        public Builder defaultFont(Font font) {
            cellStyle.setFont(font);
            return this;
        }

        public Builder boldFont(Font font) {
            cellStyle.setFont(font);
            font.setBold(true);
            return this;
        }

        public Builder alignment(HorizontalAlignment alignment) {
            cellStyle.setAlignment(alignment);
            return this;
        }

        public Builder verticalAlignment(VerticalAlignment alignment) {
            cellStyle.setVerticalAlignment(alignment);
            return this;
        }

        public Builder hidden(boolean hidden) {
            cellStyle.setHidden(hidden);
            return this;
        }

        public Builder indention(short indention) {
            cellStyle.setIndention(indention);
            return this;
        }

        public Builder locked(boolean locked) {
            cellStyle.setLocked(locked);
            return this;
        }

        public CellStyle build() {
            return cellStyle;
        }
    }

}
