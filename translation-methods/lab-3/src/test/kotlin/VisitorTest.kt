import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VisitorTest {

    private fun format(input: String): String {
        val parser = CPPParser(CommonTokenStream(CPPLexer(CharStreams.fromString(input))))
        val parsedTree = parser.translationUnit()
        val visitor = Visitor()
        visitor.visit(parsedTree)
        return visitor.getCode()
    }

    @Test
    fun testVisitor() {
        val unformattedCode = """
            int    main () {
            return 0
            ;   
            }
        """.trimIndent()
        val formattedCode = """
            int main() {
                return 0;
            }
            
        """.trimIndent()
        assertEquals(formattedCode, format(unformattedCode))
    }

    @Test
    fun testSelectionExpression() {
        val unformattedCode = """
            int f() {
            if (
            a == 0
            ) int a
            
             = 1; int 
             a = 2
             ;
            
              if(  a == 0
              ) 
               { return 0
                ;
                  }else{ 
                return 1
                ;
                }
            }
        """.trimIndent()
        val formattedCode = """
            int f() {
                if (a == 0)
                    int a = 1;
                int a = 2;
                if (a == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }
            
        """.trimIndent()
        assertEquals(formattedCode, format(unformattedCode))
    }

    @Test
    fun testIterationExpression() {
        val unformattedCode = """
            void f() {
            for(int i = 0
            ; 
            i < 10
            ; 
            i++
            ) {
            int a = 1
            ;
            } for 
           (int i = 0
           ;  ;i++
           ) { int a = 1;
                }
            while(a == 0) {
            int a = 1
            ;
            }
            }
        """.trimIndent()
        val formattedCode = """
            void f() {
                for (int i = 0; i < 10; i++) {
                    int a = 1;
                }
                for (int i = 0;; i++) {
                    int a = 1;
                }
                while (a == 0) {
                    int a = 1;
                }
            }
            
        """.trimIndent()
        assertEquals(formattedCode, format(unformattedCode))
    }
}