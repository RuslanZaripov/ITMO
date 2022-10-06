package org.example.node.value;

import org.example.node.Node;

import java.util.Collections;
import java.util.List;

public class Value<T> implements Node {
    private final T value;
    private int number;

    public Value(T value) {
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }

    @Override
    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String getLabel() {
        return "VALUE";
    }

    @Override
    public int getCountNumber() {
        return 1;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value<?> value1 = (Value<?>) o;
        return value.equals(value1.value);
    }
}
