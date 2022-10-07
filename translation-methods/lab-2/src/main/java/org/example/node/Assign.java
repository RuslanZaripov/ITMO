package org.example.node;

import org.example.Token;
import org.example.node.value.Value;

import java.util.List;

public class Assign extends AbstractNode {
    private final Value<?> value;

    public Assign(Value<?> value) {
        this.value = value;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(Token.EQUAL, value);
    }

    @Override
    public String getLabel() {
        return "assign";
    }

    @Override
    public int getCountNumber() {
        return 3;
    }

    public String toString() {
        return "= %s".formatted(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assign assign = (Assign) o;
        return value.equals(assign.value);
    }
}
