package org.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParsingJsonFile {

    private final String folderPath;

    public ParsingJsonFile(String folderPath) {
        this.folderPath = folderPath;
    }

    /**
     * Читає всі JSON-файли в папці паралельно і збирає статистику.
     *
     * @param attributeNames список атрибутів, які потрібно враховувати
     * @return Statistics з усіма файлами
     * @throws IOException
     */
    public Statistics readAllJsonFiles(List<String> attributeNames, int threadCount) throws IOException {

        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            throw new IOException("Folder not found or empty: " + folderPath);
        }

        // Використовуємо пул потоків — стільки потоків, скільки ядер
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<Statistics>> futures = new ArrayList<>();

        // Кожен файл — окремий таск
        for (File file : files) {
            futures.add(executor.submit(() -> {
                System.out.println("Thread " + Thread.currentThread().getName() + " is processing file: " + file.getName());
                Statistics localStats = readJsonFile(file, attributeNames);
                System.out.println("Thread " + Thread.currentThread().getName() + " finished file: " + file.getName());
                return localStats;
            }));
        }

        Statistics finalStats = new Statistics();

        // Збираємо всі локальні статистики і об’єднуємо
        for (Future<Statistics> future : futures) {
            try {
                Statistics localStats = future.get();
                finalStats.merge(localStats);
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException("Failed to parse JSON file", e);
            }
        }

        executor.shutdown();
        return finalStats;
    }

    /**
     * Парсить один JSON-файл і повертає локальну Statistics.
     *
     * @param file           JSON-файл
     * @param attributeNames список атрибутів, які потрібно враховувати
     * @return локальна Statistics
     * @throws IOException
     */
    private Statistics readJsonFile(File file, List<String> attributeNames) throws IOException {
        Statistics stats = new Statistics();
        JsonFactory factory = new JsonFactory();

        try (JsonParser parser = factory.createParser(file)) {

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected JSON array in " + file.getName());
            }

            // читаємо об’єкти масиву
            while (parser.nextToken() != JsonToken.END_ARRAY) {

                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String currentName = parser.getCurrentName();
                    parser.nextToken(); // перейти до значення

                    processAttribute(currentName, parser, attributeNames, stats);
                }
            }
        }

        return stats;
    }

    /**
     * Обробка одного атрибута в JSON-об'єкті.
     */
    private void processAttribute(String currentName,
                                  JsonParser parser,
                                  List<String> attributeNames,
                                  Statistics stats) throws IOException {

        if (!attributeNames.contains(currentName)) return;

        String value;

        if ("year_published".equals(currentName)) {
            value = String.valueOf(parser.getIntValue());
        } else {
            value = parser.getText();
        }

        stats.addValue(currentName, value);
    }

}
