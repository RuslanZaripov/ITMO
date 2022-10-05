package org.example.node.value;

public class StringValue extends Value<String> {
    public StringValue(String value) {
        super(value);
    }
    
    public String toString() {
        return "\"%s\"".formatted(value());
    }
}
