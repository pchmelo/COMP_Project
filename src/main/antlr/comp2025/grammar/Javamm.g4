grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

CLASS : 'class' ;
INT : 'int' ;
PUBLIC : 'public' ;
RETURN : 'return' ;

INTEGER : '0' | [1-9] [0-9]* ;
ID : [a-zA-Z_$] [a-zA-Z0-9_$]* ;

MULT_LINE_COMMENT : '/*' .*? '*/' -> skip ;
END_OF_LINE_COMMENT : '//' ~[\r\n]* -> skip ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDeclaration)* classDeclaration EOF
    ;

importDeclaration
    : 'import' value+=ID ('.' value+=ID)* ';' #ImportDecl
    ;

classDeclaration
    : 'class' name=ID ('extends' superName=ID)? '{' (varDeclaration)* (methodDeclaration)* '}' #ClassDecl     //!!superName not tested in grammar
    ;

varDeclaration
    : type name=ID ';' #VarDecl
    ;

methodDeclaration
    : ('public')? returnType methodName=ID '(' (argument)? ')' '{' (varDeclaration)* (statement)* returnStmt '}'   //empty arguments are not tested in grammar
    | ('public')? 'static' 'void' 'main' '(' 'String' '['']' argName=ID ')' '{' (varDeclaration)* (statement)* '}'
    ;

returnType
    : type
    ;

returnStmt
    : 'return' expression ';'
    ;

param
    : type name=ID
    ;

argument
    : type argName=ID (',' argument)*
    ;

type
    : INT '[' ']'       #IntArrayType
    | INT '...'         #VarArgType
    | 'boolean'         #BooleanType
    | INT               #IntType
    | name=ID                #ClassType
    | 'String'          #StringType
    ;

statement
    : '{' statement* '}' #BlockStmt
    | 'if' '(' expression ')' ifStmt=statement ('else' elseStmt=statement)? #IfStmt
    | 'while' '(' expression ')' whileStmt=statement #WhileStmt
    | expression ';' #ExpressionStmt
    | var=ID '=' expression ';' #AssignStmt
    | var=ID '[' index=expression ']' '=' expression ';' #ArrayAssignStmt
    ;

expression
    : expression op=('&&' | '<' | '+' | '-' | '*' | '/' ) expression #ComparisonExpr
    | expression '[' expression ']' #ArrayAccessExpr
    | expression '.' 'length' #ArrayLengthExpr
    | expression '.' methodName=ID '(' (expression (',' expression)*)? ')' #MethodCallExpr
    | 'new' INT '[' expression ']' #NewIntArrayExpr
    | 'new' ID '(' ')' #NewObjectExpr
    | '!' expression #NotExpr
    | '(' expression ')' #ParenthesesExpr
    | '[' (expression (',' expression)*)? ']' #ArrayExpr
    | value=INTEGER #IntegerExpr
    | 'true' #TrueExpr
    | 'false' #FalseExpr
    | var=ID #VarExpr
    | 'this' #ThisExpr
    ;

