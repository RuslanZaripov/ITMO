package ru.itmo.parser.node;

import java.util.List;

public interface Node {
    String toString();

    List<Node> getChildren();

    String getLabel();

    int getNodesAmount();

    int getNodeId();

    void setNodeId(int number);
}
