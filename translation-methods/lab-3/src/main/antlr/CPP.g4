grammar CPP;

translationUnit: declarartionseq? EOF;

declarartionseq: declaration+;

declaration: functionDefinition | blockDeclaration;



functionDefinition: declSpecifierSeq declarator compoundStatement;



statementseq: statement+;

statement: declarationStatement | expressionStatement | compoundStatement | selectionStatement | iterationStatement | jumpStatement;



declarator: IDENTIFIER LeftParen parameterList? RightParen;

parameterList: parameterDeclaration (',' parameterDeclaration)*;

parameterDeclaration: typeSpecifier IDENTIFIER;

compoundStatement: LeftBrace statementseq? RightBrace;




declarationStatement: blockDeclaration;

blockDeclaration: simpleDeclaration;

simpleDeclaration: declSpecifierSeq? initDeclaratorList Semicolon;

initDeclaratorList: initDeclarator (Comma initDeclarator)*;

initDeclarator: IDENTIFIER initializer?;

initializer: Assign assignmentExpression | LeftParen initializerList RightParen;

initializerList: assignmentExpression (Comma assignmentExpression)*;



jumpStatement: (Break | Continue | returnExpression) Semicolon;

returnExpression: Return expression?;

// can add initializer later
selectionStatement: If LeftParen expression RightParen statement (Else statement)?;

iterationStatement:
    While LeftParen expression RightParen statement
    | For LeftParen forInitStmt expression? Semicolon expression? RightParen statement;

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

additiveExpression: multiplicativeExpression ((Plus | Minus) multiplicativeExpression)*;

multiplicativeExpression: unaryExpression ((Mul | Div | Mod) unaryExpression)*;

unaryExpression: postfixExpression | (PlusPlus | MinusMinus | unaryOperator) unaryExpression;

unaryOperator: Or | Mul | And | Plus | Minus | Not;

postfixExpression: primaryExpression | postfixExpression LeftParen initializerList RightParen | postfixExpression (PlusPlus | MinusMinus);

primaryExpression: literal+ | IDENTIFIER | LeftParen expression RightParen;


literal: Intergerliteral | Booleanliteral | Stringliteral;


declSpecifierSeq: declSpecifier+;

declSpecifier: typeSpecifier | typeQualifier;

typeSpecifier: Void | Int | Bool;

typeQualifier: Const | Volatile;


Intergerliteral: DIGIT+;

Stringliteral: '"' ~ ['\\\r\n]* '"';

Booleanliteral: True | False;


If: 'if';

Else: 'else';

False: 'false';

True: 'true';

For: 'for';


Bool: 'bool';

Int: 'int';

Void: 'void';


Break: 'break';

Continue: 'continue';

Return: 'return';


Const: 'const';

Volatile: 'volatile';

While: 'while';

shiftOperator: Less Less | Greater Greater;

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


fragment DIGIT: [0-9];

IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;

Whitespace: [ \t]+ -> skip;

Newline: ('\r' '\n'? | '\n') -> skip;

BlockComment: '/*' .*? '*/' -> skip;

LineComment: '//' ~ [\r\n]* -> skip;
