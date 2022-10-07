package org.example.node;

import org.example.Token;

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
}
