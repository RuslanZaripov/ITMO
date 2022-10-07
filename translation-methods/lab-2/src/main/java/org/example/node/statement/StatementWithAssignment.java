package org.example.node.statement;

import org.example.Token;
import org.example.node.*;
import org.example.node.type.Type;

import java.util.List;

public class StatementWithAssignment extends AbstractNode implements Statement {
    private final Modifier modifier;
    private final Identifier id;
    private final Type type;
    private final Assign assign;

    public StatementWithAssignment(Modifier modifier, Identifier id, Type type, Assign assign) {
        this.modifier = modifier;
        this.id = id;
        this.type = type;
        this.assign = assign;
    }

    @Override
    public String toString() {
        return "%s %s%s %s;".formatted(modifier, id, type, assign);
    }

    public List<Node> getChildren() {
        return List.of(modifier, id, type, assign, Token.SEMICOLON);
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
        StatementWithAssignment that = (StatementWithAssignment) o;
        return modifier.equals(that.modifier) && id.equals(that.id) && type.equals(that.type) && assign.equals(that.assign);
    }
}
