package org.example;
import org.junit.jupiter.api.Test;

import javax.sql.rowset.spi.XmlWriter;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    void testJsonFilesParsing() throws Exception {
        // Check that JSON files are parsed correctly and expected keys exist
        URL resource = getClass().getClassLoader().getResource("test-json");
        String folderPath = Paths.get(resource.toURI()).toString();

        ParsingJsonFile parsing = new ParsingJsonFile(folderPath);
        List<String> attributes = List.of("genre", "author");

        Statistics stats = parsing.readAllJsonFiles(attributes, 2);

        // Verify that statistics contain the expected keys
        assertTrue(stats.getAllStats().containsKey("genre"));
        assertTrue(stats.getAllStats().containsKey("author"));

        // Verify that the statistics for each key are not empty
        assertFalse(stats.getStats("genre").isEmpty());
        assertFalse(stats.getStats("author").isEmpty());
    }

    @Test
    void testStatisticsValues() throws Exception {
        // Check that the statistics contain the correct counts for each value
        URL resource = getClass().getClassLoader().getResource("test-json");
        String folderPath = Paths.get(resource.toURI()).toString();

        ParsingJsonFile parsing = new ParsingJsonFile(folderPath);
        List<String> attributes = List.of("genre", "author");

        Statistics stats = parsing.readAllJsonFiles(attributes, 2);

        // Verify counts for genre
        Map<String, Integer> genreStats = stats.getStats("genre");
        assertEquals(2, genreStats.get("Romance"));
        assertEquals(1, genreStats.get("Dystopian"));

        // Verify counts for author
        Map<String, Integer> authorStats = stats.getStats("author");
        assertEquals(1, authorStats.get("John Doe"));
        assertEquals(1, authorStats.get("Jane Smith"));
        assertEquals(1, authorStats.get("Herman Melville"));
    }

    @Test
    void testThreadSafety() throws Exception {
        // Check that multithreaded parsing produces the same results as single-threaded parsing
        URL resource = getClass().getClassLoader().getResource("test-json");
        String folderPath = Paths.get(resource.toURI()).toString();

        ParsingJsonFile parsing = new ParsingJsonFile(folderPath);
        List<String> attributes = List.of("genre", "author");

        // Parse with a single thread
        Statistics statsSingleThread = parsing.readAllJsonFiles(attributes, 1);

        // Parse with multiple threads
        Statistics statsMultiThread = parsing.readAllJsonFiles(attributes, 4);

        // Verify that results are identical
        assertEquals(statsSingleThread.getAllStats(), statsMultiThread.getAllStats());
    }

    @Test
    void testXmlWriter() throws Exception {
        // Check that XML files are generated correctly
        Statistics stats = new Statistics();
        stats.addValue("genre", "Romance");
        stats.addValue("genre", "Dystopian");

        // Create a temporary directory for output XML files
        Path tempDir = Files.createTempDirectory("xml-test");
        XmlStatistic xmlStat = new XmlStatistic();
        xmlStat.generateAllXmlFiles(stats, tempDir.toString());

        // Verify that files were created correctly
        File[] files = tempDir.toFile().listFiles();
        assertNotNull(files);
        assertEquals(1, files.length);  // only one XML file for "genre"
        assertTrue(files[0].getName().contains("statistics_by_genre")); // file name contains expected attribute
    }

}
