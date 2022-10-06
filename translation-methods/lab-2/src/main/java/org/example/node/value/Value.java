package org.example.node.value;

import org.example.node.Node;

public class Value<T> implements Node {
    private final T value;

    public Value(T value) {
        this.value = value;
    }

    public String toString() {
        return value.toString();
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
