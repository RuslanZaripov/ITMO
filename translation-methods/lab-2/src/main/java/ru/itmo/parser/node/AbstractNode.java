package ru.itmo.parser.node;

import ru.itmo.parser.Token;

import java.util.List;

public abstract class AbstractNode implements Node {
    private static final String EMPTY_STRING = "";
    private int number;

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(Token.EPS);
    }

    @Override
    public int getCountNumber() {
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
