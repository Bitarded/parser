package org.example.strategy;

import lombok.SneakyThrows;
import org.example.model.Item;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.LongAccumulator;

import static java.lang.Long.parseLong;

/**
 * The type Abstract parser.
 */
public abstract class Parser {
    /**
     * Maximum amount Objects in file.
     */
    static final int MAX_OBJECTS_IN_FILE = 250_000;
    volatile boolean isArrayCopied = false;

    public abstract void parse(String line) throws IOException, InterruptedException, BrokenBarrierException;

    /**
     * Maps String line to Item.
     *
     * @param line the line
     * @return the item
     * @throws IllegalArgumentException the illegal argument exception
     */
    Item mapToItem(String line) throws IllegalArgumentException {
        String[] fields = line.split(",");
        if (fields.length != 4) throw new IllegalArgumentException("Invalid line - " + line);
        return new Item(fields[0], fields[1], parseLong(fields[2]), parseLong(fields[3]));
    }

    /**
     * Print statistic in the console.
     *
     * @param duplicatesMap the duplicates map
     * @param weightMap     the weight map
     * @param min           the min
     * @param max           the max
     */
    void print(Map<String, Integer> duplicatesMap, Map<String, BigInteger> weightMap, LongAccumulator min, LongAccumulator max) {
        duplicatesMap.forEach((k, v) -> System.out.printf("Item %s duplicated %s times\n", k, v));
        weightMap.forEach((k, v) -> System.out.printf("Group %s has %d weight\n", k, v));
        System.out.println("Min weight in the file " + min);
        System.out.println("Max weight in the file " + max);
    }

    void deleteTempFiles(Queue<File> tempFiles) {
        for (File tempFile : tempFiles) {
            tempFile.delete();
        }
        tempFiles.clear();
    }

    /**
     * Takes Item list and writes to temp files list sorted.
     *
     * @param items the items list
     * @param tempFiles the temp files
     * @throws IOException the io exception
     */
    @SneakyThrows(IOException.class)
    void generateSortedTempFiles(List<Item> items, Queue<File>  tempFiles) {
        List<Item> itemsList = new ArrayList<>(items);
        isArrayCopied = true;
        itemsList.sort(Item.itemComparator);
        File tempFile = File.createTempFile("chunk", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (Item item : itemsList) {
                writer.write(String.valueOf(item));
                writer.newLine();
            }
        }
        tempFiles.add(tempFile);
    }
    @SneakyThrows
    void parseTempFiles(File tempFile,
                        Map<String, Integer> itemsMap,
                        Map<String, BigInteger> weightMap,
                        LongAccumulator min, LongAccumulator max) {
        BufferedReader reader = new BufferedReader(new FileReader(tempFile));
        String line;
        while ((line = reader.readLine()) != null) {
            Item item = mapToItem(line);
            long weight = item.getWeight();
            itemsMap.merge(item.getGroup() + "," + item.getType(), 1, Integer::sum);
            weightMap.merge(item.getGroup(), BigInteger.valueOf(weight), BigInteger::add);
            min.accumulate(weight);
            max.accumulate(weight);
        }
        // Save duplicates
        itemsMap.entrySet().removeIf(e -> e.getValue() < 2);
    }
}
