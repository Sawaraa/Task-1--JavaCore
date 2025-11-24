package org.example;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testParsingJsonFiles() throws Exception {
        // Беремо тестові файли з src/test/resources/test-json
        URL resource = getClass().getClassLoader().getResource("test-json");
        String folderPath = Paths.get(resource.toURI()).toString();

        ParsingJsonFile parsing = new ParsingJsonFile(folderPath);
        List<String> attributes = List.of("genre", "author");

        Statistics stats = parsing.readAllJsonFiles(attributes, 2);

        // Перевіряємо статистику по genre
        Map<String, Integer> genreStats = stats.getStats("genre");
        assertEquals(2, genreStats.get("Romance"));
        assertEquals(1, genreStats.get("Dystopian"));

        // Перевіряємо статистику по author
        Map<String, Integer> authorStats = stats.getStats("author");
        assertEquals(1, authorStats.get("John Doe"));
        assertEquals(1, authorStats.get("Jane Smith"));
        assertEquals(1, authorStats.get("Herman Melville"));
    }
}
