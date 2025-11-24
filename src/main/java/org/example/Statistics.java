package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Statistics {

    // Зберігаємо потокобезпечну структуру
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicInteger>> stats;

    public Statistics() {
        this.stats = new ConcurrentHashMap<>();
    }

    /**
     * Додає значення(я) для атрибута.
     * Якщо value містить кілька значень розділених комою, розділяє і додає кожне.
     *
     * @param attributeName назва атрибута (наприклад "genre" або "author")
     * @param value         значення атрибута (наприклад "Romance, Satire")
     */
    public void addValue(String attributeName, String value) {
        if (attributeName == null || attributeName.isEmpty() || value == null || value.isEmpty()) {
            return;
        }

        // Отримуємо або створюємо внутрішню map для атрибута
        ConcurrentHashMap<String, AtomicInteger> attributeMap =
                stats.computeIfAbsent(attributeName, k -> new ConcurrentHashMap<>());

        // Розбиваємо по комі (видаляємо зайві пробіли) і інкрементуємо лічильник для кожного підзначення
        String[] parts = value.split(",\\s*");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            attributeMap
                    .computeIfAbsent(part, k -> new AtomicInteger(0))
                    .incrementAndGet();
        }
    }

    /**
     * Повертає копію статистики для конкретного атрибута у вигляді Map<value, count>.
     * Повертається нова HashMap — зовнішній код не бачить AtomicInteger всередині.
     *
     * @param attributeName ім'я атрибута
     * @return Map значень -> кількість
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
     * Повертає копію повної статистики: attribute -> (value -> count)
     *
     * @return Map копія всієї статистики
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
     * Друкує статистику в консоль (для перевірки).
     */
    public void printStats() {
        getAllStats().forEach((attribute, map) -> {
            System.out.println("Статистика для атрибута: " + attribute);
            map.forEach((value, count) -> System.out.println("  " + value + " : " + count));
        });
    }

    /**
     * Зливає іншу Statistics в цю (корисно при паралельній обробці,
     * коли кожен потік збирає локальну Statistics і потім їх треба об'єднати).
     *
     * @param other інша статистика (безпечна для виклику)
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
