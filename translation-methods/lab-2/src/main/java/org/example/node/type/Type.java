package org.example.node.type;

import org.example.node.Node;

public record Type(KotlinType type) implements Node {

    public String toString() {
        return type.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type that = (Type) o;
        return type == that.type;
    }
}
