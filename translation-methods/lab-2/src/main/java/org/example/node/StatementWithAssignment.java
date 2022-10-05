package org.example.node;

public record StatementWithAssignment(Modifier modifier, Identifier id, Type type, Value value) implements Statement {
    @Override
    public String toString() {
        return "%s %s: %s = %s;".formatted(modifier, id, type, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatementWithAssignment that = (StatementWithAssignment) o;
        return modifier.equals(that.modifier) && id.equals(that.id) && type.equals(that.type) && value.equals(that.value);
    }
}
