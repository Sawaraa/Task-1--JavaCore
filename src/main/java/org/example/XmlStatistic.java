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

public class XmlStatistic {
    public void generateAllXmlFiles(Statistics statistics, String outputFolder) throws Exception {

        Map<String, Map<String, Integer>> allStats = statistics.getAllStats();

        for (String attributeName : allStats.keySet()) {
            generateSingleXmlFile(statistics, attributeName, outputFolder);
        }
    }

    private void generateSingleXmlFile(Statistics statistics,
                                       String attributeName,
                                       String outputFolder) throws Exception {

        Map<String, Integer> values = statistics.getStats(attributeName);

        if (values.isEmpty()) {
            System.out.println("Немає статистики для атрибута: " + attributeName);
            return;
        }

        // сортуємо за спаданням
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(values.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        String fileName = outputFolder + "/statistics_by_" + attributeName + ".xml";

        writeXml(attributeName, sorted, fileName);
    }

    private void writeXml(String attributeName,
                          List<Map.Entry<String, Integer>> sorted,
                          String fileName) throws Exception {

        // 1) Генеруємо XML у String (без форматування)
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter rawXml = new StringWriter();
        XMLStreamWriter writer = factory.createXMLStreamWriter(rawXml);

        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("statistics");

        for (Map.Entry<String, Integer> entry : sorted) {

            writer.writeStartElement("item");

            writer.writeStartElement("value");
            writer.writeCharacters(entry.getKey());
            writer.writeEndElement(); // </value>

            writer.writeStartElement("count");
            writer.writeCharacters(String.valueOf(entry.getValue()));
            writer.writeEndElement(); // </count>

            writer.writeEndElement(); // </item>
        }

        writer.writeEndElement(); // </statistics>
        writer.writeEndDocument();
        writer.close();

        // 2) Форматуємо XML через Transformer
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // кількість пробілів
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamSource xmlInput = new StreamSource(new StringReader(rawXml.toString()));
        StreamResult xmlOutput = new StreamResult(new File(fileName));

        transformer.transform(xmlInput, xmlOutput);
    }

}
