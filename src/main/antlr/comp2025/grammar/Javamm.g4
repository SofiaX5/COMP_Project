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
STRING : 'String' ;

INTEGER : [0-9]+ ;
ID : [a-zA-Z$_][a-zA-Z$_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDecl)* classDecl EOF
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
    |  STRING
    |  name = ID
    ;

methodDecl locals[boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        '(' param ')'
        '{' varDecl* stmt* RETURN expr ';' '}'
    | (PUBLIC {$isPublic=true;})?
        STATIC VOID MAIN '(' STRING '[' ']' ID ')'
         '{' varDecl* stmt* '}'

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
    : expr op= ('&&' | '<' | '+' | '-' | '*' | '/') expr #BinaryExpr
    | expr '[' expr ']'  #ArrayElemExpr
    | expr '.' LENGTH #LengthExpr
    | expr '.' name=ID '('(expr (',' expr)*)?')' #MethodCallExpr
    | NEW INT '[' expr ']' #NewArrayExpr
    | NEW name=ID '('')' #NewObjectExpr
    | '!' expr #NotExpr
    | '(' expr ')' #ParenthesizesExpr
    | '[' (expr (',' expr)*)? ']' #ArrayExpr
    | value=INTEGER #IntegerLiteral
    | TRUE #BooleanLiteral
    | FALSE #BooleanLiteral
    | name=ID #VarRefExpr
    | THIS #ThisExpr
    ;

