package org.example.strategy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.SneakyThrows;
import org.example.model.Item;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.LongAccumulator;

public class JsonParser extends Parser {
    @SneakyThrows
    public void parse(String inputFile) throws IOException {
        long start = System.currentTimeMillis();
        LongAccumulator min = new LongAccumulator(Math::min, Long.MAX_VALUE);
        LongAccumulator max = new LongAccumulator(Math::max, Long.MIN_VALUE);
        ArrayList<Item> itemsArr = new ArrayList<>(MAX_OBJECTS_IN_FILE + 1);
        Queue<File> tempFiles = new ArrayBlockingQueue<>(200);
        Map<String, Integer> itemsMap = new HashMap<>();
        Map<String, BigInteger> weightMap = new HashMap<>();
        List<Thread> threads = new ArrayList<>();

        //Map lines to Pojo
        try (JsonReader jsonReader = new JsonReader(new InputStreamReader
                (new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            Gson gson = new GsonBuilder().create();
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                Item gsonItem = gson.fromJson(jsonReader, Item.class);
                itemsArr.add(gsonItem);
                // Split file if it's too big
                if (itemsArr.size() >= MAX_OBJECTS_IN_FILE || !jsonReader.hasNext()) {
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
            jsonReader.endArray();
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

