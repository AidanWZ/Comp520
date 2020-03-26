package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.SourcePosition;

public final class Token extends Object {

	public int kind;
	public String spelling;
	public SourcePosition position;
	public int lineNumber;
	public int charNumber;

	public Token(int kind, String spelling, SourcePosition position) {
		this.kind = kind;
		this.spelling = spelling;
		this.position = position;
		
		if (kind == Token.IDENTIFIER) {
			//Iterate through all the tokens and if we find that token then exit the loop
			
			for(int kindCount = firstReservedWord; kindCount <= lastReservedWord; kindCount++){
				if(spelling.equals(tokenTable[kindCount])){
					this.kind = kindCount;
					//if (this.kind == Token.NULL) {System.out.println("yes");}
					break;
				}
			}
		}
	}

	public Token(int kind, String spelling, int lineNumber, int charNumber) {
		this.kind = kind;
		this.spelling = spelling;
		this.lineNumber = lineNumber;
		this.charNumber = charNumber;
		
		if (kind == Token.IDENTIFIER) {
			//Iterate through all the tokens and if we find that token then exit the loop
			
			for(int kindCount = firstReservedWord; kindCount <= lastReservedWord; kindCount++){
				if(spelling.equals(tokenTable[kindCount])){
					this.kind = kindCount;
					//if (this.kind == Token.NULL) {System.out.println("yes");}
					break;
				}
			}
		}
	}

	public static String spell (int kind) {
		return tokenTable[kind];
	}
	public String toString() {
		return "Kind=" + kind + ", spelling=" + spelling + ", position=" + position;
	}

	// Token classes...
	/*
	* gives all the tokens their respective values
	*/
	public static final int
		IDENTIFIER = 0, 
		OPERATOR = 1,
		NUM = 2, 
		BINOP = 3, 
		UNOP = 4, 
		NEW = 5, 
		CLASS = 6, 
		RETURN = 7, 
		PRIVATE = 8, 
		PUBLIC = 9, 
		STATIC = 10, 
		INT = 11, 
		BOOLEAN = 12, 
		VOID = 13, 
		STRING = 14,
		THIS = 15, 
		TRUE = 16, 
		FALSE = 17, 
		IF = 18, 
		ELSE = 19, 
		NULL = 20,
		WHILE = 21, 
		EQUALS = 22,
		PERIOD = 23,
		COLON = 24, 
		SEMICOLON = 25, 
		COMMA = 26, 
		LPAREN = 27, 
		RPAREN = 28, 
		LBRACKET = 29, 
		RBRACKET = 30, 
		LCURLY = 31, 
		RCURLY = 32, 
		EOT = 33, 
		ERROR = 34; 
	
	public static String[] tokenTable = new String[] {
		"<id>", 
		"<operator>", 
		"<num>", 
		"<binop>", 
		"<unop>",
		"new",
		"class", 
		"return", 
		"private", 
		"public", 
		"static",
		"int", 
		"boolean", 
		"void", 
		"String",
		"this", 
		"true", 
		"false", 
		"if", 
		"else",
		"null",
		"while",
		"=", 
		".", 
		":", 
		";", 
		",", 
		"(", 
		")", 
		"[", 
		"]",
		"{", 
		"}",
		"",  
		"<error>"
	};

	public final static String[] binop = {"<", ">", "<=", ">=", "==", "!=", "&&", "/", "-", "+", "*", "||"};
	public final static String[] unop = {"-", "!"};

	public static boolean isBinop(String op){
		for(int i = 0; i < binop.length; i++) {
			if(binop[i].equals(op)) {
				return true;
			}
		}
		return false;
	}
	private final static int	
		firstReservedWord = Token.NEW,
		lastReservedWord  = Token.WHILE;

	public static boolean isUnop(String op) {
		for(int i = 0; i < unop.length; i++) {
			if(unop[i].equals(op)) {
				return true;
			}
		}
		return false;
	}	
}
