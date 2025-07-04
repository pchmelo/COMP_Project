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
    : 'import' name+=ID ('.' name+=ID)* ';' #ImportDecl
    ;

classDeclaration
    : 'class' name=ID ('extends' superName=ID)? '{' ((varDeclaration) | (methodDeclaration))* '}' #ClassDecl
    ;

varDeclaration
    : type name=ID ';' #VarDecl
    ;

methodDeclaration
    : (pub='public')? (st='static')? returnType name=ID '(' ( varargDeclaration | argument (',' argument)* (',' varargDeclaration)?  )? ')' '{' (varDeclaration | statement)* '}'  #MethodDecl
    //| ('public')? st='static' 'void' name='main' '(' 'String' '['']' argName=ID ')' '{' (varDeclaration | statement)* '}' #MainMethodDecl
    ;

//(v | m a* b?)?


returnType
    : type    #TypeTagNotUsed
    | 'void'  #VoidType
    ;

returnStatement
    : 'return' expression ';'
    //| 'return' ';'
    ;

argument
    : type name=ID #Param
    ;

varargDeclaration
    : type '...' name=ID        #VarArgType
    ;

type
    : defaultType '[' ']'       #ArrayType
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
    | 'const' defaultType name=ID '=' expression ';' #ConstStmt
    | type name=ID '=' expression ';' #VarAssignStmt
    | name=ID op=('=' | '+=' | '-=' | '*=' | '/=') expression ';' #AssignStmt    //
    | name=ID '[' index=expression ']' '=' expression ';' #ArrayAssignStmt      //
    | returnStatement    #ReturnStmt                                            //
    ;

expression
    : '(' expression ')' #ParenthesesExpr          //
    | name=ID op=('++' | '--') #Postfix          //
    | '!' expression #NotExpr           ////
    | expression '.length' #ArrayLengthExpr    ////
    | expression '[' expression ']' #ArrayAccessExpr   ////
    | expression '.' name=ID '(' (expression (',' expression)*)? ')' #MethodCallExpr    //
    | name=ID '(' (expression (',' expression)*)? ')' #MethodCall
    | expression op=('*' | '/') expression #BinaryExpr     //
    | expression op=('+' | '-') expression #BinaryExpr    //
    | expression op=('>' | '<' | '>=' | '<=') expression #BinaryExpr     //
    | expression op=('==' | '!=') expression #BinaryExpr     //
    | expression op='&&' expression #BinaryExpr    //
    | expression op='||' expression #BinaryExpr    //
    | 'new' defaultType '[' expression ']' #NewIntArrayExpr   ////
    | 'new' name=ID '(' ')' #NewObjectExpr      ////
    | '[' (expression (',' expression)*)? ']' #ArrayInit    ////
    | value=INTEGER #IntegerExpr        //
    | value='true' #TrueExpr            //
    | value='false' #FalseExpr          //
    | name=ID #VarRefExpr               //
    | 'this' #ThisExpr                  //
    ;

