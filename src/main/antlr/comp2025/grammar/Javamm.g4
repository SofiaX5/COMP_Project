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
    : CLASS name=ID (EXTENDS superClass=ID)?
        '{' varDecl* methodDecl*'}'
    ;

varDecl
    : type name=ID ';'
    ;

type locals[boolean isArray=false]
    : (PUBLIC {$isArray=true;})?
          INT '['']' #ArrayType
    |  INT '...' #VarargType
    |  BOOL #BoolType
    |  INT #IntType
    |  STRING #StringType
    |  name = ID #NameType
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
    : '!' expr #NotExpr
    | expr op= ( '*' | '/' ) expr #BinaryExpr
    | expr op= ( '+' | '-' ) expr #BinaryExpr
    | expr op=  '<'  expr #BinaryExpr
    | expr op= '&&' expr #BinaryExpr
    | expr '[' expr ']'  #ArrayElemExpr
    | expr '.' LENGTH #LengthExpr
    | expr '.' name=ID '('(expr (',' expr)*)?')' #MethodCallExpr
    | NEW INT '[' expr ']' #NewArrayExpr
    | NEW name=ID '('')' #NewObjectExpr
    | '(' expr ')' #ParenthesizesExpr
    | '[' (expr (',' expr)*)? ']' #ArrayExpr
    | value=INTEGER #IntegerLiteral
    | TRUE #BooleanLiteral
    | FALSE #BooleanLiteral
    | THIS #ThisExpr
    | name=ID #VarRefExpr
    ;

