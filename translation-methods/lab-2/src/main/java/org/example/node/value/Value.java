package org.example.node.value;

import org.example.node.AbstractNode;
import org.example.node.Node;

import java.util.Collections;
import java.util.List;

public class Value<T> extends AbstractNode {
    private final T value;

    public Value(T value) {
        this.value = value;
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

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value<?> value1 = (Value<?>) o;
        return value.equals(value1.value);
    }

    public String toString() {
        return value.toString();
    }
}
