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
    : 'import' value+=ID ('.' value+=ID)* ';' #ImportDecl
    ;

classDeclaration
    : 'class' name=ID ('extends' superName=ID)? '{' ((varDeclaration) | (methodDeclaration))* '}' #ClassDecl
    ;

varDeclaration
    : type name=ID ';' #VarDecl
    ;

methodDeclaration
    : ('public')? ('static')? returnType name=ID '(' (argument (',' argument)*)? ')' '{' (varDeclaration | statement)* '}'  #MethodDecl
    | ('public')? 'static' 'void' name='main' '(' 'String' '['']' argName=ID ')' '{' (varDeclaration | statement)* '}' #MainMethodDecl
    ;

returnType
    : type    #TypeTagNotUsed
    | 'void'  #VoidType
    ;

returnStatement
    : 'return' expression ';'
    | 'return' ';'
    ;

argument
    : type name=ID #Param
    ;

type
    : defaultType '[' ']'       #ArrayType
    | defaultType '...'         #VarArgType
    | defaultType               #DflType
    | name=ID                   #ClassType
    ;

defaultType
    : name='boolean' #BooleanType
    | name='int'     #IntType
    | name='String'  #StringType
    ;

statement
    : '{' statement* '}' #BracketStmt
    | 'if' '(' expression ')' statement ('else if' '(' expression ')' statement)* ('else' statement)? #IfStmt
    | 'while' '(' expression ')' statement #WhileStmt
    | expression ';' #ExpressionStmt
    | var=ID op=('=' | '+=' | '-=' | '*=' | '/=') expression ';' #AssignStmt
    | var=ID '[' index=expression ']' '=' expression ';' #ArrayAssignStmt
    | returnStatement    #ReturnStmt
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

