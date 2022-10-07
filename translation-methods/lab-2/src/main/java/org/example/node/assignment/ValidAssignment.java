package org.example.node.assignment;

import org.example.Token;
import org.example.node.AbstractNode;
import org.example.node.Node;
import org.example.node.value.Value;

import java.util.List;

public class ValidAssignment extends AbstractNode implements Assignment {
    private final Value<?> value;

    public ValidAssignment(Value<?> value) {
        this.value = value;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(Token.EQUAL, value);
    }

    @Override
    public int getCountNumber() {
        return 3;
    }

    public String toString() {
        return " = %s".formatted(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidAssignment assignment = (ValidAssignment) o;
        return value.equals(assignment.value);
    }
}
