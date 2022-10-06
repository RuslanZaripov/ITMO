package org.example.node.statement;

import org.example.Token;
import org.example.node.Identifier;
import org.example.node.Modifier;
import org.example.node.Node;
import org.example.node.type.Type;
import org.example.node.value.Value;

import java.util.List;

public record StatementWithAssignment(Modifier modifier,
                                      Identifier id,
                                      Type type,
                                      Value<?> value) implements Statement {
    private static int number;

    @Override
    public String toString() {
        return "%s %s: %s = %s;".formatted(modifier, id, type, value);
    }

    public List<Node> getChildren() {
        return List.of(modifier, id, type, Token.EQUAL, value, Token.SEMICOLON);
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
        StatementWithAssignment.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatementWithAssignment that = (StatementWithAssignment) o;
        return modifier.equals(that.modifier) && id.equals(that.id) && type.equals(that.type) && value.equals(that.value);
    }
}
