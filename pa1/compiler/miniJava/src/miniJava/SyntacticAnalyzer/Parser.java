package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.SyntaxError;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.ErrorReporter;

public class Parser {

	private Scanner Scanner;
	private ErrorReporter errorReporter;
	private Token currentToken;
	private SourcePosition previousTokenPosition;
	private boolean debug;
	
	public Parser(Scanner scanner, ErrorReporter reporter, boolean debug) {
		this.Scanner = scanner;
		this.errorReporter = reporter;
		this.previousTokenPosition = new SourcePosition();
		this.debug = debug;
	}

	void accept(int tokenExpected, String methodOrigin) throws SyntaxError {
	    if (currentToken.kind == tokenExpected) {
		  previousTokenPosition = this.currentToken.position;
		  if(debug==true) System.out.println(currentToken.spelling);
	      currentToken = Scanner.scan();
	    } 
	    else {
	      syntacticError(String.format("Unexpected token. Expected %s but got %s in %s", Token.spell(tokenExpected), currentToken.spelling, methodOrigin), currentToken.spelling);
	    }
	  }
	void acceptIt() {
		previousTokenPosition = currentToken.position;
		if(debug==true) System.out.println(currentToken.spelling);
		currentToken = Scanner.scan();
	}
	void start(SourcePosition position) {
		position.start = currentToken.position.start;
	}
	void finish(SourcePosition position) {
	    position.finish = previousTokenPosition.finish;
	}

	void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
		SourcePosition pos = currentToken.position;
		System.out.println("Syntactic Error: " + messageTemplate + " at token position " + pos.start);
		errorReporter.reportError(messageTemplate, tokenQuoted, pos);	
		throw(new SyntaxError());	
	}

	void debug(int kind) {
		System.out.println(Token.spell(kind));
	}

	public void parseProgram() throws SyntaxError{
		if(debug==true) System.out.println("Running parseProgram");
		previousTokenPosition.start = 0;
		previousTokenPosition.finish = 0;
		currentToken = Scanner.scan();
		try {
			while(currentToken.kind == Token.CLASS){
				parseClassDeclaration();
				
			}
			if (currentToken.kind != Token.EOT) {
				syntacticError("Unexpected EOT", currentToken.spelling);
			}
		}
		catch (SyntaxError s) { 
			return; 
		}
	}

	//ClassDeclaration ::= class id { ( FieldDeclaration | MethodDeclaration )* }
	public void parseClassDeclaration() throws SyntaxError{
		if(debug==true) System.out.println("Running parseClassDeclaration");
		accept(Token.CLASS, "parseClassDeclaration");
		parseIdentifier();
		accept(Token.LCURLY, "parseClassDeclaration");
		while(currentToken.kind != Token.RCURLY){
			parseDeclarators();
			parseIdentifier();
			switch(currentToken.kind){
				case Token.SEMICOLON:
					acceptIt();
					break;
				case Token.LPAREN:  
					acceptIt();
					if(currentToken.kind != Token.RPAREN){
						parseParameterList();
						accept(Token.RPAREN, "parseClassDeclaration");
					}
					else if (currentToken.kind == Token.RPAREN) {
						accept(Token.RPAREN, "parseClassDeclaration");
					}
					accept(Token.LCURLY, "parseClassDeclaration");
					while(currentToken.kind != Token.RETURN && currentToken.kind != Token.RCURLY ){
						parseStatement();
					}
					if(currentToken.kind == Token.RETURN){
						acceptIt();
						parseExpression();
						accept(Token.SEMICOLON, "parseClassDeclaration");
					}
					accept(Token.RCURLY, "parseClassDeclaration");
					break;
				case Token.EQUALS:
					acceptIt();
					parseNum();
					acceptIt();
					break;
				default: 
					syntacticError("Unexpected Token in parseClassDecleration", currentToken.spelling);
					break;
			}
		}
		accept(Token.RCURLY, "parseClassDeclaration");
	}
	
	public void parseIdentifier() throws SyntaxError {
		if(debug==true) System.out.println("Running parseIdentifier");
		if (currentToken.kind == Token.IDENTIFIER) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			if(currentToken.kind == Token.LBRACKET){
				acceptIt();
				if (currentToken.kind == Token.RBRACKET) {
					acceptIt();
					accept(Token.IDENTIFIER, "parseIdentifier");
				}
				else {
					parseExpression();
					accept(Token.RBRACKET, "parseIdentifier");
				}
			}
		} 
		else if (currentToken.kind == Token.BOOLEAN) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			if(currentToken.kind == Token.LBRACKET){
				acceptIt();
				if (currentToken.kind == Token.RBRACKET) {
					acceptIt();
					accept(Token.IDENTIFIER, "parseIdentifier");
				}
				else {
					parseExpression();
					accept(Token.RBRACKET, "parseIdentifier");
				}
			}
		}
		else if (currentToken.kind == Token.INT) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			if(currentToken.kind == Token.LBRACKET){
				acceptIt();
				if (currentToken.kind == Token.RBRACKET) {
					acceptIt();
					accept(Token.IDENTIFIER, "parseIdentifier");
				}
				else {
					parseExpression();
					accept(Token.RBRACKET, "parseIdentifier");
				}
			}
		}
		else {
			syntacticError("Unexpected Token in parseIdentifier", currentToken.spelling); 
		}
	}
	
	//Visibility ::= ( public | private )?
	//Access ::= static ?
	//Only accepts public, static or private, anything else is rejected
	public void parseDeclarators() throws SyntaxError{
		if(debug==true) System.out.println("Running parseDecalarators");
		if(currentToken.kind == Token.PUBLIC || currentToken.kind == Token.PRIVATE || currentToken.kind == Token.STATIC) {
			acceptIt();
		}
		parseType();
	}
	
	//Type ::= int | boolean | id | ( int | id ) [] 
	// if the type if Boolean/void we can just accept
	//if the type is int or id we need to figure out if it is an int/id/int array/id array
	public void parseType() throws SyntaxError{
		if(debug==true) System.out.println("Running parseType");
		while (currentToken.kind != Token.IDENTIFIER) {
			switch(currentToken.kind){
				case Token.INT:
					acceptIt();
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						accept(Token.RBRACKET, "parseType");	
					}
					break;
				case Token.IDENTIFIER:
					parseIdentifier();
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						accept(Token.RBRACKET, "parseType");	
					}
					break;
				case Token.BOOLEAN: 
					acceptIt();
					break;
				case Token.STATIC: 
					acceptIt();
					break;
				case Token.VOID: 
					acceptIt();
					break;
				default:
					syntacticError("Unexpected Token in parseType", currentToken.spelling);
					break;
			}
		}	
	}
	
	public void parseNum() throws SyntaxError {
		if(debug==true) System.out.println("Running parseNum");
		if (currentToken.kind == Token.NUM) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} else {
			syntacticError("Unexpected Token in parseNum", currentToken.spelling);
		}
	}
	
	// ArgumentList ::= Expression ( , Expression )*
	public void parseArgumentList() throws SyntaxError{	
		if(debug==true) System.out.println("Running parseArgumentList");
		parseExpression();
		while(currentToken.kind == Token.COMMA){
			acceptIt();
			parseExpression();
		}
	}

	//ParameterList ::= Type id ( , Type id )*
	public void parseParameterList() throws SyntaxError {
		if(debug==true) System.out.println("Running parseParameterList");
		parseType();
		parseIdentifier();
		while(currentToken.kind == Token.COMMA){
			acceptIt();
			parseType();
			parseIdentifier();
		}
	}
	
	public void parseStatement() throws SyntaxError{
		if(debug==true) System.out.println("Running parseStatement");
		switch(currentToken.kind){
			case Token.LCURLY:
				acceptIt();
				while(currentToken.kind != Token.RCURLY){
					parseStatement();
				}
				accept(Token.RCURLY, "parseStatement");
				break;
			case Token.WHILE:
				acceptIt();
				accept(Token.LPAREN, "parseStatement");
				parseExpression();
				accept(Token.RPAREN, "parseStatement");
				parseStatement();
				break;				
			case Token.IF:
				acceptIt();
				accept(Token.LPAREN, "parseStatement");
				parseExpression();
				accept(Token.RPAREN, "parseStatement");
				parseStatement();
				if(currentToken.kind == Token.ELSE){
					acceptIt();
					parseStatement();
				}
				break;
			case Token.IDENTIFIER:
				parseIdentifier();
				if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					if(currentToken.kind == Token.RBRACKET){
						acceptIt();
						parseIdentifier();
						accept(Token.EQUALS, "parseStatement");
						parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
					}
					else {
						parseExpression();
						accept(Token.RBRACKET, "parseStatement");
						accept(Token.EQUALS, "parseStatement");
						parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
					}
				}
				else if(currentToken.kind == Token.PERIOD){
					do{
						acceptIt();
						parseIdentifier();
					}
					while(currentToken.kind == Token.PERIOD); {
						if(currentToken.kind == Token.LPAREN){
							acceptIt();
							if(currentToken.kind != Token.RPAREN){
								parseArgumentList();
							}
							accept(Token.RPAREN, "parseStatement");
							accept(Token.SEMICOLON, "parseStatement");
						}
						else{
							if(currentToken.kind == Token.LBRACKET){
								acceptIt();
								parseExpression();
								accept(Token.RBRACKET, "parseStatement");
							}
							accept(Token.EQUALS, "parseStatement");
							parseExpression();
							accept(Token.SEMICOLON, "parseStatement");
						}
					}				
				}
				else if(currentToken.kind == Token.LPAREN){
					acceptIt();
					if(currentToken.kind != Token.RPAREN){
						parseArgumentList();
					}
					accept(Token.RPAREN, "parseStatement");
					accept(Token.SEMICOLON, "parseStatement");
				}
				else if(currentToken.kind == Token.EQUALS){
					acceptIt();
					parseExpression();
					accept(Token.SEMICOLON, "parseStatement");
				}
				else if(currentToken.kind == Token.IDENTIFIER){
					parseIdentifier();
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
					}
					else {
						accept(Token.SEMICOLON, "parseStatement");
					}
				}
				break;
			case Token.THIS:
				acceptIt();
				while(currentToken.kind == Token.PERIOD){
					acceptIt();
					parseIdentifier();
				}
				if(currentToken.kind == Token.LPAREN){
					acceptIt();
					if(currentToken.kind != Token.RPAREN){
						parseArgumentList();
					}
					accept(Token.RPAREN, "parseStatement");
					accept(Token.SEMICOLON, "parseStatement");
				}
				else {
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						
						parseExpression();
						
						accept(Token.RBRACKET, "parseStatement");
					}
					accept(Token.EQUALS, "parseStatement");				
					parseExpression();				
					accept(Token.SEMICOLON, "parseStatement");
				}
				break;
			case Token.BOOLEAN: 
				parseType();
				parseIdentifier();
				if (currentToken.kind == Token.EQUALS) {
					accept(Token.EQUALS, "parseStatement");
					parseExpression();
					accept(Token.SEMICOLON, "parseStatement");
				}		
				else {
					accept(Token.SEMICOLON, "parseStatement");
				}			
				break;
			case Token.INT: 
				parseType();
				parseIdentifier();
				if (currentToken.kind == Token.EQUALS) {
					accept(Token.EQUALS, "parseStatement");
					parseExpression();
					accept(Token.SEMICOLON, "parseStatement");
				}
				else {
					accept(Token.SEMICOLON, "parseStatement");
				}		
				break;	
			case Token.VOID: 
				parseType();
				parseIdentifier();
				accept(Token.EQUALS, "parseStatement");
				parseExpression();
				accept(Token.SEMICOLON, "parseStatement");
				break;
			default:
				syntacticError("Unexpected Token in parseParameterList", currentToken.spelling);
			}	
	}

	// Reference ::= id | this | Reference . id 
	public void parseReference() throws SyntaxError{
		if(debug==true) System.out.println("Running parseReference");
		if(currentToken.kind != Token.THIS) {
			parseIdentifier();
		}
		else {
			acceptIt();
			accept(Token.PERIOD, "parseReference");
			parseIdentifier();
		}
	}
	
	public void parseExpression() throws SyntaxError{
		if(debug==true) System.out.println("Running parseExpression");
		parseExpandedExpression();
		while(currentToken.kind == Token.OPERATOR){
			parseBinop(); 
			parseExpandedExpression();
		}
	}
	public void parseExpandedExpression() throws SyntaxError{
		if(debug==true) System.out.println("Running parseExpandedExpression");
		switch(currentToken.kind) {
			case Token.FALSE:
				acceptIt();
				break;
			case Token.TRUE:
				acceptIt();
				break;	
			case Token.NUM:
				parseNum();
				break;
			case Token.OPERATOR:
				parseUnop();
				parseExpandedExpression();
				break;
			case Token.NEW:
				acceptIt();
				parseIdentifier();
				if(currentToken.kind == Token.LBRACKET) {
					accept(Token.LBRACKET, "parseExpandedExpression");
					parseExpression();
					accept(Token.RBRACKET, "parseExpandedExpression");
				}
				else if (currentToken.kind == Token.LPAREN) {
					acceptIt();
					accept(Token.RPAREN, "parseExpandedExpression");
				}
				while (currentToken.kind == Token.PERIOD) {
					acceptIt();
					parseExpression();
				}
				break;
			case Token.LPAREN:
				acceptIt();
				parseExpression();
				accept(Token.RPAREN, "parseExpandedExpression");
				break;
			default:
				parseReference();
				if(currentToken.kind == Token.LPAREN) {
					acceptIt();
					if(currentToken.kind != Token.RPAREN) {
						parseArgumentList();
					}
					accept(Token.RPAREN, "parseExpandedExpression");
				}
				else if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					parseExpression();
					accept(Token.RBRACKET, "parseExpandedExpression");
				}
				else if (currentToken.kind == Token.PERIOD){					
					while(currentToken.kind == Token.PERIOD)  {
						acceptIt();
						parseIdentifier();
					}					 					
				}
				break;
		}
	}
	public void parseOp() throws SyntaxError {
		if(debug==true) System.out.println("Running parseOp");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} 
		else {
			syntacticError("Unexpected Token in parseOp", currentToken.spelling);
		}
	}
	public void parseBinop() throws SyntaxError{
		if(debug==true) System.out.println("Running parseBinop");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} 
		else {
			syntacticError("Unexpected Token in parseBinop", currentToken.spelling);
		}
	}
	
	public void parseUnop() throws SyntaxError {
		if(debug==true) System.out.println("Running parseUnop");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} 
		else {
			syntacticError("Unexpected Token in parseUnop", currentToken.spelling);
		}
	}
}
