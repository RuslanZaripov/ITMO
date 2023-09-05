package expression;

import expression.mode.*;

public interface GenericExpression extends TripleExpression {
     <T extends Number> T evaluate(T x, T y, T z, Mode<T> mode);

     @Override
     default int evaluate(int x, int y, int z) {
          return evaluate(x, y, z, new ModeInteger());
     }
}