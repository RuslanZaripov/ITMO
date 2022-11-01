package ru.itmo.parser.node.value;

import ru.itmo.parser.node.AbstractNode;
import ru.itmo.parser.node.Node;

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
    public int getNodesAmount() {
        return 1;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        Value<?> that = (Value<?>) o;
        return super.equals(o) && value.equals(that.value);
    }

    public String toString() {
        return value.toString();
    }
}
