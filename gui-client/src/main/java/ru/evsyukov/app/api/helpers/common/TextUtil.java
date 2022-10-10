package ru.evsyukov.app.api.helpers.common;

public class TextUtil {

    public static String getShortName(String fullName) {
        String[] fio = fullName.split("\\s+");
        if (fio.length > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(fio[0]).append(" ");
            for (int i = 1; i < fio.length; i++) {
                sb.append(fio[i].charAt(0)).append(".");
            }
            return sb.toString();
        }
        return fullName;
    }
}
