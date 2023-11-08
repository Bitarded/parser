package org.example;

import org.example.model.Format;
import org.example.strategy.CsvParser;
import org.example.strategy.JsonParser;

import java.util.Scanner;

import static org.example.model.Format.csv;
import static org.example.model.Format.json;

public class Main {
    static String JSON = json.name();
    static String CSV = csv.name();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = "";

        while (!input.equals("exit")) {
            System.out.println("Enter the path to the file or exit");
            input = scanner.nextLine();
            try {
                if (input.endsWith(CSV)) new CsvParser().parse(input);
                else if (input.endsWith(JSON)) new JsonParser().parse(input);
                else if (input.equals("exit")) break;
                else System.out.println("Wrong file type");

            } catch (Exception e) {
                System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}
