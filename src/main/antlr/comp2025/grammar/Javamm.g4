grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

CLASS : 'class' ;
INT : 'int' ;
PUBLIC : 'public' ;
RETURN : 'return' ;
IMPORT : 'import';
EXTENDS : 'extends';
BOOL : 'boolean';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
LENGTH: 'length';
NEW: 'new';
TRUE: 'true';
FALSE: 'false';
THIS: 'this';
STATIC: 'static';
VOID: 'void';
MAIN: 'main';

INTEGER : [0-9]+ ;
ID : [a-zA-Z$_][a-zA-Z$_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : classDecl EOF
    ;

importDecl
    : IMPORT name=ID ('.' name=ID)* ';'
    ;

classDecl
    : CLASS name=ID (EXTENDS name=ID)?
        '{' varDecl* methodDecl*'}'
    ;

varDecl
    : type name=ID ';'
    ;

type
    :  INT '['']'
    |  INT '...'
    |  BOOL
    |  INT
    //??? | 'String'
    | name = ID
    ;

methodDecl locals[boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        '(' param ')'
        '{' varDecl* stmt* RETURN expr ';' '}'
    /*??? | (PUBLIC {$isPublic=true;})? STATIC VOID MAIN '(' 'String' '[' ']' name=ID ')'
            '{' varDecl* stmt* '}' */

    ;

param
    :  (type name=ID (',' type name=ID)*)?
    ;

stmt
    : '{' stmt* '}'
    | IF '(' expr ')' stmt ELSE stmt
    | WHILE '(' expr ')' stmt
    | expr ';'
    | expr '=' expr ';' // #AssignStmt //
    | RETURN expr ';' // #ReturnStmt
    | expr '[' expr ']' '=' expr ';'
    ;

expr
    : expr op= ('&&' | '<' | '+' | '-' | '*' | '/') expr // #BinaryExpr //
    | expr '[' expr ']'
    | expr '.' LENGTH
    | expr '.' name=ID '('(expr (',' expr)*)?')'
    | NEW INT '[' expr ']'
    | NEW name=ID '('')'
    | '!' expr
    | '(' expr ')'
    | '[' (expr (',' expr)*)? ']'
    | value=INTEGER // #IntegerLiteral //
    | TRUE | FALSE
    | name=ID // #VarRefExpr //
    | THIS
    ;

