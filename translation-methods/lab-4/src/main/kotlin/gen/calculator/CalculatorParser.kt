package gen.calculator

class Visualizer {
    private var index = 0

    fun visualize(tree: Tree): String {
        val sb = StringBuilder("digraph G {\n")
        traverse(tree, -1, 0, sb)
        sb.append("}")
        return sb.toString()
    }

    private fun traverse(tree: Tree, parentId: Int, currentId: Int, sb: StringBuilder) {
        sb.append("\t$currentId [label=${tree.name}]\n")
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
   
	class ExprContext(name: String) : Tree(name) {
		var term: TermContext? = null
		var exprPrime: ExprPrimeContext? = null
		var res: Int? = null
	}
	class ExprPrimeContext(name: String, val acc: Int) : Tree(name) {
		var exprOp: ExprOpContext? = null
		var term: TermContext? = null
		var exprPrime: ExprPrimeContext? = null
	
		var res: Int? = null
	}
	class TermContext(name: String) : Tree(name) {
		var factor: FactorContext? = null
		var termPrime: TermPrimeContext? = null
		var res: Int? = null
	}
	class TermPrimeContext(name: String, val acc: Int) : Tree(name) {
		var termOp: TermOpContext? = null
		var factor: FactorContext? = null
		var termPrime: TermPrimeContext? = null
	
		var res: Int? = null
	}
	class FactorContext(name: String) : Tree(name) {
		var LPAREN: CalculatorToken? = null
		var expr: ExprContext? = null
		var RPAREN: CalculatorToken? = null
		var num: CalculatorToken? = null
		var exprOp: ExprOpContext? = null
		var factor: FactorContext? = null
		var res: Int? = null
	}
	class ExprOpContext(name: String) : Tree(name) {
		var PLUS: CalculatorToken? = null
		var MINUS: CalculatorToken? = null
	
	}
	class TermOpContext(name: String) : Tree(name) {
		var TIMES: CalculatorToken? = null
		var DIVIDE: CalculatorToken? = null
	
	}
    
	private fun expr(): ExprContext {
	    val exprLocalContext = ExprContext("expr")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.NUM, CalculatorToken.LPAREN, CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val term = term()
				exprLocalContext.term = term
				exprLocalContext.add(term)
		
				val exprPrime = exprPrime(term.res!!)
				exprLocalContext.exprPrime = exprPrime
				exprLocalContext.res = exprLocalContext.exprPrime!!.res
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
			CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val exprOp = exprOp()
				exprPrimeLocalContext.exprOp = exprOp
				exprPrimeLocalContext.add(exprOp)
		
				val term = term()
				exprPrimeLocalContext.term = term
				if (exprPrimeLocalContext.exprOp!!.PLUS != null)
				    exprPrimeLocalContext.res = exprPrimeLocalContext.acc + exprPrimeLocalContext.term!!.res!!
				else if (exprPrimeLocalContext.exprOp!!.MINUS != null)
				    exprPrimeLocalContext.res = exprPrimeLocalContext.acc - exprPrimeLocalContext.term!!.res!!
				exprPrimeLocalContext.add(term)
		
				val exprPrime = exprPrime(exprPrimeLocalContext.res!!)
				exprPrimeLocalContext.exprPrime = exprPrime
				exprPrimeLocalContext.res = exprPrimeLocalContext.exprPrime!!.res
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
			CalculatorToken.NUM, CalculatorToken.LPAREN, CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val factor = factor()
				termLocalContext.factor = factor
				termLocalContext.add(factor)
		
				val termPrime = termPrime(factor.res!!)
				termLocalContext.termPrime = termPrime
				termLocalContext.res = termLocalContext.termPrime!!.res
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
			CalculatorToken.TIMES, CalculatorToken.DIVIDE -> {
				val termOp = termOp()
				termPrimeLocalContext.termOp = termOp
				termPrimeLocalContext.add(termOp)
		
				val factor = factor()
				termPrimeLocalContext.factor = factor
				if (termPrimeLocalContext.termOp!!.TIMES != null)
				   termPrimeLocalContext.res = termPrimeLocalContext.acc * termPrimeLocalContext.factor!!.res!!
				else if (termPrimeLocalContext.termOp!!.DIVIDE != null)
				   termPrimeLocalContext.res = termPrimeLocalContext.acc / termPrimeLocalContext.factor!!.res!!
				termPrimeLocalContext.add(factor)
		
				val termPrime = termPrime(termPrimeLocalContext.res!!)
				termPrimeLocalContext.termPrime = termPrime
				termPrimeLocalContext.res = termPrimeLocalContext.termPrime!!.res
				termPrimeLocalContext.add(termPrime)
		
			}
			CalculatorToken.END, CalculatorToken.RPAREN, CalculatorToken.PLUS, CalculatorToken.MINUS -> {
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
				factorLocalContext.res = factorLocalContext.expr!!.res
				factorLocalContext.add(expr)
		
				lastToken = check(CalculatorToken.RPAREN)
				factorLocalContext.RPAREN = lastToken
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
		
			}
			CalculatorToken.NUM -> {
				lastToken = check(CalculatorToken.NUM)
				factorLocalContext.num = lastToken
				factorLocalContext.res = factorLocalContext.num!!.value.toInt()
				factorLocalContext.add(Leaf(lastToken, lastToken.value))
		
			}
			CalculatorToken.PLUS, CalculatorToken.MINUS -> {
				val exprOp = exprOp()
				factorLocalContext.exprOp = exprOp
				factorLocalContext.add(exprOp)
		
				val factor = factor()
				factorLocalContext.factor = factor
				if (factorLocalContext.exprOp!!.PLUS != null)
				    factorLocalContext.res = factorLocalContext.factor!!.res!!
				else if (factorLocalContext.exprOp!!.MINUS != null)
				    factorLocalContext.res = -factorLocalContext.factor!!.res!!
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
	    val exprOpLocalContext = ExprOpContext("exprOp")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.PLUS -> {
				lastToken = check(CalculatorToken.PLUS)
				exprOpLocalContext.PLUS = lastToken
				exprOpLocalContext.add(Leaf(lastToken, lastToken.value))
		
			}
			CalculatorToken.MINUS -> {
				lastToken = check(CalculatorToken.MINUS)
				exprOpLocalContext.MINUS = lastToken
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
	    val termOpLocalContext = TermOpContext("termOp")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.TIMES -> {
				lastToken = check(CalculatorToken.TIMES)
				termOpLocalContext.TIMES = lastToken
				termOpLocalContext.add(Leaf(lastToken, lastToken.value))
		
			}
			CalculatorToken.DIVIDE -> {
				lastToken = check(CalculatorToken.DIVIDE)
				termOpLocalContext.DIVIDE = lastToken
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
