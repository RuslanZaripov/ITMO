package org.example;

import org.example.visualizer.Visualizer;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        try {
            final String path = "src/main/resources/graph/tree.dot";
            Visualizer visualizer = new Visualizer(parser.parse(System.in), path);
            visualizer.visualize();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}