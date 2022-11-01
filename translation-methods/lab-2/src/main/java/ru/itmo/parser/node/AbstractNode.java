package ru.itmo.parser.node;

import ru.itmo.parser.Token;

import java.util.List;

public abstract class AbstractNode implements Node {
    private static final String EMPTY_STRING = "";
    private int id;

    @Override
    public int getNodeId() {
        return id;
    }

    @Override
    public void setNodeId(int number) {
        this.id = number;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(Token.EPS);
    }

    @Override
    public int getNodesAmount() {
        return 2;
    }

    @Override
    public String toString() {
        return EMPTY_STRING;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass();
    }
}
