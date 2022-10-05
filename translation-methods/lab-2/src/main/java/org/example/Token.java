package org.example;

// (VAR | VAL) ID COLON TYPE (EQUAL VALUE | eps) SEMICOLON END
public enum Token {
    VAR("var"),
    VAL("val"),
    COLON(":"),
    EQUAL("="),
    SEMICOLON(";"),
    TYPE("Int|String"),
    VALUE("-?[0-9]+"),
    ID("[a-zA-Z_][a-zA-Z0-9_]*"),
    EOF("$");

    private final String regex;

    Token(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }
}
