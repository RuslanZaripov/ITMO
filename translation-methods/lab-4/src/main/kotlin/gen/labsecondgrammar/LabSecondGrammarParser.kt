package gen.labsecondgrammar

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

open class Leaf(val token: LabSecondGrammarToken, val value: String) : Tree(token.name)

class LabSecondGrammarParser(private val lexer: LabSecondGrammarLexer) {
    private var curToken: LabSecondGrammarToken = lexer.nextToken()
   
    private fun next() {
        curToken = lexer.nextToken()
    }
   
    fun parse(): ListContext {
        val ast = list()
        return when (curToken) {
            LabSecondGrammarToken.END -> ast
            else -> {
                println("Tree: \n$ast")
                throw Exception("Unexpected token: $curToken")
            }
        }
    }
    
    private fun check(token: LabSecondGrammarToken): LabSecondGrammarToken {
        val last = curToken
        if (last != token) {
            throw Exception("Unexpected token: $curToken")
        }
        next()
        return last
    }
   
	class ListContext(name: String /* no input attrs */) : Tree(name) {
		lateinit var stmt: StmtContext
		lateinit var listPrime: ListPrimeContext
		/* no return attrs */
	}

	class ListPrimeContext(name: String /* no input attrs */) : Tree(name) {
		lateinit var stmt: StmtContext
		lateinit var listPrime: ListPrimeContext
		/* context listPrime can be epsilon */
		/* no return attrs */
	}

	class StmtContext(name: String /* no input attrs */) : Tree(name) {
		lateinit var modifier: ModifierContext
		var ID: LabSecondGrammarToken? = null
		lateinit var type: TypeContext
		lateinit var assignment: AssignmentContext
		var SEMICOLON: LabSecondGrammarToken? = null
		/* no return attrs */
	}

	class ModifierContext(name: String /* no input attrs */) : Tree(name) {
		var VAR: LabSecondGrammarToken? = null
		var VAL: LabSecondGrammarToken? = null
		/* no return attrs */
	}

	class TypeContext(name: String /* no input attrs */) : Tree(name) {
		var COLON: LabSecondGrammarToken? = null
		var TYPE: LabSecondGrammarToken? = null
		/* no return attrs */
	}

	class AssignmentContext(name: String /* no input attrs */) : Tree(name) {
		var ASSIGN: LabSecondGrammarToken? = null
		lateinit var value: ValueContext
		/* context assignment can be epsilon */
		/* no return attrs */
	}

	class ValueContext(name: String /* no input attrs */) : Tree(name) {
		var INT: LabSecondGrammarToken? = null
		/* no return attrs */
	}
    
	private fun list(): ListContext {
	    val listLocalContext = ListContext("list" /* no input attrs */)
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAR, LabSecondGrammarToken.VAL -> {
				val stmt = stmt()
				listLocalContext.stmt = stmt
				/* no action performed on attributes for listLocalContext */
				listLocalContext.add(stmt)
		
				val listPrime = listPrime()
				listLocalContext.listPrime = listPrime
				/* no action performed on attributes for listLocalContext */
				listLocalContext.add(listPrime)
			}
	
	        else -> {
	            println("Tree: \n$listLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return listLocalContext
	}
	
	private fun listPrime(): ListPrimeContext {
	    val listPrimeLocalContext = ListPrimeContext("listPrime" /* no input attrs */)
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAR, LabSecondGrammarToken.VAL -> {
				val stmt = stmt()
				listPrimeLocalContext.stmt = stmt
				/* no action performed on attributes for listPrimeLocalContext */
				listPrimeLocalContext.add(stmt)
		
				val listPrime = listPrime()
				listPrimeLocalContext.listPrime = listPrime
				/* no action performed on attributes for listPrimeLocalContext */
				listPrimeLocalContext.add(listPrime)
			}
	
			LabSecondGrammarToken.END -> {
				/* no initialization performed for EPSILON */
				/* no context filling for EPSILON */
				/* no action performed on attributes for listPrimeLocalContext */
				listPrimeLocalContext.add(Leaf(LabSecondGrammarToken.EPSILON, "ε"))
			}
	
	        else -> {
	            println("Tree: \n$listPrimeLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return listPrimeLocalContext
	}
	
	private fun stmt(): StmtContext {
	    val stmtLocalContext = StmtContext("stmt" /* no input attrs */)
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAR, LabSecondGrammarToken.VAL -> {
				val modifier = modifier()
				stmtLocalContext.modifier = modifier
				/* no action performed on attributes for stmtLocalContext */
				stmtLocalContext.add(modifier)
		
				lastToken = check(LabSecondGrammarToken.ID)
				stmtLocalContext.ID = lastToken
				/* no action performed on attributes for stmtLocalContext */
				stmtLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val type = type()
				stmtLocalContext.type = type
				/* no action performed on attributes for stmtLocalContext */
				stmtLocalContext.add(type)
		
				val assignment = assignment()
				stmtLocalContext.assignment = assignment
				/* no action performed on attributes for stmtLocalContext */
				stmtLocalContext.add(assignment)
		
				lastToken = check(LabSecondGrammarToken.SEMICOLON)
				stmtLocalContext.SEMICOLON = lastToken
				/* no action performed on attributes for stmtLocalContext */
				stmtLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
	        else -> {
	            println("Tree: \n$stmtLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return stmtLocalContext
	}
	
	private fun modifier(): ModifierContext {
	    val modifierLocalContext = ModifierContext("modifier" /* no input attrs */)
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAR -> {
				lastToken = check(LabSecondGrammarToken.VAR)
				modifierLocalContext.VAR = lastToken
				/* no action performed on attributes for modifierLocalContext */
				modifierLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
			LabSecondGrammarToken.VAL -> {
				lastToken = check(LabSecondGrammarToken.VAL)
				modifierLocalContext.VAL = lastToken
				/* no action performed on attributes for modifierLocalContext */
				modifierLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
	        else -> {
	            println("Tree: \n$modifierLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return modifierLocalContext
	}
	
	private fun type(): TypeContext {
	    val typeLocalContext = TypeContext("type" /* no input attrs */)
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.COLON -> {
				lastToken = check(LabSecondGrammarToken.COLON)
				typeLocalContext.COLON = lastToken
				/* no action performed on attributes for typeLocalContext */
				typeLocalContext.add(Leaf(lastToken, lastToken.value))
		
				lastToken = check(LabSecondGrammarToken.TYPE)
				typeLocalContext.TYPE = lastToken
				/* no action performed on attributes for typeLocalContext */
				typeLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
	        else -> {
	            println("Tree: \n$typeLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return typeLocalContext
	}
	
	private fun assignment(): AssignmentContext {
	    val assignmentLocalContext = AssignmentContext("assignment" /* no input attrs */)
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.ASSIGN -> {
				lastToken = check(LabSecondGrammarToken.ASSIGN)
				assignmentLocalContext.ASSIGN = lastToken
				/* no action performed on attributes for assignmentLocalContext */
				assignmentLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val value = value()
				assignmentLocalContext.value = value
				/* no action performed on attributes for assignmentLocalContext */
				assignmentLocalContext.add(value)
			}
	
			LabSecondGrammarToken.SEMICOLON -> {
				/* no initialization performed for EPSILON */
				/* no context filling for EPSILON */
				/* no action performed on attributes for assignmentLocalContext */
				assignmentLocalContext.add(Leaf(LabSecondGrammarToken.EPSILON, "ε"))
			}
	
	        else -> {
	            println("Tree: \n$assignmentLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return assignmentLocalContext
	}
	
	private fun value(): ValueContext {
	    val valueLocalContext = ValueContext("value" /* no input attrs */)
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.INT -> {
				lastToken = check(LabSecondGrammarToken.INT)
				valueLocalContext.INT = lastToken
				/* no action performed on attributes for valueLocalContext */
				valueLocalContext.add(Leaf(lastToken, lastToken.value))
			}
	
	        else -> {
	            println("Tree: \n$valueLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return valueLocalContext
	}
	
}


