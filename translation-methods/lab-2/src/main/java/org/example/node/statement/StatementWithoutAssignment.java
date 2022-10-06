package org.example.node.statement;

import org.example.Token;
import org.example.node.Identifier;
import org.example.node.Modifier;
import org.example.node.Node;
import org.example.node.type.Type;

import java.util.List;

public record StatementWithoutAssignment(Modifier modifier, Identifier id, Type type) implements Statement {
    private static int number;

    @Override
    public String toString() {
        return "%s %s: %s;".formatted(modifier, id, type);
    }

    public List<Node> getChildren() {
        return List.of(modifier, id, type, Token.SEMICOLON);
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
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        StatementWithoutAssignment.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatementWithoutAssignment that = (StatementWithoutAssignment) o;
        return modifier.equals(that.modifier) && id.equals(that.id) && type.equals(that.type);
    }
}
