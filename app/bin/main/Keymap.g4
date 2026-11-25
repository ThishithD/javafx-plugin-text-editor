grammar Keymap;

// Parser EBNF rules
keymapFile : binding* EOF ;

binding    : keyCombo EQUALS action target=STRING location ;

keyCombo   : (modifiers+=MODIFIER PLUS)+ key=LETTER ;

action     : INSERT
           | DELETE
           ;

location   : AT START OF LINE
           | AT CARET
           ;

// Lexer regex rules
MODIFIER   : 'alt' | 'ctrl' | 'shift' ;
INSERT     : 'insert' ;
DELETE     : 'delete' ;
AT         : 'at' ;
START      : 'start' ;
OF         : 'of' ;
LINE       : 'line' ;
CARET      : 'caret' ;

EQUALS     : '=' ;
PLUS       : '+' ;
LETTER     : [a-z] ;
STRING     : '"' ~["\r\n]* '"' ;

WHITESPACE : [ \t\r\n]+ -> skip ;
COMMENT    : '#' ~[\r\n]* -> skip ;