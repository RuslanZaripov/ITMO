package ru.itmo.parser.node;

import java.util.Collections;
import java.util.List;

public class Identifier extends AbstractNode {
    private final String name;

    public Identifier(String name) {
        this.name = name;
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getLabel() {
        return "ID";
    }

    @Override
    public int getNodesAmount() {
        return 1;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        Identifier that = (Identifier) o;
        return super.equals(o) && name.equals(that.name);
    }
}
