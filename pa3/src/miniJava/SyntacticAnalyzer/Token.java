package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.SourcePosition;

public final class Token extends Object {

	public int kind;
	public String spelling;
	public SourcePosition position;

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
		THIS = 14, 
		TRUE = 15, 
		FALSE = 16, 
		IF = 17, 
		ELSE = 18, 
		NULL = 19,
		WHILE = 20, 
		EQUALS = 21,
		PERIOD = 22,
		COLON = 23, 
		SEMICOLON = 24, 
		COMMA = 25, 
		LPAREN = 26, 
		RPAREN = 27, 
		LBRACKET = 28, 
		RBRACKET = 29, 
		LCURLY = 30, 
		RCURLY = 31, 
		EOT = 32, 
		ERROR = 33; 
	
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
