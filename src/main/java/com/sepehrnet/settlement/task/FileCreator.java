package com.sepehrnet.settlement.task;

import com.ibm.icu.util.Calendar;
import com.sepehrnet.settlement.enums.ProjectNameEnum;
import com.sepehrnet.settlement.init.StartUpInit;
import com.sepehrnet.settlement.utility.CalendarUtils;
import com.sepehrnet.settlement.utility.FTPUtils;
import com.sepehrnet.settlement.utility.GlobalUtils;
import com.sepehrnet.settlement.utility.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.partitioningBy;

@Component
@Slf4j
public class FileCreator {

    @Value("${cfg.bank.name}")
    private String bankName;
    @Value("${cfg.bank.code}")
    private String bankCode;
    @Value("${cfg.file.path}")
    private String filePath;
    @Value("${cfg.isc.start.file.name}")
    private String iscStartFileName;
    @Value("${cfg.isc.file.postfix}")
    private String iscFilePostfix;
    @Value("${cfg.isc.nor.file.postfix}")
    private String iscNorFilePostfix;
    @Value("${cfg.nasim.file.postfix}")
    private String nasimFilePostfix;
    @Value("${cfg.last.date.and.index.file.name}")
    private String lastDateFileName;
    @Value("${cfg.utility.code}")
    private String utilityCode;
    @Value("${cfg.day.file.postfix}")
    private String dayFilePostfix;
    @Value("${cfg.day.bank.name}")
    private String dayBankName;
    @Value("${cfg.ftp.nasim.host.path}")
    private String nasimHostDirectory;
    @Value("${cfg.ftp.gas.host.path}")
    private String gasHostDirectory;
    @Value("${cfg.ftp.day.host.path}")
    private String dayHostDirectory;
    @Value("${cfg.ftp.isc.host.path}")
    private String iscHostDirectoryForNasimBackup;
    @Value("${cfg.last.gas.index}")
    private int lastGasIndex;

    private final StartUpInit startUpInit;

    public FileCreator(StartUpInit startUpInit) {
        this.startUpInit = startUpInit;
    }

    @Scheduled(cron = "${cfg.read.file.cron.job}")
    public void process() {
        log.info("FileCreator task is running... ");

        // 1: Create Last Date File
        Path lastDateFilePath = createLastDateAndLastIndexFile();

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

                boolean doneISCFTP = false;
                boolean doneDayFTP = false;
                boolean doneNasimFTP = false;
                AtomicBoolean doneGasFTP = new AtomicBoolean(false);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String currentDate = CalendarUtils.getCurrentPersianDateTime(GlobalUtils.CALENDAR_DATE_FLAT_PATTERN, calendar);
                String currentDirectory = filePath + currentDate;
                GlobalUtils.createDirectory(currentDirectory);

                String iscCurrentFile = iscStartFileName + currentDate + iscFilePostfix;
                String iscDirectory = currentDirectory + "/isc/";
                String iscZipFileFullPath = iscDirectory + iscCurrentFile;
                GlobalUtils.createDirectory(iscDirectory);


                log.info("Try connect to ISC FTP.");
                FTPUtils iscFTP = new FTPUtils();
                if (iscFTP.connect((String) startUpInit.cache.get("isc.ip"), (Integer) startUpInit.cache.get("isc.port"))) {
                    if (iscFTP.login((String) startUpInit.cache.get("isc.username"), (String) startUpInit.cache.get("isc.password"))) {
                        //if (iscFTP.checkFileExist("/", iscCurrentFile)) {
                        if (iscFTP.downloadFile("/" + iscCurrentFile, iscZipFileFullPath)) {
                            doneISCFTP = true;
                        }
                        //}
                    }
                    iscFTP.disconnect();
                }

                if (doneISCFTP) {
                    if (canConnectToFTPNasim() && canConnectToFTPGas()) {
                        log.info("Try to extract NOOR bill file from ISC zip file.");
                        String iscDate = currentDate.substring(2);
                        Path source = Paths.get(iscZipFileFullPath);
                        Path target = Paths.get(iscDirectory);
                        ZipUtils.extractFile(source, target, createISCFileName(iscDate));

                        String iscTxtFileFullPath = iscDirectory + createISCFileName(iscDate);
                        log.info("Start to read NOOR bill file and separate to multiple files.");
                        try (Stream<String> inputStream = GlobalUtils.readFile(iscTxtFileFullPath)) {
                            log.info("Reading ISC file: {}", iscTxtFileFullPath);
                            if (inputStream != null) {
                                //skip line 1
                                //filter by service type ==> Type=3 (Gas)
                                Map<Boolean, List<String>> filterByTypeBooleanListMap = inputStream
                                        .skip(1)
                                        .collect(partitioningBy(s -> s.charAt(25) == utilityCode.charAt(0)));

                                List<String> gasBillList = filterByTypeBooleanListMap.get(true);
                                List<String> withoutGasBillList = filterByTypeBooleanListMap.get(false);

                                String nasimDirectory = currentDirectory + "/nasim/";
                                String nasimFileName = createNasimFileName(date);
                                String nasimFileFullPath = nasimDirectory + nasimFileName;

                                GlobalUtils.writeToFile(nasimDirectory, nasimFileName, createSettlementData(gasBillList, "", ProjectNameEnum.NASIM.getValue(), date));

                                log.info("Try connect to Nasim FTP.");
                                FTPUtils nasimFTP = new FTPUtils();
                                if (nasimFTP.connect((String) startUpInit.cache.get("nasim.ip"), (Integer) startUpInit.cache.get("nasim.port"))) {
                                    if (nasimFTP.login((String) startUpInit.cache.get("nasim.username"), (String) startUpInit.cache.get("nasim.password"))) {
                                        if (nasimFTP.uploadFile(nasimFileFullPath, nasimFileName, nasimHostDirectory)) {
                                            doneNasimFTP = true;
                                        }
                                    }
                                    nasimFTP.disconnect();
                                }

                                //Group by company code
                                Map<String, List<String>> gasMap = gasBillList.stream()
                                        .collect(
                                                Collectors.groupingBy(s -> s.substring(22, 25),
                                                        Collectors.toList())
                                        );

                                log.info("Try connect to Gas FTP.");
                                FTPUtils gasFTP = new FTPUtils();
                                if (gasFTP.connect((String) startUpInit.cache.get("gas.ip"), (Integer) startUpInit.cache.get("gas.port"))) {
                                    if (gasFTP.login((String) startUpInit.cache.get("gas.username"), (String) startUpInit.cache.get("gas.password"))) {

                                        int index = getGasLastIndex(lastDateFilePath);
                                        gasMap.forEach((key, value) -> {

                                            String gasDirectory = currentDirectory + "/gas/" + key + "/";
                                            String gasFileName = createGasFileName(index, key, date);
                                            String gasFileFullPath = gasDirectory + gasFileName;

                                            GlobalUtils.writeToFile(gasDirectory, gasFileName, createSettlementData(value, key, ProjectNameEnum.GAS.getValue(), date));

                                            String hostDir = gasHostDirectory + key + "/";
                                            if (gasFTP.uploadFile(gasFileFullPath, gasFileName, hostDir)) {
                                                doneGasFTP.set(true);
                                            }
                                        });
                                    }
                                    gasFTP.disconnect();
                                }

                                // Day bank file without gas bills
                                String dayDirectory = currentDirectory + "/day_bank/";
                                String dayFileName = createDayBankFileName(date);
                                String dayFileFullPath = dayDirectory + dayFileName;
                                GlobalUtils.writeToFile(dayDirectory, dayFileName, createSettlementData(withoutGasBillList, "", ProjectNameEnum.DAY.getValue(), date));

                                log.info("Try connect to DAY Bank FTP.");
                                FTPUtils dayFTP = new FTPUtils();
                                if (dayFTP.connect((String) startUpInit.cache.get("isc.ip"), (Integer) startUpInit.cache.get("isc.port"))) {
                                    if (dayFTP.login((String) startUpInit.cache.get("isc.username"), (String) startUpInit.cache.get("isc.password"))) {
                                        if (dayFTP.uploadFile(dayFileFullPath, dayFileName, dayHostDirectory)) {
                                            dayFTP.uploadFile(nasimFileFullPath, nasimFileName, iscHostDirectoryForNasimBackup);
                                            doneDayFTP = true;
                                        }
                                    }
                                    dayFTP.disconnect();
                                }

                            }
                        } catch (IOException e) {
                            log.error("Exception occurred to read file {}.", iscTxtFileFullPath);
                            e.printStackTrace();
                        }

                        if (doneNasimFTP && doneGasFTP.get() && doneDayFTP) {
                            String newContent = currentDate + "," + getGasLastIndex(lastDateFilePath);
                            GlobalUtils.writeToFile(filePath, lastDateFileName, newContent);
                        }
                    }
                }
            });
        }
    }

    private Path createLastDateAndLastIndexFile() {
        String fullPath = filePath + lastDateFileName;
        // For the first time (if file not exist create and write in the file.)
        if (!GlobalUtils.checkFileExist(filePath, lastDateFileName)) {
            log.info("File {} not exist, create for the first time.", fullPath);
            String content = getPersianDateBeforeCurrentDate(2) +
                    "," +
                    lastGasIndex;
            GlobalUtils.writeToFile(filePath, lastDateFileName, content);
            return Paths.get(fullPath);
        }
        return Paths.get(fullPath);
    }

    private String readLastDateFile(Path lastDateFilePath) {
        try (Stream<String> lines = GlobalUtils.readFile(lastDateFilePath.toString())) {
            List<String> collect = lines.collect(Collectors.toList());
            log.info("Content: {}", collect.get(0));
            String[] content = collect.get(0).split(",");
            log.info("Last persian date: {}", content[0]);
            return content[0];
        } catch (IOException e) {
            log.error("Exception occurred to read file {}.", lastDateFilePath);
            e.printStackTrace();
        }
        return "";
    }

    private String readLastIndex(Path lastDateFilePath) {
        try (Stream<String> lines = GlobalUtils.readFile(lastDateFilePath.toString())) {
            List<String> collect = lines.collect(Collectors.toList());
            log.info("Content: {}", collect.get(0));
            String[] content = collect.get(0).split(",");
            log.info("Last index: {}", content[1]);
            return content[1];
        } catch (IOException e) {
            log.error("Exception occurred to read file {}.", lastDateFilePath);
            e.printStackTrace();
        }
        return "";
    }

    private int getGasLastIndex(Path lastDateFilePath) {
        String lastIndexFromFile = readLastIndex(lastDateFilePath);
        return Integer.parseInt(lastIndexFromFile) + 1;
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

    private String createISCFileName(String date) {
        return bankName + "_ALL_BILLS" + date + iscNorFilePostfix;
    }

    private String createNasimFileName(Date date) {
        // file name is (NOR_'Date'_ALL_BILLS_'Counter'.TXT) ; Example NOR_14000714_ALL_BILLS_1.TXT
        String sendDate = fileDate(date);
        int counter = 1;
        return bankName + "_" + sendDate + "_" + "ALL_BILLS_" + counter + nasimFilePostfix;
    }

    private String createGasFileName(int index, String companyCode, Date date) {
        // file name is (NOR'Date'GA'Counter'.'CompanyCode') ; Example NOR14000714GA001.062
        String sendDate = fileDate(date);
        String counter = String.format("%03d", index);
        return bankName + sendDate + "GA" + counter + "." + companyCode;
    }

    private String createDayBankFileName(Date date) {
        // file name is (DAY_'Date'.TXT) ; Example DAY_14000723.TXT
        String sendDate = fileDate(date);
        return dayBankName + "_" + sendDate + dayFilePostfix;
    }

    private String createFileHeaderBaseOnProject(String projectName, long totalPriceL, long recordNoL, String subUtilityCode, Date date) {
        // Header base on Nasim. (bankCode + sendDate + totalPrice + recordNo); Example: 801400071400000000000001511457000000001080
        // Header base on Gas Co. (utilityCode + subUtilityCode + bankCode + sendDate + totalPrice + recordNo); Example: 3082801400071400000000000000428232000000000536
        // Header base on Day Bank. (sendDate + totalPrice + recordNo); Example: 000723000042679400000312
        String totalPrice;
        String recordNo;
        String sendDate = fileDate(date);

        if (projectName.equals(ProjectNameEnum.NASIM.getValue()) || projectName.equals(ProjectNameEnum.GAS.getValue())) {
            totalPrice = String.format("%020d", totalPriceL);
            recordNo = String.format("%012d", recordNoL);
        } else {
            totalPrice = String.format("%010d", totalPriceL);
            recordNo = String.format("%08d", recordNoL);
        }

        if (projectName.equals(ProjectNameEnum.NASIM.getValue())) {
            return bankCode + sendDate + totalPrice + recordNo;
        } else if (projectName.equals(ProjectNameEnum.GAS.getValue())) {
            return utilityCode + subUtilityCode + bankCode + sendDate + totalPrice + recordNo;
        }
        return sendDate.substring(2) + totalPrice + recordNo;
    }

    private List<String> createSettlementData(List<String> customList, String subUtilityCode, String projectName, Date date) {
        List<String> billArrayList = new ArrayList<>();
        AtomicLong sum = new AtomicLong();
        if (projectName.equals(ProjectNameEnum.DAY.getValue())) {
            // Like ISC file without UtilityCode=3
            billArrayList = customList.stream()
                    .peek(s -> sum.set(Long.parseLong(s.substring(27, 35)) + sum.get()))
                    .collect(Collectors.toList());
        } else {
            // value before change
            // 00156602000614604688420101400011388346481042390005892101141385191     ATM17310001
            // value after change
            // 001566021400061460468842010140001138834648104239
            String year = CalendarUtils.getCurrentPersianDateTime(GlobalUtils.CALENDAR_YEAR_PATTERN_YYYY);
            billArrayList = customList.stream()
                    .map(s -> s.substring(0, 46))
                    .peek(s -> sum.set(Long.parseLong(s.substring(27, 35)) + sum.get()))
                    .map(s -> GlobalUtils.insertString(s, year.substring(0, 2), 8))
                    .collect(Collectors.toList());
        }

        List<String> finalBillList = new ArrayList<>();
        finalBillList.add(createFileHeaderBaseOnProject(projectName, sum.longValue(), billArrayList.size(), subUtilityCode, date));
        finalBillList.addAll(billArrayList);
        return finalBillList;
    }

    private boolean canConnectToFTPNasim() {
        boolean status = Boolean.FALSE;
        FTPUtils fTP = new FTPUtils();
        if (fTP.connect((String) startUpInit.cache.get("nasim.ip"), (Integer) startUpInit.cache.get("nasim.port"))) {
            if (fTP.login((String) startUpInit.cache.get("nasim.username"), (String) startUpInit.cache.get("nasim.password"))) {
                status = Boolean.TRUE;
            }
            fTP.disconnect();
        }
        return status;
    }

    private boolean canConnectToFTPGas() {
        boolean status = Boolean.FALSE;
        FTPUtils fTP = new FTPUtils();
        if (fTP.connect((String) startUpInit.cache.get("gas.ip"), (Integer) startUpInit.cache.get("gas.port"))) {
            if (fTP.login((String) startUpInit.cache.get("gas.username"), (String) startUpInit.cache.get("gas.password"))) {
                status = Boolean.TRUE;
            }
            fTP.disconnect();
        }
        return status;
    }


}
