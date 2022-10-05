package org.example;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        try {
            System.out.println(parser.parse(System.in));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}