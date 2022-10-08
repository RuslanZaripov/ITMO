package ru.itmo.parser.node.type;

import ru.itmo.parser.Token;
import ru.itmo.parser.node.AbstractNode;
import ru.itmo.parser.node.Node;

import java.util.List;

public class Type extends AbstractNode {
    private final KotlinType type;

    public Type(final KotlinType type) {
        this.type = type;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(Token.COLON, Token.TYPE);
    }

    @Override
    public String getLabel() {
        return "type";
    }

    @Override
    public int getCountNumber() {
        return 3;
    }

    public KotlinType getType() {
        return type;
    }

    public String toString() {
        return ": %s".formatted(type.getName());
    }

    @Override
    public boolean equals(final Object o) {
        Type that = (Type) o;
        return super.equals(o) && type.equals(that.type);
    }
}
