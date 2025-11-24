package org.example;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XmlStatistic is responsible for generating XML files from a Statistics object.
 * Each attribute in the statistics will generate a separate XML file.
 */
public class XmlStatistic {

    /**
     * Generates XML files for all attributes in the provided Statistics object.
     *
     * @param statistics   Statistics object containing data to write
     * @param outputFolder folder path where XML files will be saved
     */
    public void generateAllXmlFiles(Statistics statistics, String outputFolder) throws Exception {

        Map<String, Map<String, Integer>> allStats = statistics.getAllStats();

        for (String attributeName : allStats.keySet()) {
            generateSingleXmlFile(statistics, attributeName, outputFolder);
        }
    }

    /**
     * Generates a single XML file for a given attribute.
     *
     * @param statistics   Statistics object containing the attribute data
     * @param attributeName attribute name to generate XML for
     * @param outputFolder folder path where the XML file will be saved
     */
    private void generateSingleXmlFile(Statistics statistics,
                                       String attributeName,
                                       String outputFolder) throws Exception {

        Map<String, Integer> values = statistics.getStats(attributeName);

        if (values.isEmpty()) {
            System.out.println("Немає статистики для атрибута: " + attributeName);
            return;
        }

        // Sort the values by count descending
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(values.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        String fileName = outputFolder + "/statistics_by_" + attributeName + ".xml";

        writeXml(attributeName, sorted, fileName);
    }

    /**
     * Writes XML content for a given attribute and sorted values to a file.
     *
     * @param attributeName attribute name
     * @param sorted        list of sorted key-value pairs
     * @param fileName      file path to write the XML
     */
    private void writeXml(String attributeName,
                          List<Map.Entry<String, Integer>> sorted,
                          String fileName) throws Exception {

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter rawXml = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(rawXml);

        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("statistics");

        for (Map.Entry<String, Integer> entry : sorted) {

            writer.writeStartElement("item");

            writer.writeStartElement("value");
            writer.writeCharacters(entry.getKey());
            writer.writeEndElement();

            writer.writeStartElement("count");
            writer.writeCharacters(String.valueOf(entry.getValue()));
            writer.writeEndElement();

            writer.writeEndElement();
        }

        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamSource xmlInput = new StreamSource(new StringReader(rawXml.toString()));
        StreamResult xmlOutput = new StreamResult(new File(fileName));

        transformer.transform(xmlInput, xmlOutput);
    }

}
