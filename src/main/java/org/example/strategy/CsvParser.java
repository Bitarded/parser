package org.example.strategy;

import lombok.SneakyThrows;
import org.example.model.Item;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAccumulator;


public class CsvParser extends Parser {
    @SneakyThrows
    public void parse(String inputFile)  {
        long start = System.currentTimeMillis();
        LongAccumulator min = new LongAccumulator(Math::min,Long.MAX_VALUE);
        LongAccumulator max = new LongAccumulator(Math::max,Long.MIN_VALUE);
        ArrayList<Item> itemsArr = new ArrayList<>(MAX_OBJECTS_IN_FILE + 1);
        Queue<File> tempFiles = new ArrayBlockingQueue<>(200);
        Map<String, Integer> itemsMap = new HashMap<>();
        Map<String, BigInteger> weightMap = new HashMap<>();
        List<Thread> threads = new ArrayList<>();

        //Map lines to Pojo
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                itemsArr.add(mapToItem(line));
                // Split file if it's too big
                if (itemsArr.size() == MAX_OBJECTS_IN_FILE || !reader.ready()) {
                    Thread thread = new Thread(() -> generateSortedTempFiles(itemsArr, tempFiles));
                    threads.add(thread);
                    thread.start();
                    while (!isArrayCopied) {
                        Thread.onSpinWait();
                    }
                    itemsArr.clear();
                    isArrayCopied = false;
                }
            }
        }
        //Get duplicates,min,max and weight for every group from files
        for (Thread thread : threads) {
            thread.join();
        }
        tempFiles.forEach(file -> parseTempFiles(file, itemsMap, weightMap, min, max));
        print(itemsMap, weightMap, min, max);
        deleteTempFiles(tempFiles);
        System.out.printf("Total Time Taken: %d seconds\n", (System.currentTimeMillis() - start) / 1000);
    }


}