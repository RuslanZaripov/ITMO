package org.example.node.assignment;

import org.example.node.Node;

public interface Assignment extends Node {
    default String getLabel() {
        return "assign";
    }
}
