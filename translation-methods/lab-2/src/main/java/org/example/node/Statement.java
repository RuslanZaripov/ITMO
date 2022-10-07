package org.example.node;

import org.example.Token;
import org.example.node.assignment.Assignment;
import org.example.node.type.Type;

import java.util.List;

public class Statement extends AbstractNode {
    private final Modifier modifier;
    private final Identifier id;
    private final Type type;
    private final Assignment assignment;

    public Statement(Modifier modifier, Identifier id, Type type, Assignment assignment) {
        this.modifier = modifier;
        this.id = id;
        this.type = type;
        this.assignment = assignment;
    }

    @Override
    public String toString() {
        return "%s %s%s%s;".formatted(modifier, id, type, assignment);
    }

    public List<Node> getChildren() {
        return List.of(modifier, id, type, assignment, Token.SEMICOLON);
    }

    @Override
    public String getLabel() {
        return "stat";
    }

    @Override
    public int getCountNumber() {
        return 12;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statement that = (Statement) o;
        return modifier.equals(that.modifier) && id.equals(that.id) && type.equals(that.type) && assignment.equals(that.assignment);
    }
}
