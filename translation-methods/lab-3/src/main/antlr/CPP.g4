grammar CPP;

/*
Parser
*/

translationUnit: declarartionseq? EOF;

declarartionseq: declaration+;

declaration: blockDeclaration | functionDefinition;



functionDefinition: declSpecifierSeq? declarator compoundStatement;



idExpression: unqualifiedid | qualifiedid;

qualifiedid: nestedNameSpecifier unqualifiedid;

unqualifiedid: IDENTIFIER;

nestedNameSpecifier:
    IDENTIFIER? DoubleColon
    | nestedNameSpecifier IDENTIFIER DoubleColon;



statementseq: statement+;

statement:
    declarationStatement
    | expressionStatement
    | compoundStatement
    | selectionStatement
    | iterationStatement
    | jumpStatement;



declarator: ptrDeclarator;

ptrDeclarator: (ptrOperator)* noPtrDeclarator;

noPtrDeclarator: declaratorId | noPtrDeclarator parametersAndQualifiers;

declaratorId: idExpression;

ptrOperator: (And | AndAnd) | Mul;

parametersAndQualifiers: LeftParen parameterDeclarationClause? RightParen;

parameterDeclarationClause: parameterDeclarationList;

parameterDeclarationList: parameterDeclaration (Comma parameterDeclaration)*;

parameterDeclaration: declSpecifierSeq declarator (Assign assignmentExpression)?;



compoundStatement: LeftBrace statementseq? RightBrace;



declarationStatement: blockDeclaration;



blockDeclaration: simpleDeclaration;

simpleDeclaration: declSpecifierSeq? initDeclaratorList Semicolon;

initDeclaratorList: initDeclarator (Comma initDeclarator)*;

initDeclarator: declarator initializer?;

initializer: Assign assignmentExpression | LeftParen initializerList RightParen;

initializerList: assignmentExpression (Comma assignmentExpression)*;



jumpStatement: (Break | Continue | returnExpression) Semicolon;

returnExpression: Return expression?;



selectionStatement: If LeftParen condition RightParen statement (Else statement)?;



iterationStatement:
    While LeftParen condition RightParen statement
    | For LeftParen forInitStmt condition? Semicolon expression? RightParen statement;

condition: expression | declSpecifierSeq declarator Assign assignmentExpression;

forInitStmt: expressionStatement | simpleDeclaration;



expressionStatement: expression? Semicolon;

expression: assignmentExpression (',' assignmentExpression)*;

assignmentExpression: conditionalExpression | logicalOrExpression assignmentOperator assignmentExpression;

assignmentOperator: Assign | MulAssign | DivAssign | ModAssign | PlusAssign | MinusAssign | AndAssign | XorAssign | OrAssign;

conditionalExpression: logicalOrExpression;

logicalOrExpression: logicalAndExpression (OrOr logicalAndExpression)*;

logicalAndExpression: inclusiveOrExpression (AndAnd inclusiveOrExpression)*;

inclusiveOrExpression: exclusiveOrExpression (Or exclusiveOrExpression)*;

exclusiveOrExpression: andExpression (Caret andExpression)*;

andExpression: equalityExpression (And equalityExpression)*;

equalityExpression: relationalExpression ((Equal | NotEqual) relationalExpression)*;

relationalExpression: shiftExpression ((Less | Greater | LessEqual | GreaterEqual) shiftExpression)*;

shiftExpression: additiveExpression (shiftOperator additiveExpression)*;

shiftOperator: Less Less | Greater Greater;

additiveExpression: multiplicativeExpression ((Plus | Minus) multiplicativeExpression)*;

multiplicativeExpression: unaryExpression ((Mul | Div | Mod) unaryExpression)*;

unaryExpression: postfixExpression | (PlusPlus | MinusMinus | unaryOperator) unaryExpression;

unaryOperator: Or | Mul | And | Plus | Minus | Not;

postfixExpression: primaryExpression | postfixExpression LeftParen initializerList RightParen | postfixExpression (PlusPlus | MinusMinus);

primaryExpression: literal+ | idExpression | LeftParen expression RightParen;


literal: Intergerliteral | Booleanliteral | Stringliteral;


declSpecifierSeq: declSpecifier+;

declSpecifier: simpleTypeSpecifier | cvQualifier;

simpleTypeSpecifier:
    nestedNameSpecifier? IDENTIFIER
    | Auto
    | Void
    | Int
    | Bool;

cvQualifier: Const | Volatile;


Intergerliteral: DIGIT+;

Stringliteral: '"' Schar* '"';

Booleanliteral: True | False;


/*
Lexer
*/

If: 'if';

Else: 'else';

False: 'false';

True: 'true';

For: 'for';

Bool: 'bool';

Int: 'int';

Void: 'void';

Auto: 'auto';

Break: 'break';

Continue: 'continue';

Return: 'return';

Const: 'const';

Volatile: 'volatile';

While: 'while';

LeftParen: '(';

RightParen: ')';

LeftBrace: '{';

RightBrace: '}';

Plus: '+';

Minus: '-';

Mul: '*';

Div: '/';

Mod: '%';

Caret: '^';

And: '&';

Or: '|';

Not: '!';

Assign: '=';

Less: '<';

Greater: '>';

PlusAssign: '+=';

MinusAssign: '-=';

MulAssign: '*=';

DivAssign: '/=';

ModAssign: '%=';

XorAssign: '^=';

AndAssign: '&=';

OrAssign: '|=';

Equal: '==';

NotEqual: '!=';

LessEqual: '<=';

GreaterEqual: '>=';

OrOr: '||';

PlusPlus: '++';

MinusMinus: '--';

AndAnd: '&&';

Comma: ',';

Semicolon: ';';

DoubleColon: '::';

IDENTIFIER: NONDIGIT (DIGIT | NONDIGIT)*;


fragment DIGIT: [0-9];

fragment NONDIGIT: [a-zA-Z_];

fragment Schar: ~ ["\\\r\n];

Whitespace: [ \t]+ -> skip;

Newline: ('\r' '\n'? | '\n') -> skip;

BlockComment: '/*' .*? '*/' -> skip;

LineComment: '//' ~ [\r\n]* -> skip;
