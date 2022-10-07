package org.example.node;

public abstract class AbstractNode implements Node {
    private int number;

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }
}
