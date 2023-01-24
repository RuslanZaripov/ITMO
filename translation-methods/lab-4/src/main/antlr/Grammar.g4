grammar Grammar;

inputGrammar: grammarName rules;

grammarName: GRAMMAR TOKEN_NAME SEMI;

rules: rule+;

rule: terminalRule | nonTerminalRule;

terminalRule: TOKEN_NAME COLON REGEX SKIP_MODIFIER? SEMI;

nonTerminalRule: RULE_NAME attributeList? returnList? COLON alternatives SEMI;

returnList: RETURN attributeList;

attributeList:  LEFT_SQUARE_BRACKET attribute (COMMA attribute)* RIGHT_SQUARE_BRACKET;

attribute: type=TOKEN_NAME name=RULE_NAME;

// TODO: add EPSILON
alternatives: alternative (OR alternative)*;

alternative: production+;

production: TOKEN_NAME CODE? | RULE_NAME ARGS? CODE?;

// | '(' alternatives ')';

GRAMMAR: 'grammar';
RETURN: 'return';

OR: '|';
COLON: ':';
SEMI: ';';
COMMA: ',';
LEFT_SQUARE_BRACKET: '[';
RIGHT_SQUARE_BRACKET: ']';
LEFT_PARAN: '(';
RIGHT_PARAN: ')';
LEFT_BRACE: '{';
RIGHT_BRACE: '}';
SKIP_MODIFIER : '-> skip';
RULE_NAME: [a-z][a-zA-Z]*;
TOKEN_NAME: [A-Z][a-zA-Z_]*;

ARGS : LEFT_PARAN .*? RIGHT_PARAN ;
CODE : LEFT_BRACE .*? RIGHT_BRACE ;
REGEX: '"'.*?'"';

WHITESPACE: [ \t\r\n] -> skip ;
