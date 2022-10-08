package ru.itmo.parser.visualizer;

import ru.itmo.parser.node.Node;

import java.io.*;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Visualizer {
    private final Node tree;
    private final String path;
    private final StringBuilder builder;
    private final String TAB = "\t";
    private final String EOL = "\n";

    public Visualizer(final Node node, final String path) {
        this.path = path;
        this.tree = node;
        this.builder = new StringBuilder();
    }

    private static String setNodeColorParameterRed(final int nodeNumber) {
        return "%d [color=red]".formatted(nodeNumber);
    }

    private static String setNode(final Node curr, final int nodeNumber) {
        return "%d [label=\"%s\"]".formatted(nodeNumber, curr.getLabel());
    }

    private static String setEdge(final int nodeNumber, final int nextNodeNumber) {
        return "%d -> %d".formatted(nodeNumber, nextNodeNumber);
    }

    public void visualize() {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), UTF_8))) {
            writer.write(buildGraph(tree));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String buildGraph(final Node node) {
        buildGraphHelper(node, 0);
        return "digraph G {" + EOL
                + TAB + "node [shape=oval, style=filled, fixedsize=true]" + EOL
                + builder + EOL
                + "}" + EOL;
    }

    private void buildGraphHelper(final Node curr, final int nodeNumber) {
        curr.setNumber(nodeNumber);
        addEntry(setNode(curr, nodeNumber));
        List<Node> children = curr.getChildren();
        if (children.isEmpty()) {
            addEntry(setNodeColorParameterRed(nodeNumber));
        }
        for (int i = 0; i < children.size(); i++) {
            int nextNodeNumber;
            if (i == 0) {
                nextNodeNumber = nodeNumber + 1;
            } else {
                Node prev = children.get(i - 1);
                nextNodeNumber = prev.getNumber() + prev.getCountNumber();
            }
            buildGraphHelper(children.get(i), nextNodeNumber);
            addEntry(setEdge(nodeNumber, nextNodeNumber));
        }
    }

    private void addEntry(final String str) {
        this.builder.append(TAB).append(str).append(EOL);
    }
}

