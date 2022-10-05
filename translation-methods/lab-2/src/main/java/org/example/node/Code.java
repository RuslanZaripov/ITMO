package org.example.node;

import org.example.node.statement.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Code implements Node {
    List<Statement> code;

    public Code(Statement statement) {
        code = new ArrayList<>();
        code.add(statement);
    }

    public Code(Statement... statements) {
        code = new ArrayList<>();
        code.addAll(Arrays.asList(statements));
    }

    public Code(Statement head, Code tail) {
        code = new ArrayList<>();
        code.add(head);
        code.addAll(tail.code);
    }

    @Override
    public String toString() {
        return code.stream().map(Node::toString).collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Code that = (Code) o;
        return code.equals(that.code);
    }
}
