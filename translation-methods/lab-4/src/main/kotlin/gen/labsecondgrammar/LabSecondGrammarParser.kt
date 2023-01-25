package gen.labsecondgrammar

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
   
	class ListContext(name: String) : Tree(name) {
		var stmt: StmtContext? = null
		var listPrime: ListPrimeContext? = null
	
	}
	class ListPrimeContext(name: String) : Tree(name) {
		var stmt: StmtContext? = null
		var listPrime: ListPrimeContext? = null
	
	
	}
	class StmtContext(name: String) : Tree(name) {
		var modifier: ModifierContext? = null
		var ID: LabSecondGrammarToken? = null
		var type: TypeContext? = null
		var assignment: AssignmentContext? = null
		var SEMICOLON: LabSecondGrammarToken? = null
	
	}
	class ModifierContext(name: String) : Tree(name) {
		var VAR: LabSecondGrammarToken? = null
		var VAL: LabSecondGrammarToken? = null
	
	}
	class TypeContext(name: String) : Tree(name) {
		var COLON: LabSecondGrammarToken? = null
		var TYPE: LabSecondGrammarToken? = null
	
	}
	class AssignmentContext(name: String) : Tree(name) {
		var ASSIGN: LabSecondGrammarToken? = null
		var value: ValueContext? = null
	
	
	}
	class ValueContext(name: String) : Tree(name) {
		var INT: LabSecondGrammarToken? = null
	
	}
    
	private fun list(): ListContext {
	    val listLocalContext = ListContext("list")
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAL, LabSecondGrammarToken.VAR -> {
				val stmt = stmt()
				listLocalContext.stmt = stmt
				listLocalContext.add(stmt)
		
				val listPrime = listPrime()
				listLocalContext.listPrime = listPrime
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
	    val listPrimeLocalContext = ListPrimeContext("listPrime")
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAL, LabSecondGrammarToken.VAR -> {
				val stmt = stmt()
				listPrimeLocalContext.stmt = stmt
				listPrimeLocalContext.add(stmt)
		
				val listPrime = listPrime()
				listPrimeLocalContext.listPrime = listPrime
				listPrimeLocalContext.add(listPrime)
		
			}
			LabSecondGrammarToken.END -> {
				// do nothing
			}
	        else -> {
	            println("Tree: \n$listPrimeLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return listPrimeLocalContext
	}
	
	private fun stmt(): StmtContext {
	    val stmtLocalContext = StmtContext("stmt")
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAL, LabSecondGrammarToken.VAR -> {
				val modifier = modifier()
				stmtLocalContext.modifier = modifier
				stmtLocalContext.add(modifier)
		
				lastToken = check(LabSecondGrammarToken.ID)
				stmtLocalContext.ID = lastToken
				stmtLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val type = type()
				stmtLocalContext.type = type
				stmtLocalContext.add(type)
		
				val assignment = assignment()
				stmtLocalContext.assignment = assignment
				stmtLocalContext.add(assignment)
		
				lastToken = check(LabSecondGrammarToken.SEMICOLON)
				stmtLocalContext.SEMICOLON = lastToken
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
	    val modifierLocalContext = ModifierContext("modifier")
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.VAR -> {
				lastToken = check(LabSecondGrammarToken.VAR)
				modifierLocalContext.VAR = lastToken
				modifierLocalContext.add(Leaf(lastToken, lastToken.value))
		
			}
			LabSecondGrammarToken.VAL -> {
				lastToken = check(LabSecondGrammarToken.VAL)
				modifierLocalContext.VAL = lastToken
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
	    val typeLocalContext = TypeContext("type")
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.COLON -> {
				lastToken = check(LabSecondGrammarToken.COLON)
				typeLocalContext.COLON = lastToken
				typeLocalContext.add(Leaf(lastToken, lastToken.value))
		
				lastToken = check(LabSecondGrammarToken.TYPE)
				typeLocalContext.TYPE = lastToken
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
	    val assignmentLocalContext = AssignmentContext("assignment")
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.ASSIGN -> {
				lastToken = check(LabSecondGrammarToken.ASSIGN)
				assignmentLocalContext.ASSIGN = lastToken
				assignmentLocalContext.add(Leaf(lastToken, lastToken.value))
		
				val value = value()
				assignmentLocalContext.value = value
				assignmentLocalContext.add(value)
		
			}
			LabSecondGrammarToken.SEMICOLON -> {
				// do nothing
			}
	        else -> {
	            println("Tree: \n$assignmentLocalContext")
	            throw Exception("Unexpected token: $curToken")
	        }
	    }
	    return assignmentLocalContext
	}
	
	private fun value(): ValueContext {
	    val valueLocalContext = ValueContext("value")
	    var lastToken: LabSecondGrammarToken
    
	    when (curToken) {
			LabSecondGrammarToken.INT -> {
				lastToken = check(LabSecondGrammarToken.INT)
				valueLocalContext.INT = lastToken
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
