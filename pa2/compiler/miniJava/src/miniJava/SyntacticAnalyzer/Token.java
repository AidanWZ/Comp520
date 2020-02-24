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
		WHILE = 19, 
		EQUALS = 20,
		PERIOD = 21,
		COLON = 22, 
		SEMICOLON = 23, 
		COMMA = 24, 
		LPAREN = 25, 
		RPAREN = 26, 
		LBRACKET = 27, 
		RBRACKET = 28, 
		LCURLY = 29, 
		RCURLY = 30, 
		EOT = 31, 
		ERROR = 32; 
	
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
