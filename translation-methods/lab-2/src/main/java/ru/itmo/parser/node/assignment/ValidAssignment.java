package ru.itmo.parser.node.assignment;

import ru.itmo.parser.Token;
import ru.itmo.parser.node.AbstractNode;
import ru.itmo.parser.node.Node;
import ru.itmo.parser.node.value.Value;

import java.util.List;

public class ValidAssignment extends AbstractNode implements Assignment {
    private final Value<?> value;

    public ValidAssignment(final Value<?> value) {
        this.value = value;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(Token.EQUAL, value);
    }

    @Override
    public int getNodesAmount() {
        return 3;
    }

    public String toString() {
        return " = %s".formatted(value);
    }

    @Override
    public boolean equals(final Object o) {
        ValidAssignment assignment = (ValidAssignment) o;
        return super.equals(o) && value.equals(assignment.value);
    }
}
