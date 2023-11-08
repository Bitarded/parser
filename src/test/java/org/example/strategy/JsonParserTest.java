package org.example.strategy;

import org.example.model.Item;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    void givenFile_shouldParseStatistic() throws IOException {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        File file = File.createTempFile("chunk", ".сsv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i <= 100; i++) {
                writer.write("1,1,1,1");
                writer.newLine();
            }
        }
        new CsvParser().parse(file.getAbsolutePath());
        String ex = "Item 1,1 duplicated 100 times\n" +
                "Group 1 has 100 weight\n" +
                "Min weight in the file 1\n" +
                "Max weight in the file 1\n" +
                "Total Time Taken: 0 seconds";
        assertEquals(ex.replaceAll("\\s", ""), outContent.toString().replaceAll("\\s", ""));

    }

    @Test
    void givenStringLine_shouldMapToItem_ThenReturnItem() {
        String line = "123,123,123,123";
        Object o = new JsonParser().mapToItem(line);

        assertInstanceOf(Item.class, o);
    }

    @Test
    void givenBadStringLine_shouldThrow() {
        String line = "123,123,123,";
        assertThrows(IllegalArgumentException.class, () -> new JsonParser().mapToItem(line));
    }

    @Test
    void shouldDeleteTempFiles() {
        File file = new File("");
        Queue<File> tempFiles = new ArrayBlockingQueue<>(20);
        tempFiles.add(file);
        new JsonParser().deleteTempFiles(tempFiles);

        assertTrue(tempFiles.isEmpty());
    }

    @Test
    void shouldGenerateSortedTempFiles() throws IOException {
        List<Item> itemsList = new ArrayList<>();
        Queue<File> tempFiles = new ArrayBlockingQueue<>(20);
        itemsList.add(new Item("123", "123", 123, 123));
        new JsonParser().generateSortedTempFiles(itemsList, tempFiles);

        assertEquals(1, tempFiles.size());
    }
}