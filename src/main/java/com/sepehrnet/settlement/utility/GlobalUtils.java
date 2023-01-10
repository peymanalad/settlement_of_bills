package com.sepehrnet.settlement.utility;

import com.sepehrnet.settlement.enums.GregorianCalendarLocale;
import com.sepehrnet.settlement.enums.Language;
import com.sepehrnet.settlement.enums.PersianCalendarLocale;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class GlobalUtils {

    public static final String EN = Language.ENGLISH.getValue();
    public static final String FA = Language.PERSIAN.getValue();
    public static final String CALENDAR_DATE_TIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
    public static final String CALENDAR_DATE_TIME_FLAT_PATTERN = "yyyyMMddHHmmss";
    public static final String CALENDAR_DATE_PATTERN = "yyyy/MM/dd";
    public static final String CALENDAR_DATE_FLAT_PATTERN = "yyyyMMdd";
    public static final String CALENDAR_TIME_PATTERN = "HH:mm:ss";
    public static final String CALENDAR_TIME_FLAT_PATTERN = "HHmmss";
    public static final String CALENDAR_YEAR_PATTERN_YYYY = "yyyy";
    public static final String CALENDAR_PERSIAN_TYPE_WITH_LOCALE_FA_IR = PersianCalendarLocale.LOCALE_FA.getValue();
    public static final String CALENDAR_PERSIAN_TYPE_WITH_LOCALE_EN_US = PersianCalendarLocale.LOCALE_EN.getValue();
    public static final String CALENDAR_GREGORIAN_TYPE_WITH_LOCALE_FA_IR = GregorianCalendarLocale.LOCALE_FA.getValue();
    public static final String CALENDAR_GREGORIAN_TYPE_WITH_LOCALE_EN_US = GregorianCalendarLocale.LOCALE_EN.getValue();
    /**
     * Unix-style end-of-line marker (LF)
     */
    private static final String EOL_UNIX = "\n";

    /**
     * Windows-style end-of-line marker (CRLF)
     */
    private static final String EOL_WINDOWS = "\r\n";

    public static boolean checkDirectoryExist(String path) {
        Path dirPath = Paths.get(path);
        return Files.isDirectory(dirPath);
    }

    public static boolean checkFileExist(String path, String fileName) {
        Path file = Paths.get(path + fileName);
        return Files.exists(file);
    }

    public static void createDirectory(String path) {
        try {
            if (!checkDirectoryExist(path)) {
                Path dirPath = Paths.get(path);
                Files.createDirectories(dirPath);
                log.info("Directories {} is created.", path);
            }
        } catch (IOException e) {
            log.error("Exception occurred to create directory {}.", path);
            e.printStackTrace();
        }
    }

    public static void writeToFile(String path, String fileName, Object object) {
        String fullPath = path + fileName;
        File file = new File(fullPath);
        try {
            Path filePath = Paths.get(fullPath);
            if (!checkDirectoryExist(path))
                createDirectory(path);
            if (!checkFileExist(path, fileName))
                Files.createFile(filePath);
            if (object instanceof List) {
                List<String> content = (List<String>) object;
                //Files.write(filePath, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
                fileWriter(file, content);
            } else if (object instanceof String) {
                String content = (String) object;
                Files.write(filePath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            }
            log.info("File {} create successfully.", fullPath);
        } catch (IOException e) {
            log.error("Exception occurred to write file {}.", fullPath);
            e.printStackTrace();
        }
    }

    private static void fileWriter(File file, List<String> content) throws IOException {
        try (FileWriter fw = new FileWriter(file, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            int counter = 0;
            for (String s : content) {
                bw.write(s);
                if (counter < content.size() - 1) {
                    //bw.newLine();
                    bw.write(EOL_WINDOWS);
                    counter = counter + 1;
                }
            }
        }
    }

    public static Stream<String> readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.lines(path, StandardCharsets.UTF_8);
    }

    /*    public static String getPathOfJarFile() {
     *//*               File jarFile = new File(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(".")).getPath());
        File dir = jarFile.getAbsoluteFile().getParentFile();
        String jarDirectory = dir.toString();
        log.info("JAR file path: {}", jarDirectory);
        return jarDirectory;*//*
        try {
            String jarFilePath = GlobalUtils.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            String jarPath = jarFilePath.substring(0, jarFilePath.lastIndexOf("/") + 1);
            log.info("JAR File Path: {}", jarPath);
            return jarPath;
        } catch (URISyntaxException e) {
            log.error("Exception occurred for jar path");
            e.printStackTrace();
        }
        return "";
    }*/

    public static List<Date> datesBetweenTwoDate(Date startDate, Date endDate) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);
        while (calendar.getTime().before(endDate)) {
            calendar.add(Calendar.DATE, 1);
            Date result = calendar.getTime();
            dates.add(result);
        }
        return dates;
    }

    public static String insertString(String originalString, String stringToBeInsert, int index) {
        String start = originalString.substring(0, index);
        String end = originalString.substring(index);
        return start + stringToBeInsert + end;
    }
}
