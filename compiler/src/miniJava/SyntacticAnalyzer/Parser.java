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
	
	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.Scanner = scanner;
		this.errorReporter = reporter;
		this.previousTokenPosition = new SourcePosition();
	}

	// accept checks whether the current token matches tokenExpected.
	// If so, fetches the next token.
	// If not, reports a syntactic error.
	void accept(int tokenExpected) throws SyntaxError {
	    if (currentToken.kind == tokenExpected) {
		  previousTokenPosition = this.currentToken.position;
		  System.out.println(currentToken.spelling);
	      currentToken = Scanner.scan();
	    } 
	    else {
	      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
	    }
	  }
	void acceptIt() {
		previousTokenPosition = currentToken.position;
		System.out.println(currentToken.spelling);
		currentToken = Scanner.scan();
	}

	// start records the position of the start of a phrase.
	// This is defined to be the position of the first
	// character of the first token of the phrase.
	void start(SourcePosition position) {
		position.start = currentToken.position.start;
	}

	// finish records the position of the end of a phrase.
	// This is defined to be the position of the last
	// character of the last token of the phrase.
	void finish(SourcePosition position) {
	    position.finish = previousTokenPosition.finish;
	}

	void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
		SourcePosition pos = currentToken.position;
		System.out.println("Syntactic Error: " + messageTemplate + " at position " + pos.start);
		errorReporter.reportError(messageTemplate, tokenQuoted, pos);	
		//System.exit(4);
		throw(new SyntaxError());	
	}

	void debug(int kind) {
		System.out.println(Token.spell(kind));
	}

	// parseProgram should return void if its not an AST
	// should return Program if it needs an AST
	// Need to change for pa2 when we use AST
	public void parseProgram() throws SyntaxError{
		System.out.println("Running parseProgram");
		previousTokenPosition.start = 0;
		previousTokenPosition.finish = 0;
		currentToken = Scanner.scan();
		try {
			while(currentToken.kind == Token.CLASS){
				parseClassDeclaration();
				
			}
			if (currentToken.kind != Token.EOT) {
				syntacticError("Unexpected EOT in parseProgram", currentToken.spelling);
			}
		}
		catch (SyntaxError s) { 
			return; 
		}
	}

	//ClassDeclaration ::= class id { ( FieldDeclaration | MethodDeclaration )* }
	public void parseClassDeclaration() throws SyntaxError{
		System.out.println("Running parseClassDeclaration");
		accept(Token.CLASS);
		parseIdentifier();
		accept(Token.LCURLY);
		while(currentToken.kind != Token.RCURLY){
			parseDeclarators();
			parseIdentifier();
			switch(currentToken.kind){
				case Token.SEMICOLON:
					acceptIt();
					break;
				//If the two next tokens arent LPAREN and RPAREN then there should be a another 
				//decleration following the first, if not then there is only 1 decleration
				//Either FieldDeclaration or MethodDecleration
				case Token.LPAREN:  
					acceptIt();
					if(currentToken.kind != Token.RPAREN){
						parseParameterList();
						accept(Token.RPAREN);
					}
					else if (currentToken.kind == Token.RPAREN) {
						accept(Token.RPAREN);
					}
					accept(Token.LCURLY);
					while(currentToken.kind != Token.RETURN && currentToken.kind != Token.RCURLY ){
						parseStatement();
					}
					if(currentToken.kind == Token.RETURN){
						acceptIt();
						parseExpression();
						accept(Token.SEMICOLON);
					}
					accept(Token.RCURLY);
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
		accept(Token.RCURLY);
	}
	
	public void parseIdentifier() throws SyntaxError {
		System.out.println("Running parseIdentifier");
		if (currentToken.kind == Token.IDENTIFIER) {
			previousTokenPosition = currentToken.position;
			System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			if(currentToken.kind == Token.LBRACKET){
				acceptIt();
				if (currentToken.kind == Token.RBRACKET) {
					acceptIt();
					accept(Token.IDENTIFIER);
				}
				else {
					parseExpression();
					accept(Token.RBRACKET);
				}
			}
		} 
		else if (currentToken.kind == Token.BOOLEAN) {
			previousTokenPosition = currentToken.position;
			System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			if(currentToken.kind == Token.LBRACKET){
				acceptIt();
				if (currentToken.kind == Token.RBRACKET) {
					acceptIt();
					accept(Token.IDENTIFIER);
				}
				else {
					parseExpression();
					accept(Token.RBRACKET);
				}
			}
		}
		else if (currentToken.kind == Token.INT) {
			previousTokenPosition = currentToken.position;
			System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			if(currentToken.kind == Token.LBRACKET){
				acceptIt();
				if (currentToken.kind == Token.RBRACKET) {
					acceptIt();
					accept(Token.IDENTIFIER);
				}
				else {
					parseExpression();
					accept(Token.RBRACKET);
					//accept(Token.SEMICOLON);
				}
			}
		}
		else {
			syntacticError("parseIdentifier Error: Identifier Expected here", currentToken.spelling); 
		}
	}
	
	//Visibility ::= ( public | private )?
	//Access ::= static ?
	//Only accepts public, static or private, anything else is rejected
	public void parseDeclarators() throws SyntaxError{
		System.out.println("Running parseDecalarators");
		if(currentToken.kind == Token.PUBLIC || currentToken.kind == Token.PRIVATE || currentToken.kind == Token.STATIC) {
			acceptIt();
		}
		parseType();
	}
	
	//Type ::= int | boolean | id | ( int | id ) [] 
	// if the type if Boolean/void we can just accept
	//if the type is int or id we need to figure out if it is an int/id/int array/id array
	public void parseType() throws SyntaxError{
		System.out.println("Running parseType");
		while (currentToken.kind != Token.IDENTIFIER) {
			switch(currentToken.kind){
				case Token.INT:
					acceptIt();
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						accept(Token.RBRACKET);	
					}
					break;
				case Token.IDENTIFIER:
					parseIdentifier();
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						accept(Token.RBRACKET);	
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
					syntacticError("Unexpected Type: in parseType", currentToken.spelling);
					break;
			}
		}	
	}
	
	public void parseNum() throws SyntaxError {
		System.out.println("Running parseNum");
		if (currentToken.kind == Token.NUM) {
			previousTokenPosition = currentToken.position;
			System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} else {
			syntacticError("Integer literal not found in parseNum", currentToken.spelling);
		}
	}
	
	// ArgumentList ::= Expression ( , Expression )*
	public void parseArgumentList() throws SyntaxError{	
		System.out.println("Running parseArgumentList");
		parseExpression();
		while(currentToken.kind == Token.COMMA){
			acceptIt();
			parseExpression();
		}
	}

	//ParameterList ::= Type id ( , Type id )*
	public void parseParameterList() throws SyntaxError {
		System.out.println("Running parseParameterList");
		parseType();
		parseIdentifier();
		while(currentToken.kind == Token.COMMA){
			acceptIt();
			parseType();
			parseIdentifier();
		}
	}
	
	public void parseStatement() throws SyntaxError{
		System.out.println("Running parseStatement");
		switch(currentToken.kind){
			case Token.LCURLY:
				acceptIt();
				while(currentToken.kind != Token.RCURLY){
					parseStatement();
				}
				accept(Token.RCURLY);
				break;
			case Token.WHILE:
				acceptIt();
				accept(Token.LPAREN);
				parseExpression();
				accept(Token.RPAREN);
				parseStatement();
				break;				
			case Token.IF:
				acceptIt();
				accept(Token.LPAREN);
				parseExpression();
				accept(Token.RPAREN);
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
						accept(Token.EQUALS);
						parseExpression();
						accept(Token.SEMICOLON);
					}
					else {
						parseExpression();
						accept(Token.RBRACKET);
						accept(Token.EQUALS);
						parseExpression();
						accept(Token.SEMICOLON);
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
							accept(Token.RPAREN);
							accept(Token.SEMICOLON);
						}
						else{
							if(currentToken.kind == Token.LBRACKET){
								acceptIt();
								parseExpression();
								accept(Token.RBRACKET);
							}
							accept(Token.EQUALS);
							parseExpression();
							accept(Token.SEMICOLON);
						}
					}				
				}
				else if(currentToken.kind == Token.LPAREN){
					acceptIt();
					if(currentToken.kind != Token.RPAREN){
						parseArgumentList();
					}
					accept(Token.RPAREN);
					accept(Token.SEMICOLON);
				}
				else if(currentToken.kind == Token.EQUALS){
					acceptIt();
					parseExpression();
					accept(Token.SEMICOLON);
				}
				else if(currentToken.kind == Token.IDENTIFIER){
					parseIdentifier();
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS);
						parseExpression();
						accept(Token.SEMICOLON);
					}
					else {
						accept(Token.SEMICOLON);
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
					accept(Token.RPAREN);
					accept(Token.SEMICOLON);
				}
				else {
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						
						parseExpression();
						
						accept(Token.RBRACKET);
					}
					accept(Token.EQUALS);				
					parseExpression();				
					accept(Token.SEMICOLON);
				}
				break;
			case Token.BOOLEAN: 
				parseType();
				parseIdentifier();
				if (currentToken.kind == Token.EQUALS) {
					accept(Token.EQUALS);
					parseExpression();
					accept(Token.SEMICOLON);
				}		
				else {
					accept(Token.SEMICOLON);
				}			
				break;
			case Token.INT: 
				parseType();
				parseIdentifier();
				if (currentToken.kind == Token.EQUALS) {
					accept(Token.EQUALS);
					parseExpression();
					accept(Token.SEMICOLON);
				}
				else {
					accept(Token.SEMICOLON);
				}		
				break;	
			case Token.VOID: 
				parseType();
				parseIdentifier();
				accept(Token.EQUALS);
				parseExpression();
				accept(Token.SEMICOLON);
				break;
			default:
				syntacticError("Expected statement token not found in parseParameterList", currentToken.spelling);
			}	
	}

	// Reference ::= id | this | Reference . id 
	public void parseReference() throws SyntaxError{
		System.out.println("Running parseReference");
		if(currentToken.kind != Token.THIS) {
			parseIdentifier();
		}
		else {
			acceptIt();
			accept(Token.PERIOD);
			parseIdentifier();
		}
	}
	
	//All operators are infix binary operators (binop) with the exception of the prefix unary operators
	//(unop) that consists of logical negation (!), and arithmetic negation (-). The latter is both a unary
	//and binary operator.
	// ! and - will be parsed as a binary operator since that comes first, It shouldn't matter which one it gets read as
	public void parseExpression() throws SyntaxError{
		System.out.println("Running parseExpression");
		parseExpandedExpression();
		while(currentToken.kind == Token.OPERATOR){
			parseBinop(); 
			parseExpandedExpression();
		}
	}
	public void parseExpandedExpression() throws SyntaxError{
		System.out.println("Running parseExpandedExpression");
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
					accept(Token.LBRACKET);
					parseExpression();
					accept(Token.RBRACKET);
				}
				else if (currentToken.kind == Token.LPAREN) {
					acceptIt();
					accept(Token.RPAREN);
				}
				while (currentToken.kind == Token.PERIOD) {
					acceptIt();
					parseExpression();
				}
				break;
			case Token.LPAREN:
				acceptIt();
				parseExpression();
				accept(Token.RPAREN);
				break;
			default:
				parseReference();
				if(currentToken.kind == Token.LPAREN) {
					acceptIt();
					if(currentToken.kind != Token.RPAREN) {
						parseArgumentList();
					}
					accept(Token.RPAREN);
				}
				else if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					parseExpression();
					accept(Token.RBRACKET);
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
		System.out.println("Running parseOp");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} 
		else {
			syntacticError("operator not found", currentToken.spelling);
		}
	}
	public void parseBinop() throws SyntaxError{
		System.out.println("Running parseBinop");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} 
		else {
			syntacticError("Binop not found", currentToken.spelling);
		}
	}
	
	public void parseUnop() throws SyntaxError {
		System.out.println("Running parseUnop");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
		} 
		else {
			syntacticError("Unop not found", currentToken.spelling);
		}
	}
}
