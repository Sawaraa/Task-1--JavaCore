package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Введіть шлях до папки з JSON: ");
            String folderPath = scanner.nextLine(); //C:\\Users\\Admin\\Desktop\\json

            System.out.print("Введіть атрибути через кому (наприклад genre,author): ");
            List<String> attributes = Arrays.stream(scanner.nextLine().split(","))
                    .map(String::trim).toList();

            System.out.print("Введіть шлях для збереження XML: "); //C:\Users\Admin\Desktop\Task-1\xml C:\Users\Admin\Desktop\xml
            String xmlPath = scanner.nextLine();

            ParsingJsonFile parsing = new ParsingJsonFile(folderPath);

            long startTime = System.nanoTime();
            Statistics statistics = parsing.readAllJsonFiles(attributes, 4);
            long endTime = System.nanoTime();

            System.out.println("Час виконання читання JSON-файлів: " + (endTime - startTime) / 1_000_000 + " мс");

            XmlStatistic xmlStatistic =  new XmlStatistic();
            xmlStatistic.generateAllXmlFiles(statistics, xmlPath);

            statistics.printStats();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}