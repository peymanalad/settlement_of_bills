package com.sepehrnet.settlement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.partitioningBy;

@SpringBootTest
class SettlementOfBillsApplicationTests {

    @Test
    void contextLoads() throws IOException {
       // main();
    }

    public static void main() throws IOException {

        /*
         * FTP name: Khadamat
         * Destination IP: 10.1.122.61
         * username: Pardakht
         * password: @!Aa135642
         *
         *
         * FTP name: GAS Companies
         * Destination IP: 10.1.115.108
         * username: Pardakht
         * password: @!Aa135642
         *
         *
         * FTP name: Nasim Companies
         * Destination IP: 10.0.65.90
         * username: Pardakht
         * password: @!Aa135642
         * */

        try (Stream<String> inputStream = readFile("D:\\workspace\\sample bill file\\NOR_ALL_BILLS000614.TXT")) {

            if (inputStream != null) {
                //skip line 1
                //filter by service type ==> Type=3 (Gas)
                Map<Boolean, List<String>> filterByTypeBooleanListMap = inputStream
                        .skip(1)
                        .collect(partitioningBy(s -> s.charAt(25) == '3'));

                List<String> gasBillList = filterByTypeBooleanListMap.get(true);
                List<String> withoutGasBillList = filterByTypeBooleanListMap.get(false);

                writeFile("D:\\workspace\\sample bill file\\", createFileNameForCentralBank(), createBill(withoutGasBillList, "", "NASIM"));

                //Group by company code
                Map<String, List<String>> gasMap = gasBillList.stream()
                        .collect(
                                Collectors.groupingBy(s -> s.substring(22, 25),
                                        Collectors.toList())
                        );

                AtomicInteger index = new AtomicInteger();
                gasMap.forEach((key, value) -> {
                    index.set(index.get() + 1);
                    try {
                        writeFile("D:\\workspace\\sample bill file\\" + key + "\\", createFileNameForNasimProject(index.get(), key), createBill(value, key, "GAS"));
                    } catch (IOException ex) {
                        System.out.println("Write file exception occurred: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static String insertString(String originalString, String stringToBeInsert, int index) {
        String start = originalString.substring(0, index);
        String end = originalString.substring(index);
        return start + stringToBeInsert + end;
    }

    private static List<String> createBill(List<String> customList, String subUtilityCode, String orgName) {
        // value before change
        // 00156602000614604688420101400011388346481042390005892101141385191     ATM17310001
        AtomicLong sum = new AtomicLong();
        List<String> billArrayList = customList.stream()
                .map(s -> s.substring(0, 46))
                .peek(s -> sum.set(Long.parseLong(s.substring(27, 35)) + sum.get()))
                .map(s -> insertString(s, "14", 8))
                .collect(Collectors.toList());
        // value after change
        // 001566021400061460468842010140001138834648104239

        List<String> finalBillList = new ArrayList<>();
        finalBillList.add(createHeaderBaseOnOrganization(orgName, sum.longValue(), billArrayList.size(), subUtilityCode));
        finalBillList.addAll(billArrayList);
        return finalBillList;
    }

    private static String createHeaderBaseOnOrganization(String orgName, long totalPriceL, long recordNoL, String subUtilityCode) {

        String totalPrice = String.format("%020d", totalPriceL);
        String recordNo = String.format("%012d", recordNoL);
        String bankCode = "80";
        String sendDate = "14000714";
        String utilityCode = "3";

        if (orgName.equalsIgnoreCase("NASIM")) {
            // Header base on Central Bank (bankCode + sendDate + totalPrice + recordNo); Example: 801400071400000000000001511457000000001080
            return bankCode + sendDate + totalPrice + recordNo;
        }
        // Header base on NASIM (utilityCode + subUtilityCode + bankCode + sendDate + totalPrice + recordNo); Example: 3082801400071400000000000000428232000000000536
        return utilityCode + subUtilityCode + bankCode + sendDate + totalPrice + recordNo;
    }

    private static Stream<String> readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.lines(path, StandardCharsets.UTF_8);
    }

    private static void writeFile(String path, String fileName, List<String> billList) throws IOException {
        Path dirPath = Paths.get(path);
        Path filePath = Paths.get(path + fileName);
        Files.createDirectories(dirPath);
        Files.createFile(filePath);
        Files.write(filePath, billList, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    private static String createFileNameForCentralBank() {
        // Central bank ==> file name is (NOR_'Date'_ALL_BILLS_'Counter'.TXT) ; Example NOR_14000714_ALL_BILLS_1.TXT
        String bankName = "NOR";
        String date = "14000714";
        int counter = 1;
        return bankName + "_" + date + "_" + "ALL_BILLS_" + counter + ".TXT";
    }

    private static String createFileNameForNasimProject(int index, String companyCode) {
        // Nasim Project ==> file name is (NOR'Date'GA'Counter'.'CompanyCode') ; Example NOR14000714GA001.062
        String bankName = "NOR";
        String date = "14000714";
        String counter = String.format("%03d", index);
        return bankName + date + "GA" + counter + "." + companyCode;
    }

}
