package ru.itmo.parser.node.assignment;

import ru.itmo.parser.node.Node;

public interface Assignment extends Node {
    default String getLabel() {
        return "assign";
    }
}
