package gen.calculator

open class Tree(open val name: String, private val children: MutableList<Tree> = mutableListOf()) {
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
                next.print(builder, "$childrenPrefix├── ", "$childrenPrefix│   ")
            } else {
                next.print(builder, "$childrenPrefix└── ", "$childrenPrefix    ")
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
   
	class ExprContext(name: String) : Tree(name) {
		lateinit var term: TermContext
		lateinit var exprPrime: ExprPrimeContext
		var res: Int? = null
	}
	class ExprPrimeContext(name: String, val acc: Int) : Tree(name) {
		lateinit var PLUS: CalculatorToken
		lateinit var term: TermContext
		lateinit var exprPrime: ExprPrimeContext
		lateinit var EPSILON: CalculatorToken
		var res: Int? = null
	}
	class TermContext(name: String) : Tree(name) {
		lateinit var factor: FactorContext
		lateinit var termPrime: TermPrimeContext
		var res: Int? = null
	}
	class TermPrimeContext(name: String, val acc: Int) : Tree(name) {
		lateinit var TIMES: CalculatorToken
		lateinit var factor: FactorContext
		lateinit var termPrime: TermPrimeContext
		lateinit var EPSILON: CalculatorToken
		var res: Int? = null
	}
	class FactorContext(name: String) : Tree(name) {
		lateinit var LPAREN: CalculatorToken
		lateinit var expr: ExprContext
		lateinit var RPAREN: CalculatorToken
		lateinit var NUM: CalculatorToken
		var res: Int? = null
	}
    
	private fun expr(): ExprContext {
	    val exprLocalContext = ExprContext("expr")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.NUM, CalculatorToken.LPAREN -> {
				val term = term()
				exprLocalContext.term = term
				exprLocalContext.add(term)
		
				val exprPrime = exprPrime(term.res!!)
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
	
	private fun exprPrime(acc: Int): ExprPrimeContext {
	    val exprPrimeLocalContext = ExprPrimeContext("exprPrime", acc)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.PLUS -> {
				lastToken = check(CalculatorToken.PLUS)
				exprPrimeLocalContext.PLUS = lastToken
				exprPrimeLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val term = term()
				exprPrimeLocalContext.term = term
				exprPrimeLocalContext.res = exprPrimeLocalContext.acc + exprPrimeLocalContext.term.res!!
				exprPrimeLocalContext.add(term)
		
				val exprPrime = exprPrime(exprPrimeLocalContext.res!!)
				exprPrimeLocalContext.exprPrime = exprPrime
				exprPrimeLocalContext.res = exprPrimeLocalContext.exprPrime.res
				exprPrimeLocalContext.add(exprPrime)
		
			}
			CalculatorToken.END, CalculatorToken.RPAREN -> {
				exprPrimeLocalContext.res = exprPrimeLocalContext.acc
			}
	        else -> {
	            println("Tree: \n$exprPrimeLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return exprPrimeLocalContext
	}
	
	private fun term(): TermContext {
	    val termLocalContext = TermContext("term")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.NUM, CalculatorToken.LPAREN -> {
				val factor = factor()
				termLocalContext.factor = factor
				termLocalContext.add(factor)
		
				val termPrime = termPrime(factor.res!!)
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
	
	private fun termPrime(acc: Int): TermPrimeContext {
	    val termPrimeLocalContext = TermPrimeContext("termPrime", acc)
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.TIMES -> {
				lastToken = check(CalculatorToken.TIMES)
				termPrimeLocalContext.TIMES = lastToken
				termPrimeLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val factor = factor()
				termPrimeLocalContext.factor = factor
				termPrimeLocalContext.res = termPrimeLocalContext.acc * termPrimeLocalContext.factor.res!!
				termPrimeLocalContext.add(factor)
		
				val termPrime = termPrime(termPrimeLocalContext.res!!)
				termPrimeLocalContext.termPrime = termPrime
				termPrimeLocalContext.res = termPrimeLocalContext.termPrime.res
				termPrimeLocalContext.add(termPrime)
		
			}
			CalculatorToken.END, CalculatorToken.RPAREN, CalculatorToken.PLUS -> {
				termPrimeLocalContext.res = termPrimeLocalContext.acc
			}
	        else -> {
	            println("Tree: \n$termPrimeLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return termPrimeLocalContext
	}
	
	private fun factor(): FactorContext {
	    val factorLocalContext = FactorContext("factor")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.LPAREN -> {
				lastToken = check(CalculatorToken.LPAREN)
				factorLocalContext.LPAREN = lastToken
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val expr = expr()
				factorLocalContext.expr = expr
				factorLocalContext.res = factorLocalContext.expr.res
				factorLocalContext.add(expr)
		
				lastToken = check(CalculatorToken.RPAREN)
				factorLocalContext.RPAREN = lastToken
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
		
			}
			CalculatorToken.NUM -> {
				lastToken = check(CalculatorToken.NUM)
				factorLocalContext.NUM = lastToken
				factorLocalContext.res = factorLocalContext.NUM.value.toInt()
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
		
			}
	        else -> {
	            println("Tree: \n$factorLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return factorLocalContext
	}
	
}
