package ru.itmo.parser;

import ru.itmo.parser.node.Node;
import ru.itmo.parser.visualizer.Visualizer;

import java.text.ParseException;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        try {
            final String path = "src/main/resources/graph/tree.dot";
            final Node node = parser.parse(System.in);
            System.out.println(node);
            Visualizer visualizer = new Visualizer(node, path);
            visualizer.visualize();
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
        }
    }
}