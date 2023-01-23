grammar Grammar;

inputGrammar: grammarName rules;

grammarName: GRAMMAR TOKEN_NAME SEMI;

rules: rule+;

rule: terminalRule | nonTerminalRule;

terminalRule: TOKEN_NAME COLON REGEX SKIP_MODIFIER? SEMI;

nonTerminalRule: RULE_NAME COLON alternatives SEMI;

alternatives: alternative (OR alternative)*;

alternative: production+;

production: TOKEN_NAME | RULE_NAME;

// | '(' alternatives ')';

GRAMMAR: 'grammar';

OR: '|';
COLON: ':';
SEMI: ';';
SKIP_MODIFIER : '-> skip';
RULE_NAME: [a-z][a-zA-Z]*;
TOKEN_NAME: [A-Z][a-zA-Z_]*;
REGEX: '"'.*?'"';
WHITESPACE: [ \t\r\n] -> skip ;
