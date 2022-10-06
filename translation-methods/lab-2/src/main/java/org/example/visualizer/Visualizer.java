package org.example.visualizer;

import org.example.node.Node;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Visualizer {
    private final Node tree;
    private final String path;

    public Visualizer(Node node, String path) {
        this.path = path;
        this.tree = node;
    }

    public void visualize() {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write(buildGraph(tree));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String buildGraph(Node node) {
        return "digraph G {" + "\n" +
                "\t" + "node [shape=circle, style=filled]" + "\n" +
                buildGraphHelper(node, 0) +
                "}" + "\n";
    }

    private String buildGraphHelper(Node curr, int nodeNumber) {
        curr.setNumber(nodeNumber);
        StringBuilder builder = new StringBuilder();
        builder.append("\t").append("%d [label=\"%s\"]".formatted(nodeNumber, curr.getLabel())).append("\n");
        List<Node> children = curr.getChildren();
        if (children.isEmpty()) {
            builder.append("\t").append("%d [color=red]".formatted(nodeNumber)).append("\n");
        }
        for (int i = 0; i < children.size(); i++) {
            int nextNodeNumber = i == 0
                    ? nodeNumber + 1
                    : children.get(i - 1).getNumber() + children.get(i - 1).getCountNumber();
            builder.append(buildGraphHelper(children.get(i), nextNodeNumber));
            builder.append("\t").append("%d -> %d".formatted(nodeNumber, nextNodeNumber)).append("\n");
        }
        return builder.toString();
    }
}

