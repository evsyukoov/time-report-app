package ru.evsyukov.polling.tasks;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EnableScheduling
@Component
@Slf4j
// TODO сделать отдельный стартер, сделать возможность дампов для Postgres
public class DatabaseDump {

    private HikariDataSource dataSource;

    @Value("${dump.save-dir}")
    private String saveDir;

    private static final String COMMAND = "mysqldump";

    private static final Pattern JDBC_CONNECTION_PATTERN = Pattern.compile("jdbc:mysql://(.+):(\\d+)/(.+)\\?.*");

    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(".*_\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])\\.sql$"); //yyyy-MM-dd

    private final static DateTimeFormatter DTF_ISO = DateTimeFormatter.ISO_DATE;

    @Autowired
    public DatabaseDump(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Scheduled(cron = "${dump.cron}", zone = "GMT+3:00")
    public void makeDump() {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.HOURS.toMillis(1));

        DefaultExecutor defaultExecutor = new DefaultExecutor();
        defaultExecutor.setWatchdog(watchdog);

        String url = dataSource.getJdbcUrl();
        Matcher matcher = JDBC_CONNECTION_PATTERN.matcher(url);
        if (!matcher.matches()) {
            log.error("Проверьте URL подключения к БД");
        }
        String host = matcher.group(1);
        String port = matcher.group(2);
        String dbName = matcher.group(3);

        CommandLine commandLine = new CommandLine(COMMAND);
        commandLine.addArgument("-u" + dataSource.getUsername()); // username
        commandLine.addArgument("-p" + dataSource.getPassword()); // password
        commandLine.addArgument("-h" + host);
        commandLine.addArgument("-P" + port);
        commandLine.addArgument(dbName);
        File dir;
        String fileName;
        try {
            dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir();
            }
            fileName = formFileName(dbName);
            defaultExecutor.setStreamHandler(new PumpStreamHandler(new FileOutputStream(formSaveDirectory(fileName))));
            defaultExecutor.execute(commandLine);
            log.info("Successfully create database dump");
        } catch (Exception e) {
            log.error("Error when try to make database dump", e);
            return;
        }
        deleteOldDumps(dir, fileName);
    }

    private String formSaveDirectory(String fileName) {
        return "./" + saveDir + "/" + fileName;
    }

    private String formFileName(String dbName) {
        return dbName + "_" + LocalDate.now().format(DTF_ISO) + ".sql";
    }

    private void deleteOldDumps(File dir, String todayDump) {
        if (dir.listFiles() == null) {
            log.error("Check your dump directory");
            return;
        }
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
            if (matcher.matches() && !fileName.equals(todayDump)) {
                file.delete();
            }
        }
        log.info("Successfully delete old dumps");
    }
}
