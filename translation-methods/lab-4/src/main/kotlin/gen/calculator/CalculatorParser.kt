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
   
    fun parse(): Tree {
        val ast = e()
        return when (curToken) {
            CalculatorToken.END -> ast
            else -> throw Exception("Unexpected token: $curToken")
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
   
	class EContext(name: String) : Tree(name)
	class EfContext(name: String) : Tree(name)
	class TContext(name: String) : Tree(name)
	class TfContext(name: String) : Tree(name)
	class FContext(name: String) : Tree(name)
    
	private fun e(): Tree {
	    val eLocalContext = EContext("e")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.LPAREN, CalculatorToken.ID -> {
				val tContext = t()
				eLocalContext.add(tContext)
				val efContext = ef()
				eLocalContext.add(efContext)
			}
	        else -> throw Exception("Unexpected token: $curToken")
	    }
	    return eLocalContext
	}
	
	private fun ef(): Tree {
	    val efLocalContext = EfContext("ef")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.PLUS -> {
				lastToken = check(CalculatorToken.PLUS)
				efLocalContext.add(Leaf(lastToken, lastToken.value!!))
				val tContext = t()
				efLocalContext.add(tContext)
				val eContext = e()
				efLocalContext.add(eContext)
			}
	        else -> throw Exception("Unexpected token: $curToken")
	    }
	    return efLocalContext
	}
	
	private fun t(): Tree {
	    val tLocalContext = TContext("t")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.LPAREN, CalculatorToken.ID -> {
				val fContext = f()
				tLocalContext.add(fContext)
				val tfContext = tf()
				tLocalContext.add(tfContext)
			}
	        else -> throw Exception("Unexpected token: $curToken")
	    }
	    return tLocalContext
	}
	
	private fun tf(): Tree {
	    val tfLocalContext = TfContext("tf")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.TIMES -> {
				lastToken = check(CalculatorToken.TIMES)
				tfLocalContext.add(Leaf(lastToken, lastToken.value!!))
				val fContext = f()
				tfLocalContext.add(fContext)
				val tfContext = tf()
				tfLocalContext.add(tfContext)
			}
	        else -> throw Exception("Unexpected token: $curToken")
	    }
	    return tfLocalContext
	}
	
	private fun f(): Tree {
	    val fLocalContext = FContext("f")
	    var lastToken: CalculatorToken
    
	    when (curToken) {
			CalculatorToken.LPAREN -> {
				lastToken = check(CalculatorToken.LPAREN)
				fLocalContext.add(Leaf(lastToken, lastToken.value!!))
				val eContext = e()
				fLocalContext.add(eContext)
				lastToken = check(CalculatorToken.RPAREN)
				fLocalContext.add(Leaf(lastToken, lastToken.value!!))
			}
			CalculatorToken.ID -> {
				lastToken = check(CalculatorToken.ID)
				fLocalContext.add(Leaf(lastToken, lastToken.value!!))
			}
	        else -> throw Exception("Unexpected token: $curToken")
	    }
	    return fLocalContext
	}
	
}
