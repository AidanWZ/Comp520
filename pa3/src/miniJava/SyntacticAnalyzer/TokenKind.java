/**
 *   TokenKind is a simple enumeration of the different kinds of tokens
 *   
 */
package miniJava.SyntacticAnalyzer;

public enum TokenKind {
    PROGRAM, 
    CLASSDECLARATION, 
    FIELDDECLARATION, 
    METHODDECLARATION, 
    VISIBILITY, 
    ACCESS, 
    TYPE, 
    PARAMETERLIST, 
    ARGUMENTLIST,
    REFERENCE, 
    IXREFERENCE, 
    EXPRESSION,
    STATEMENT,
    EOT
}
