grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

INTEGER : '0' | [1-9] [0-9]* ;
ID : [a-zA-Z_$] [a-zA-Z0-9_$]* ;

MULT_LINE_COMMENT : '/*' .*? '*/' -> skip ;
END_OF_LINE_COMMENT : '//' ~[\r\n]* -> skip ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : importDeclaration* classDeclaration EOF
    ;

importDeclaration
    : 'import' value+=ID ('.' value+=ID)* ';' #ImportStatement
    ;

classDeclaration
    : 'class' ID ('extends' ID)? '{' varDeclaration* methodDeclaration* '}'
    ;

varDeclaration
    : type name=ID ';'
    ;

methodDeclaration
    : ('public')? ('static')? returnType methodName=ID '(' argument? (',' argument)* ')' '{' (varDeclaration | statement)* returnStmt '}'
    | ('public')? 'static' 'void' 'main' '(' 'String' '['']' argName=ID ')' '{' varDeclaration* statement* '}'
    ;

returnType
    : type
    | 'void'
    ;

returnStmt
    : 'return' expression ';'
    | 'return' ';'
    ;

argument
    : type argName=ID
    ;

type
    : type '[' ']'      #ArrayType
    | type '...'        #VarArgType
    | 'boolean'         #BooleanType
    | 'int'             #IntType
    | 'String'          #StringType
    | ID                #ClassType
    ;

statement
    : '{' statement* '}' #BracketStmt
    | 'if' '(' expression ')' statement ('else if' '(' expression ')' statement)* ('else' statement)? #IfStmt
    | 'while' '(' expression ')' statement #WhileStmt
    | expression ';' #ExpressionStmt
    | var=ID op=('=' | '+=' | '-=' | '*=' | '/=') expression ';' #AssignStmt
    | var=ID '[' index=expression ']' '=' expression ';' #ArrayAssignStmt
    ;

expression
    : '(' expression ')' #ParenthesesExpr
    | value=ID op=('++' | '--') #Postfix
    | expression op=('*' | '/') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | expression op=('>' | '<' | '>=' | '<=') expression #BinaryOp
    | expression op=('==' | '!=') expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | expression op='||' expression #BinaryOp
    | expression '[' expression ']' #ArrayAccessExpr
    | expression '.' 'length' #ArrayLengthExpr
    | expression '.' methodName=ID '(' (expression (',' expression)*)? ')' #MethodCallExpr
    | 'new' 'int' '[' expression ']' #NewIntArrayExpr
    | 'new' ID '(' ')' #NewObjectExpr
    | '!' expression #NotExpr
    | '[' (expression (',' expression)*)? ']' #ArrayExpr
    | value=INTEGER #IntegerExpr
    | value='true' #TrueExpr
    | value='false' #FalseExpr
    | value=ID #VarExpr
    | 'this' #ThisExpr
    ;

