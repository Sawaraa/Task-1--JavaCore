package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Statistics {

    // Thread-safe map to store statistics:
    // attributeName -> (value -> count)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicInteger>> stats;

    public Statistics() {
        this.stats = new ConcurrentHashMap<>();
    }

    /**
     * Adds a value (or multiple comma-separated values) for a given attribute.
     * If the value contains multiple entries separated by commas, each is added separately.
     *
     * @param attributeName name of the attribute (e.g., "genre" or "author")
     * @param value         attribute value (e.g., "Romance, Satire")
     */
    public void addValue(String attributeName, String value) {
        if (attributeName == null || attributeName.isEmpty() || value == null || value.isEmpty()) {
            return;
        }

        // Get or create the inner map for the attribute
        ConcurrentHashMap<String, AtomicInteger> attributeMap =
                stats.computeIfAbsent(attributeName, k -> new ConcurrentHashMap<>());

        // Split by comma (trim spaces) and increment count for each value
        String[] parts = value.split(",\\s*");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            attributeMap
                    .computeIfAbsent(part, k -> new AtomicInteger(0))
                    .incrementAndGet();
        }
    }

    /**
     * Returns a copy of the statistics for a specific attribute as Map<value, count>.
     *
     * @param attributeName the name of the attribute
     * @return Map of value -> count
     */
    public Map<String, Integer> getStats(String attributeName) {
        ConcurrentHashMap<String, AtomicInteger> inner = stats.get(attributeName);
        if (inner == null) {
            return new HashMap<>();
        }
        return inner.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));
    }

    /**
     * Returns a copy of the entire statistics: attribute -> (value -> count)
     *
     * @return Map copy of all statistics
     */
    public Map<String, Map<String, Integer>> getAllStats() {
        return stats.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        ie -> ie.getValue().get()
                                ))
                ));
    }


    /**
     * Prints the statistics to the console
     */
    public void printStats() {
        getAllStats().forEach((attribute, map) -> {
            System.out.println("Статистика для атрибута: " + attribute);
            map.forEach((value, count) -> System.out.println("  " + value + " : " + count));
        });
    }

    /**
     * Merges another Statistics object into this one.
     * Each stream collects local statistics
     * and then it needs to be combined.
     *
     * @param other another Statistics object (thread-safe to call)
     */
    public void merge(Statistics other) {
        if (other == null) return;
        Map<String, Map<String, Integer>> otherAll = other.getAllStats();
        for (Map.Entry<String, Map<String, Integer>> attrEntry : otherAll.entrySet()) {
            String attribute = attrEntry.getKey();
            Map<String, Integer> values = attrEntry.getValue();

            ConcurrentHashMap<String, AtomicInteger> targetMap =
                    stats.computeIfAbsent(attribute, k -> new ConcurrentHashMap<>());

            for (Map.Entry<String, Integer> ve : values.entrySet()) {
                targetMap
                        .computeIfAbsent(ve.getKey(), k -> new AtomicInteger(0))
                        .addAndGet(ve.getValue());
            }
        }
    }
}
