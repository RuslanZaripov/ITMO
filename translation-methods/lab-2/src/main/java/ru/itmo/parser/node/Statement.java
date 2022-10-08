package ru.itmo.parser.node;

import ru.itmo.parser.Token;
import ru.itmo.parser.node.assignment.Assignment;
import ru.itmo.parser.node.type.Type;

import java.util.List;

public class Statement extends AbstractNode {
    private final Modifier modifier;
    private final Identifier id;
    private final Type type;
    private final Assignment assignment;

    public Statement(final Modifier modifier, final Identifier id, final Type type, final Assignment assignment) {
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
    public boolean equals(final Object o) {
        Statement that = (Statement) o;
        return super.equals(o)
                && modifier.equals(that.modifier)
                && id.equals(that.id)
                && type.equals(that.type)
                && assignment.equals(that.assignment);
    }
}
