package gen.calculator

import kotlin.properties.Delegates

class Visualizer {
    private var index = 0

    fun visualize(tree: Tree): String {
        val sb = StringBuilder("digraph G {\n")
        traverse(tree, -1, 0, sb)
        sb.append("}")
        return sb.toString()
    }

    private fun traverse(tree: Tree, parentId: Int, currentId: Int, sb: StringBuilder) {
        sb.append("\t$currentId [${formatNode(tree)}]\n")
        if (parentId != -1) {
            sb.append("\t$parentId -> $currentId\n")
        }
        if (tree.children.isNotEmpty()) {
            tree.children.forEach { child ->
                index += 1
                traverse(child, currentId, index, sb)
            }
        } else {
            index += 1
        }
    }
    
    private fun formatNode(tree: Tree): String {
        return when(tree) {
            is Leaf -> "label=\"${tree.value}\", style=filled, color=green"
            else -> "label=\"${tree.name}\", color=brown"
        }
    }
}

open class Tree(open val name: String, val children: MutableList<Tree> = mutableListOf()) {
    fun add(tree: Tree) {
        children.add(tree)
    }
    
    override fun toString(): String {
        val builder = StringBuilder()
        print(builder, "", "")
        return builder.toString()
    }  

    private fun print(builder: StringBuilder, prefix: String, childrenPrefix: String) {
        builder.append(prefix)
        builder.append(name)
        builder.append("\n")
        val it = children.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (it.hasNext()) {
                next.print(builder, "$childrenPrefix|-- ", "$childrenPrefix|   ")
            } else {
                next.print(builder, "$childrenPrefix|__ ", "$childrenPrefix    ")
            }
        }
    }
}

open class Leaf(val token: CalculatorToken, val value: String) : Tree(token.name)

class CalculatorParser(private val lexer: CalculatorLexer) {
    private var curToken: CalculatorToken = lexer.nextToken()
   
    private fun next() {
        curToken = lexer.nextToken()
    }
   
    fun parse(): ExprContext {
        val ast = expr()
        return when (curToken) {
            CalculatorToken.END -> ast
            else -> {
                println("Tree: \n$ast")
                throw Exception("Unexpected token: $curToken")
            }
        }
    }
    
    private fun check(token: CalculatorToken): CalculatorToken {
        val last = curToken
        if (last != token) {
            throw Exception("Unexpected token: $curToken")
        }
        next()
        return last
    }
   
	class ExprContext(name: String /* no input attrs */) : Tree(name) {
		lateinit var term: TermContext
		lateinit var exprPrime: ExprPrimeContext
		var res by Delegates.notNull<Rational>()
	}

	class ExprPrimeContext(name: String, val acc: Rational) : Tree(name) {
		lateinit var exprOp: ExprOpContext
		lateinit var term: TermContext
		lateinit var exprPrime: ExprPrimeContext
		/* context exprPrime can be epsilon */
		var res by Delegates.notNull<Rational>()
	}

	class TermContext(name: String /* no input attrs */) : Tree(name) {
		lateinit var factor: FactorContext
		lateinit var termPrime: TermPrimeContext
		var res by Delegates.notNull<Rational>()
	}

	class TermPrimeContext(name: String, val acc: Rational) : Tree(name) {
		lateinit var termOp: TermOpContext
		lateinit var factor: FactorContext
		lateinit var termPrime: TermPrimeContext
		/* context termPrime can be epsilon */
		var res by Delegates.notNull<Rational>()
	}

	class FactorContext(name: String /* no input attrs */) : Tree(name) {
		var LPAREN: CalculatorToken? = null
		lateinit var expr: ExprContext
		var RPAREN: CalculatorToken? = null
		lateinit var num: CalculatorToken
		lateinit var exprOp: ExprOpContext
		lateinit var factor: FactorContext
		var res by Delegates.notNull<Rational>()
	}

	class ExprOpContext(name: String /* no input attrs */) : Tree(name) {
		var PLUS: CalculatorToken? = null
		var MINUS: CalculatorToken? = null
		/* no return attrs */
	}

	class TermOpContext(name: String /* no input attrs */) : Tree(name) {
		var TIMES: CalculatorToken? = null
		var DIVIDE: CalculatorToken? = null
		/* no return attrs */
	}
    
	private fun expr(): ExprContext {
	    val exprLocalContext = ExprContext("expr" /* no input attrs */)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.LPAREN, CalculatorToken.NUM, CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val term = term()
				exprLocalContext.term = term
				/* no action performed on attributes for exprLocalContext */
				exprLocalContext.add(term)
		
				val exprPrime = exprPrime(term.res)
				exprLocalContext.exprPrime = exprPrime
				exprLocalContext.res = exprLocalContext.exprPrime.res
				exprLocalContext.add(exprPrime)
			}
	
	        else -> {
	            println("Tree: \n$exprLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return exprLocalContext
	}
	
	private fun exprPrime(acc: Rational): ExprPrimeContext {
	    val exprPrimeLocalContext = ExprPrimeContext("exprPrime", acc)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val exprOp = exprOp()
				exprPrimeLocalContext.exprOp = exprOp
				/* no action performed on attributes for exprPrimeLocalContext */
				exprPrimeLocalContext.add(exprOp)
		
				val term = term()
				exprPrimeLocalContext.term = term
				if (exprPrimeLocalContext.exprOp.PLUS != null)
				    exprPrimeLocalContext.res = exprPrimeLocalContext.acc + exprPrimeLocalContext.term.res
				else if (exprPrimeLocalContext.exprOp.MINUS != null)
				    exprPrimeLocalContext.res = exprPrimeLocalContext.acc - exprPrimeLocalContext.term.res
				exprPrimeLocalContext.add(term)
		
				val exprPrime = exprPrime(exprPrimeLocalContext.res)
				exprPrimeLocalContext.exprPrime = exprPrime
				exprPrimeLocalContext.res = exprPrimeLocalContext.exprPrime.res
				exprPrimeLocalContext.add(exprPrime)
			}
	
			CalculatorToken.RPAREN, CalculatorToken.END -> {
				exprPrimeLocalContext.res = exprPrimeLocalContext.acc
				exprPrimeLocalContext.add(Leaf(CalculatorToken.EPSILON, "ε"))
			}
	
	        else -> {
	            println("Tree: \n$exprPrimeLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return exprPrimeLocalContext
	}
	
	private fun term(): TermContext {
	    val termLocalContext = TermContext("term" /* no input attrs */)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.LPAREN, CalculatorToken.NUM, CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val factor = factor()
				termLocalContext.factor = factor
				/* no action performed on attributes for termLocalContext */
				termLocalContext.add(factor)
		
				val termPrime = termPrime(factor.res)
				termLocalContext.termPrime = termPrime
				termLocalContext.res = termLocalContext.termPrime.res
				termLocalContext.add(termPrime)
			}
	
	        else -> {
	            println("Tree: \n$termLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return termLocalContext
	}
	
	private fun termPrime(acc: Rational): TermPrimeContext {
	    val termPrimeLocalContext = TermPrimeContext("termPrime", acc)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.DIVIDE, CalculatorToken.TIMES -> {
				val termOp = termOp()
				termPrimeLocalContext.termOp = termOp
				/* no action performed on attributes for termPrimeLocalContext */
				termPrimeLocalContext.add(termOp)
		
				val factor = factor()
				termPrimeLocalContext.factor = factor
				if (termPrimeLocalContext.termOp.TIMES != null)
				    termPrimeLocalContext.res = termPrimeLocalContext.acc * termPrimeLocalContext.factor.res
				else if (termPrimeLocalContext.termOp.DIVIDE != null)
				    termPrimeLocalContext.res = termPrimeLocalContext.acc / termPrimeLocalContext.factor.res
				termPrimeLocalContext.add(factor)
		
				val termPrime = termPrime(termPrimeLocalContext.res)
				termPrimeLocalContext.termPrime = termPrime
				termPrimeLocalContext.res = termPrimeLocalContext.termPrime.res
				termPrimeLocalContext.add(termPrime)
			}
	
			CalculatorToken.END, CalculatorToken.RPAREN, CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				termPrimeLocalContext.res = termPrimeLocalContext.acc
				termPrimeLocalContext.add(Leaf(CalculatorToken.EPSILON, "ε"))
			}
	
	        else -> {
	            println("Tree: \n$termPrimeLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return termPrimeLocalContext
	}
	
	private fun factor(): FactorContext {
	    val factorLocalContext = FactorContext("factor" /* no input attrs */)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.LPAREN -> {
				lastToken = check(CalculatorToken.LPAREN)
				factorLocalContext.LPAREN = lastToken
				/* no action performed on attributes for factorLocalContext */
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val expr = expr()
				factorLocalContext.expr = expr
				factorLocalContext.res = factorLocalContext.expr.res
				factorLocalContext.add(expr)
		
				lastToken = check(CalculatorToken.RPAREN)
				factorLocalContext.RPAREN = lastToken
				/* no action performed on attributes for factorLocalContext */
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
			CalculatorToken.NUM -> {
				lastToken = check(CalculatorToken.NUM)
				factorLocalContext.num = lastToken
				factorLocalContext.res = valueOf(factorLocalContext.num.value)
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
			CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val exprOp = exprOp()
				factorLocalContext.exprOp = exprOp
				/* no action performed on attributes for factorLocalContext */
				factorLocalContext.add(exprOp)
		
				val factor = factor()
				factorLocalContext.factor = factor
				if (factorLocalContext.exprOp.PLUS != null)
				    factorLocalContext.res = factorLocalContext.factor.res
				else if (factorLocalContext.exprOp.MINUS != null)
				    factorLocalContext.res = -factorLocalContext.factor.res
				factorLocalContext.add(factor)
			}
	
	        else -> {
	            println("Tree: \n$factorLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return factorLocalContext
	}
	
	private fun exprOp(): ExprOpContext {
	    val exprOpLocalContext = ExprOpContext("exprOp" /* no input attrs */)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.PLUS -> {
				lastToken = check(CalculatorToken.PLUS)
				exprOpLocalContext.PLUS = lastToken
				/* no action performed on attributes for exprOpLocalContext */
				exprOpLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
			CalculatorToken.MINUS -> {
				lastToken = check(CalculatorToken.MINUS)
				exprOpLocalContext.MINUS = lastToken
				/* no action performed on attributes for exprOpLocalContext */
				exprOpLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
	        else -> {
	            println("Tree: \n$exprOpLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return exprOpLocalContext
	}
	
	private fun termOp(): TermOpContext {
	    val termOpLocalContext = TermOpContext("termOp" /* no input attrs */)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.TIMES -> {
				lastToken = check(CalculatorToken.TIMES)
				termOpLocalContext.TIMES = lastToken
				/* no action performed on attributes for termOpLocalContext */
				termOpLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
			CalculatorToken.DIVIDE -> {
				lastToken = check(CalculatorToken.DIVIDE)
				termOpLocalContext.DIVIDE = lastToken
				/* no action performed on attributes for termOpLocalContext */
				termOpLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
	        else -> {
	            println("Tree: \n$termOpLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return termOpLocalContext
	}
	
}

class Rational(val numerator: Int, val denominator: Int = 1) {
    operator fun plus(rhs: Rational) =
        numerator.times(rhs.denominator).plus(denominator.times(rhs.numerator))
            .divBy(denominator.times(rhs.denominator))

    operator fun minus(rhs: Rational) = this.plus(rhs.unaryMinus())

    operator fun times(rhs: Rational) = numerator.times(rhs.numerator).divBy(denominator.times(rhs.denominator))

    operator fun div(rhs: Rational) = this.times(Rational(rhs.denominator, rhs.numerator))

    operator fun unaryMinus() = Rational(-numerator, denominator)

    override fun toString(): String {
        val r = simplify(this)
        return "${r.numerator}/${r.denominator}"
    }
}

fun String.toRational(): Rational {
    val number = split("/")
    return when (number.size) {
        1 -> Rational(number[0].toInt())
        else -> Rational(number[0].toInt(), number[1].toInt())
    }
}

infix fun Int.divBy(rhs: Int) = Rational(this, rhs)

private fun valueOf(value: String) = value.toRational()

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun simplify(r: Rational): Rational {
    val gcd = kotlin.math.abs(gcd(r.numerator, r.denominator))
    return Rational(r.numerator.div(gcd), r.denominator.div(gcd))
}

