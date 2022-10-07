package org.example.node.assignment;

import org.example.node.AbstractNode;

public class EmptyAssignment extends AbstractNode implements Assignment {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }
}
