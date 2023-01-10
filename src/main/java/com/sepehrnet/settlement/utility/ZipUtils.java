package com.sepehrnet.settlement.utility;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.nio.file.Path;

@Slf4j
public class ZipUtils {

    public static void extractAll(Path source, Path target) {
        try {
            new ZipFile(source.toFile()).extractAll(target.toString());
        } catch (ZipException e) {
            log.info("Extract all files exception occurred: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void extractFile(Path source, Path target, String fileName) {
        try {
            new ZipFile(source.toFile()).extractFile(fileName, target.toString());
        } catch (ZipException e) {
            log.info("Extract file exception occurred: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
