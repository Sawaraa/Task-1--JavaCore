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

/**
 * ParsingJsonFile is responsible for reading and parsing JSON files
 * from a specified folder and collecting statistics on specific attributes.
 * It supports parallel processing using a thread pool.
 */
public class ParsingJsonFile {

    private final String folderPath;

    public ParsingJsonFile(String folderPath) {
        this.folderPath = folderPath;
    }

    /**
     * Reads all JSON files in the folder in parallel and collects statistics.
     *
     * @param attributeNames list of attribute names to include in statistics
     * @param threadCount    number of threads to use in the thread pool
     * @return Statistics object containing combined statistics for all files
     */
    public Statistics readAllJsonFiles(List<String> attributeNames, int threadCount) throws IOException {

        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            throw new IOException("Folder not found or empty: " + folderPath);
        }


        // Create a thread pool with the specified number of threads
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<Statistics>> futures = new ArrayList<>();

        // Submit each file as a separate task to the thread pool
        for (File file : files) {
            futures.add(executor.submit(() -> {
                System.out.println("Thread " + Thread.currentThread().getName() + " is processing file: " + file.getName());
                Statistics localStats = readJsonFile(file, attributeNames);
                System.out.println("Thread " + Thread.currentThread().getName() + " finished file: " + file.getName());
                return localStats;
            }));
        }

        Statistics finalStats = new Statistics();

        // Combine results from all threads into a single Statistics object
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
     * Parses a single JSON file and returns its local Statistics.
     *
     * @param file           JSON file to parse
     * @param attributeNames list of attribute names to include in statistics
     * @return local Statistics object for the file
     */
    private Statistics readJsonFile(File file, List<String> attributeNames) throws IOException {
        Statistics stats = new Statistics();
        JsonFactory factory = new JsonFactory();

        try (JsonParser parser = factory.createParser(file)) {

            // Expect a JSON array at the top level
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected JSON array in " + file.getName());
            }

            // Read each object in the array
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
     * Processes a single attribute in a JSON object.
     * If the attribute is in the list of required attributes, it is added to the statistics.
     *
     * @param currentName    attribute name
     * @param parser         JsonParser positioned at the value
     * @param attributeNames list of attributes to include in statistics
     * @param stats          Statistics object to update
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
