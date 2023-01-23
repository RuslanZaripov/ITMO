package gen.test

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

open class Leaf(val token: TestToken, val value: String) : Tree(token.name)

class TestParser(private val lexer: TestLexer) {
    private var curToken: TestToken = lexer.nextToken()
   
    private fun next() {
        curToken = lexer.nextToken()
    }
   
    fun parse(): Tree {
        val ast = lex()
        return when (curToken) {
            TestToken.END -> ast
            else -> throw Exception("Unexpected token: $curToken")
        }
    }
    
    private fun check(token: TestToken): TestToken {
        val last = curToken
        if (last != token) {
            throw Exception("Unexpected token: $curToken")
        }
        next()
        return last
    }
   
	class LexContext(name: String) : Tree(name)
	class JokContext(name: String) : Tree(name)
    
	private fun lex(): Tree {
	    val lexLocalContext = LexContext("lex")
	    var lastToken: TestToken
    
	    when (curToken) {
			TestToken.ID -> {
				val jokContext = jok()
				lexLocalContext.add(jokContext)
				lastToken = check(TestToken.ID)
				lexLocalContext.add(Leaf(lastToken, lastToken.value!!))
			}
			TestToken.PIG -> {
				lastToken = check(TestToken.PIG)
				lexLocalContext.add(Leaf(lastToken, lastToken.value!!))
				val lexContext = lex()
				lexLocalContext.add(lexContext)
			}
	        else -> throw Exception("Unexpected token: $curToken")
	    }
	    return lexLocalContext
	}
	
	private fun jok(): Tree {
	    val jokLocalContext = JokContext("jok")
	    var lastToken: TestToken
    
	    when (curToken) {
			TestToken.ID -> {
				lastToken = check(TestToken.ID)
				jokLocalContext.add(Leaf(lastToken, lastToken.value!!))
			}
	        else -> throw Exception("Unexpected token: $curToken")
	    }
	    return jokLocalContext
	}
	
}
