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
    : IMPORT name+=ID ('.' name+=ID)* ';'
    ;

classDecl
    : CLASS name=ID (EXTENDS superClass=ID)?
        '{' varDecl* methodDecl*'}'
    ;

varDecl
    : type name=ID ';'
    ;

type locals[boolean isArray=false, boolean isEllipsis=false]
    : (PUBLIC {$isArray=true;})?
          INT '['']' #ArrayType
    |  INT '...' {$isArray=true; $isEllipsis=true;} #VarargType
    |  BOOL #BoolType
    |  INT #IntType
    |  STRING #StringType
    |  name = ID #NameType
    ;

methodDecl locals[boolean isPublic=false, boolean isEmpty=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        '(' param ')'
        '{' varDecl* stmt* RETURN expr ';' '}'
    | (PUBLIC {$isPublic=true;})?
        STATIC VOID name=MAIN '(' STRING '[' ']' id=ID ')'
         '{' varDecl* stmt* '}' {$isEmpty=true;}
    ;

param
    :  (type name+=ID (',' type name+=ID)*)?
    ;

stmt
    : '{' stmt* '}'                            #BlockStmt
    | IF '(' expr ')' stmt ELSE stmt           #IfStmt
    | WHILE '(' expr ')' stmt                  #WhileStmt
    | expr ';'                                 #ExprStmt
    | expr '=' expr ';'                        #AssignStmt
    | RETURN expr ';'                          #ReturnStmt
    | expr '[' expr ']' '=' expr ';'           #ArrayAssignStmt
    ;

expr
    : '(' expr ')' #ParenthesizesExpr
    | NEW INT '[' expr ']' #NewArrayExpr
    | NEW name=ID '('')' #NewObjectExpr
    | expr '[' expr ']'  #ArrayElemExpr
    | expr '.' LENGTH #LengthExpr
    | expr '.' name=ID '('(expr (',' expr)*)?')' #MethodCallExpr
    |'!' expr #NotExpr
    | expr op= ( '*' | '/' ) expr #BinaryExpr
    | expr op= ( '+' | '-' ) expr #BinaryExpr
    | expr op=  '<'  expr #BinaryExpr
    | expr op= '&&' expr #BinaryExpr
    | '[' (expr (',' expr)*)? ']' #ArrayExpr
    | value=INTEGER #IntegerLiteral
    | TRUE #BooleanLiteral
    | FALSE #BooleanLiteral
    | THIS #ThisExpr
    | name=ID #VarRefExpr
    ;

