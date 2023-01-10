package com.sepehrnet.settlement.task;

import com.ibm.icu.util.Calendar;
import com.sepehrnet.settlement.init.StartUpInit;
import com.sepehrnet.settlement.utility.CalendarUtils;
import com.sepehrnet.settlement.utility.FTPUtils;
import com.sepehrnet.settlement.utility.GlobalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class ReadNasimOutFile {

    @Value("${cfg.bank.name}")
    private String bankName;
    @Value("${cfg.file.path}")
    private String filePath;
    @Value("${cfg.last.date.out.file.name}")
    private String lastDateOutFileName;
    @Value("${cfg.ftp.nasim.host.out.path}")
    private String nasimHostOutDirectory;
    @Value("${cfg.ftp.isc.nasim.out.host.path}")
    private String nasimOutDirectoryBackup;

    private final StartUpInit startUpInit;

    public ReadNasimOutFile(StartUpInit startUpInit) {
        this.startUpInit = startUpInit;
    }

    @Scheduled(cron = "${cfg.read.nasim.out.file.cron.job}")
    public void process() {
        log.info("Read nasim out file task is running... ");

        // 1: Create Last Date File
        Path lastDateFilePath = createLastDateFile();

        // 2: Read content from last date file
        String lastPersianDate = readLastDateFile(lastDateFilePath);
        if (lastPersianDate.trim().length() == 0) {
            log.warn("Not found content in last date file.");
            return;
        }

        String currentPersianDate = getPersianDateBeforeCurrentDate(1);

        // 3: Check date between last date and current date for read all files
        if (Long.parseLong(lastPersianDate) < Long.parseLong(currentPersianDate)) {
            log.info("Start calculate dates between two date.");
            Date startDate = CalendarUtils.convertPersianStringToDate(lastPersianDate);
            Date endDate = CalendarUtils.convertPersianStringToDate(currentPersianDate);
            List<Date> dateList = GlobalUtils.datesBetweenTwoDate(startDate, endDate);

            dateList.forEach(date -> {
                boolean doneNasimFTP = false;
                boolean doneISCFTP = false;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String currentDate = CalendarUtils.getCurrentPersianDateTime(GlobalUtils.CALENDAR_DATE_FLAT_PATTERN, calendar);
                String currentDirectory = filePath + currentDate;
                GlobalUtils.createDirectory(currentDirectory);

                String nasimDirectory = currentDirectory + "/nasim/out/";
                GlobalUtils.createDirectory(nasimDirectory);
                String nasimFileName = "";
                String nasimFileFullPath = "";

                log.info("Try connect to Nasim FTP.");
                FTPUtils nasimFTP = new FTPUtils();
                if (nasimFTP.connect((String) startUpInit.cache.get("nasim.ip"), (Integer) startUpInit.cache.get("nasim.port"))) {
                    if (nasimFTP.login((String) startUpInit.cache.get("nasim.username"), (String) startUpInit.cache.get("nasim.password"))) {

                        String returnFileName = nasimFTP.getFileNameWithPattern(nasimHostOutDirectory, filterFileName(date));
                        if (returnFileName != null) {
                            nasimFileName = returnFileName;
                            nasimFileFullPath = nasimDirectory + nasimFileName;
                            if (nasimFTP.downloadFile(nasimHostOutDirectory + nasimFileName, nasimFileFullPath)) {
                                doneNasimFTP = true;
                            }
                        }

                    }
                    nasimFTP.disconnect();
                }


                if (doneNasimFTP) {

                    log.info("Try connect to ISC FTP.");
                    FTPUtils iscFTP = new FTPUtils();
                    if (iscFTP.connect((String) startUpInit.cache.get("isc.ip"), (Integer) startUpInit.cache.get("isc.port"))) {
                        if (iscFTP.login((String) startUpInit.cache.get("isc.username"), (String) startUpInit.cache.get("isc.password"))) {
                            if (iscFTP.uploadFile(nasimFileFullPath, nasimFileName, nasimOutDirectoryBackup)) {
                                doneISCFTP = true;
                            }
                        }
                        iscFTP.disconnect();
                    }

                }

                if (doneNasimFTP && doneISCFTP) {
                    GlobalUtils.writeToFile(filePath, lastDateOutFileName, currentDate);
                }

            });

        }
    }

    private Path createLastDateFile() {
        String fullPath = filePath + lastDateOutFileName;
        // For the first time (if file not exist create and write in the file.)
        if (!GlobalUtils.checkFileExist(filePath, lastDateOutFileName)) {
            log.info("File {} not exist, create for the first time.", fullPath);
            String content = getPersianDateBeforeCurrentDate(2);
            GlobalUtils.writeToFile(filePath, lastDateOutFileName, content);
            return Paths.get(fullPath);
        }
        return Paths.get(fullPath);
    }

    private String readLastDateFile(Path lastDateFilePath) {
        try (Stream<String> lines = GlobalUtils.readFile(lastDateFilePath.toString())) {
            List<String> collect = lines.collect(Collectors.toList());
            log.info("Last persian date: {}", collect.get(0));
            return collect.get(0);
        } catch (IOException e) {
            log.error("Exception occurred to read file {}.", lastDateFilePath);
            e.printStackTrace();
        }
        return "";
    }

    private String getPersianDateBeforeCurrentDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -day);
        String persianDate = CalendarUtils.getCurrentPersianDateTime(GlobalUtils.CALENDAR_DATE_FLAT_PATTERN, calendar);
        log.info("{} days before current date is: {}", day, persianDate);
        return persianDate;
    }

    private String fileDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return CalendarUtils.getCurrentPersianDateTime(GlobalUtils.CALENDAR_DATE_FLAT_PATTERN, calendar);
    }

    private String filterFileName(Date date) {
        String dateFile = fileDate(date);
        return bankName + "_" + dateFile + "_";
    }


}
