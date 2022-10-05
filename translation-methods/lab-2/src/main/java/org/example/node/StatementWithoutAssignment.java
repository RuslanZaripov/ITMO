package org.example.node;

public record StatementWithoutAssignment(Modifier modifier, Identifier id, Type type) implements Statement {
    @Override
    public String toString() {
        return "%s %s: %s;".formatted(modifier, id, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatementWithoutAssignment that = (StatementWithoutAssignment) o;
        return modifier.equals(that.modifier) && id.equals(that.id) && type.equals(that.type);
    }
}
