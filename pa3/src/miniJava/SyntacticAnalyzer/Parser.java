package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.SyntaxError;
import miniJava.SyntacticAnalyzer.Scanner;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.RC2ParameterSpec;

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ArrayType;
import miniJava.AbstractSyntaxTrees.AssignStmt;
import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.BinaryExpr;
import miniJava.AbstractSyntaxTrees.BlockStmt;
import miniJava.AbstractSyntaxTrees.BooleanLiteral;
import miniJava.AbstractSyntaxTrees.CallExpr;
import miniJava.AbstractSyntaxTrees.CallStmt;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassDeclList;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declarators;
import miniJava.AbstractSyntaxTrees.ExprList;
import miniJava.AbstractSyntaxTrees.Expression;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.IdRef;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.IfStmt;
import miniJava.AbstractSyntaxTrees.IntLiteral;
import miniJava.AbstractSyntaxTrees.IxAssignStmt;
import miniJava.AbstractSyntaxTrees.IxExpr;
import miniJava.AbstractSyntaxTrees.LiteralExpr;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.NewArrayExpr;
import miniJava.AbstractSyntaxTrees.NewObjectExpr;
import miniJava.AbstractSyntaxTrees.NullExpr;
import miniJava.AbstractSyntaxTrees.Operator;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;
import miniJava.AbstractSyntaxTrees.QualRef;
import miniJava.AbstractSyntaxTrees.RefExpr;
import miniJava.AbstractSyntaxTrees.Reference;
import miniJava.AbstractSyntaxTrees.ReturnStmt;
import miniJava.AbstractSyntaxTrees.Statement;
import miniJava.AbstractSyntaxTrees.StatementList;
import miniJava.AbstractSyntaxTrees.Terminal;
import miniJava.AbstractSyntaxTrees.ThisRef;
import miniJava.AbstractSyntaxTrees.TypeDenoter;
import miniJava.AbstractSyntaxTrees.TypeKind;
import miniJava.AbstractSyntaxTrees.UnaryExpr;
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.AbstractSyntaxTrees.VarDeclStmt;
import miniJava.AbstractSyntaxTrees.Visitor;
import miniJava.AbstractSyntaxTrees.WhileStmt;

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

	public AST parse() throws SyntaxError{
		if(debug==true) System.out.println("Running parse");
		return new Package(parseProgram(), currentToken.position);
	}

	public ClassDeclList parseProgram() throws SyntaxError{
		if(debug==true) System.out.println("Running parseProgram");
		previousTokenPosition.start = 0;
		previousTokenPosition.finish = 0;
		currentToken = Scanner.scan();
		try {
			ClassDeclList list = new ClassDeclList();
			while(currentToken.kind == Token.CLASS){
				list.add(parseClassDeclaration());				
			}			
			if (currentToken.kind != Token.EOT) {				
				syntacticError("Unexpected EOT", currentToken.spelling);
			}			
			return list;
		}
		catch (SyntaxError s) { 
			syntacticError("Unexpected EOT", currentToken.spelling);
			return null; 
		}
	}

	//ClassDeclaration ::= class id { ( FieldDeclaration | MethodDeclaration )* }
	public ClassDecl parseClassDeclaration() throws SyntaxError{
		if(debug==true) System.out.println("Running parseClassDeclaration");
		FieldDeclList fieldList = new FieldDeclList();
		MethodDeclList methodList = new MethodDeclList();
		Token classStart = currentToken;		
		accept(Token.CLASS, "parseClassDeclaration");		
		Identifier classid = parseClassIdentifier();
		accept(Token.LCURLY, "parseClassDeclaration");
		Declarators decls;
		while(currentToken.kind != Token.RCURLY){
			decls = parseDeclarators();
			Identifier name = parseIdentifier();
			Token current;
			switch(currentToken.kind){
				case Token.SEMICOLON:
					current = currentToken;
					if (decls.type.typeKind == TypeKind.VOID){
						syntacticError("Unexpected Token in parseClassDecleration", current.spelling);
					}
					else {
						current = currentToken;
						acceptIt();
						fieldList.add(new FieldDecl(decls.isPrivate, decls.isStatic, decls.type, name.spelling, current.position));
						break;
					}
					break;
				case Token.EQUALS:
					current = currentToken;
					acceptIt();
					parseExpression();
					accept(Token.SEMICOLON, "parseClassDeclaration");
					fieldList.add(new FieldDecl(decls.isPrivate, decls.isStatic, decls.type, name.spelling, current.position));
					break;
				case Token.LPAREN:  
					current = currentToken;
					acceptIt();
					ParameterDeclList parameterList = new ParameterDeclList();
					if(currentToken.kind != Token.RPAREN){
						parameterList = parseParameterList();
						accept(Token.RPAREN, "parseClassDeclaration");
					}
					else if (currentToken.kind == Token.RPAREN) {
						accept(Token.RPAREN, "parseClassDeclaration");
					}					
					accept(Token.LCURLY, "parseClassDeclaration");
					StatementList statementList = new StatementList();					
					while(currentToken.kind != Token.RCURLY){					
						statementList.add(parseStatement());											
					}															
					accept(Token.RCURLY, "parseClassDeclaration");
					MethodDecl temp = new MethodDecl(new FieldDecl(decls.isPrivate, decls.isStatic, decls.type, name.spelling, current.position), parameterList, statementList, current.position);	
					//System.out.println("============= " +temp.statementList.get(3));
					methodList.add(temp);				
					break;
				default: 
					current = currentToken;
					syntacticError("Unexpected Token in parseClassDecleration", current.spelling);
					break;
			}			
		}		
		accept(Token.RCURLY, "parseClassDeclaration");
		return new ClassDecl(classid.spelling, fieldList, methodList, classStart.position);
	}
	
	public Identifier parseIdentifier() throws SyntaxError {
		if(debug==true) System.out.println("Running parseIdentifier");
		Token temp;
		if (currentToken.kind == Token.IDENTIFIER) {
			temp = new Token(Token.IDENTIFIER, currentToken.spelling, currentToken.position);
			accept(Token.IDENTIFIER, "parseIdentifier");
			previousTokenPosition = currentToken.position;
			return new Identifier(temp);
		} 
		else if (currentToken.kind == Token.BOOLEAN) {
			temp = new Token(Token.IDENTIFIER, currentToken.spelling, currentToken.position);
			accept(Token.BOOLEAN, "parseIdentifier");
			previousTokenPosition = currentToken.position;
			return new Identifier(temp);
		}
		else if (currentToken.kind == Token.INT) {
			temp = new Token(Token.IDENTIFIER, currentToken.spelling, currentToken.position);
			accept(Token.INT, "parseIdentifier");
			previousTokenPosition = currentToken.position;
			return new Identifier(temp);
		}
		else {
			syntacticError("Unexpected Token in parseIdentifier", currentToken.spelling); 
		}
		syntacticError("Unexpected Token in parseIdentifier", currentToken.spelling);
		return null;
	}

	public Identifier parseClassIdentifier() throws SyntaxError {
		if(debug==true) System.out.println("Running parseIdentifier");
		Token temp;
		if (currentToken.kind == Token.IDENTIFIER) {
			temp = new Token(Token.IDENTIFIER, currentToken.spelling, currentToken.position);
			accept(Token.IDENTIFIER, "parseIdentifier");
			previousTokenPosition = currentToken.position;
			return new Identifier(temp);
		} 
		else {
			syntacticError("Unexpected Token in parseIdentifier", currentToken.spelling); 
		}
		return null;
	}
	
	//Visibility ::= ( public | private )?
	//Access ::= static ?
	//Only accepts public, static or private, anything else is rejected
	public Declarators parseDeclarators() throws SyntaxError{
		if(debug==true) System.out.println("Running parseDecalarators");
		boolean isPrivate = false;
		boolean isStatic = false;
		if (currentToken.kind == Token.PRIVATE) {
			acceptIt();
			isPrivate = true;
		}
		else if (currentToken.kind == Token.PUBLIC){
			acceptIt();
			isPrivate = false;
		}
		if (currentToken.kind == Token.STATIC) {
			acceptIt();
			isStatic = true;
		}
		else {
			isStatic = false;
		}
		Identifier id = new Identifier(currentToken);
		return new Declarators(isPrivate, isStatic, parseType(), id.spelling, currentToken.position);
	}
	
	//Type ::= int | boolean | id | ( int | id ) [] 
	// if the type if Boolean/void we can just accept
	//if the type is int or id we need to figure out if it is an int/id/int array/id array
	public TypeDenoter parseType() throws SyntaxError{
		if(debug==true) System.out.println("Running parseType");
		Token current = null;
		switch(currentToken.kind){
			case Token.INT:
				current = currentToken;
				acceptIt();
				if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					accept(Token.RBRACKET, "parseType");	
					return new ArrayType(new BaseType(TypeKind.INT, current.position), current.position);
				}
				else {
					return new BaseType(TypeKind.INT, currentToken.position);
				}					
			case Token.IDENTIFIER:
				current = currentToken;
				parseIdentifier();
				if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					accept(Token.RBRACKET, "parseType");	
					return new ArrayType(new ClassType(new Identifier(current), current.position), current.position);					}
				else {
					return new ClassType(new Identifier(currentToken), current.position);					
				}	
			case Token.BOOLEAN: 
				current = currentToken;
				acceptIt();
				if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					accept(Token.RBRACKET, "parseType");
					return new ArrayType(new BaseType(TypeKind.BOOLEAN, current.position), current.position);					
				}
				else {
					return new BaseType(TypeKind.BOOLEAN, current.position);					
				}	
			case Token.VOID:
				current = currentToken;
				acceptIt();
				return new BaseType(TypeKind.VOID, current.position);
			default:
				syntacticError("Unexpected Token in parseType", currentToken.spelling);
				return new BaseType(TypeKind.ERROR, currentToken.position);		
		}
	}
	
	public IntLiteral parseNum() throws SyntaxError {
		if(debug==true) System.out.println("Running parseNum");
		if (currentToken.kind == Token.NUM) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			return new IntLiteral(currentToken);
		} else {
			syntacticError("Unexpected Token in parseNum", currentToken.spelling);
			return null;
		}
	}
	
	// ArgumentList ::= Expression ( , Expression )*
	public ExprList parseArgumentList() throws SyntaxError{	
		if(debug==true) System.out.println("Running parseArgumentList");
		ExprList arguments = new ExprList();
		arguments.add(parseExpression());
		while(currentToken.kind == Token.COMMA){
			acceptIt();
			arguments.add(parseExpression());
		}
		return arguments;
	}

	//ParameterList ::= Type id ( , Type id )*
	public ParameterDeclList parseParameterList() throws SyntaxError {
		if(debug==true) System.out.println("Running parseParameterList");
		ParameterDeclList parameters = new ParameterDeclList();
		if (currentToken.kind == Token.VOID) {
			syntacticError("Unexpected Token in parseParameterList", currentToken.spelling);
			return null;
		}
		else {
			parameters.add(new ParameterDecl(parseType(), parseIdentifier().spelling, currentToken.position));
		}	
		while(currentToken.kind == Token.COMMA){			
			acceptIt();
			if (currentToken.kind == Token.VOID) {
				syntacticError("Unexpected Token in parseParameterList", currentToken.spelling);
				return null;
			}
			else {
				parameters.add(new ParameterDecl(parseType(), parseIdentifier().spelling, currentToken.position));
			}			
		}
		return parameters;
	}
	
	public Statement parseStatement() throws SyntaxError{
		if(debug==true) System.out.println("Running parseStatement");
		Token current;
		switch(currentToken.kind){
			case Token.LCURLY:
				current = currentToken;
				acceptIt();
				StatementList list = new StatementList();
				while(currentToken.kind != Token.RCURLY){
					list.add(parseStatement());
				} 
				accept(Token.RCURLY, "parseStatement");
				return new BlockStmt(list, current.position);
			case Token.WHILE:
				current = currentToken;
				acceptIt();
				accept(Token.LPAREN, "parseStatement");
				Expression expr = parseExpression();
				accept(Token.RPAREN, "parseStatement");
				return new WhileStmt(expr, parseStatement(), current.position);			
			case Token.IF:
				current = currentToken;
				acceptIt();
				accept(Token.LPAREN, "parseStatement");
				Expression cond = parseExpression();
				accept(Token.RPAREN, "parseStatement");
				Statement first = parseStatement();
				if(currentToken.kind == Token.ELSE){
					acceptIt();
					Statement second = parseStatement();
					return new IfStmt(cond, first, second, current.position);
				}
				else {
					return new IfStmt(cond, first, current.position);
				}	
			case Token.IDENTIFIER:
				current = currentToken;
				Reference id = parseReference();
				if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					if(currentToken.kind == Token.RBRACKET){
						acceptIt();
						Reference name = null;
						if (currentToken.kind == Token.IDENTIFIER) {
							name = parseReference();
						}				
						Expression arrayExpr;		
						if (currentToken.kind == Token.EQUALS) {
							accept(Token.EQUALS, "parseStatement");
							arrayExpr = parseExpression();
							accept(Token.SEMICOLON, "parseStatement");
							return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.CLASS, currentToken.position), current.spelling, current.position), arrayExpr, current.position);
						}
						else if (currentToken.kind == Token.SEMICOLON) {
							accept(Token.SEMICOLON, "parseStatement");
							return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.CLASS, currentToken.position), current.spelling, current.position), null, current.position);
						}				
					}
					else {
						Expression i = parseExpression();
						accept(Token.RBRACKET, "parseStatement");
						accept(Token.EQUALS, "parseStatement");					
						Expression arrayExpr = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new IxAssignStmt(id, i, arrayExpr, current.position);									
					}
				}
				else if(currentToken.kind == Token.PERIOD){					
					acceptIt();
					Reference ref = parseReference();					
					if(currentToken.kind == Token.LPAREN){
						acceptIt();
						ExprList exprList = new ExprList();
						if(currentToken.kind != Token.RPAREN){
							exprList = parseArgumentList();
						}
						accept(Token.RPAREN, "parseStatement");
						accept(Token.SEMICOLON, "parseStatement");
						return new CallStmt(ref, exprList, current.position);
					}
					else if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						if(currentToken.kind == Token.RBRACKET){
							acceptIt();
							Reference name = null;
							if (currentToken.kind == Token.IDENTIFIER) {
								name = parseReference();
							}				
							Expression arrayExpr;		
							if (currentToken.kind == Token.EQUALS) {
								accept(Token.EQUALS, "parseStatement");
								arrayExpr = parseExpression();
								accept(Token.SEMICOLON, "parseStatement");
								return new IxAssignStmt(name, null, arrayExpr, current.position);
							}
							else if (currentToken.kind == Token.SEMICOLON) {
								accept(Token.SEMICOLON, "parseStatement");
								return new IxAssignStmt(name, null, null, current.position);
							}				
						}
						else {
							Expression i = parseExpression();
							accept(Token.RBRACKET, "parseStatement");
							accept(Token.EQUALS, "parseStatement");					
							Expression arrayExpr = parseExpression();
							accept(Token.SEMICOLON, "parseStatement");
							return new IxAssignStmt(id, i, arrayExpr, current.position);									
						}							
					}
					else if (currentToken.kind == Token.EQUALS){
						acceptIt();
						Expression periodExpr = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new AssignStmt(ref, periodExpr, current.position);
					}							
				}
				else if(currentToken.kind == Token.LPAREN){
					acceptIt();
					ExprList exprList = new ExprList();
					if(currentToken.kind != Token.RPAREN){
						exprList = parseArgumentList();
					}
					accept(Token.RPAREN, "parseStatement");
					accept(Token.SEMICOLON, "parseStatement");
					return new CallStmt(id, exprList, current.position);
				}
				else if(currentToken.kind == Token.EQUALS){
					acceptIt();
					Expression assign = parseExpression();
					accept(Token.SEMICOLON, "parseStatement");
					return new AssignStmt(id, assign, current.position);
				}
				else if(currentToken.kind == Token.IDENTIFIER){
					Reference ref = parseReference();
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						Expression classExpr = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.CLASS, currentToken.position), current.spelling, current.position), classExpr, current.position);
					}
					else {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.CLASS, currentToken.position), current.spelling, current.position), null, current.position);
					}
				}
				break;
			case Token.THIS:
				current = currentToken;
				Reference ref = parseReference();
				if (currentToken.kind == Token.PERIOD) {
					accept(Token.PERIOD, "parseStatement");
					Identifier thisId = parseIdentifier();
					if(currentToken.kind == Token.LPAREN){
						acceptIt();
						ExprList exprList = new ExprList();
						if(currentToken.kind != Token.RPAREN){
							exprList = parseArgumentList();
							accept(Token.RPAREN, "parseStatement");
							accept(Token.SEMICOLON, "parseStatement");
							return new CallStmt(ref, exprList, current.position);
						}
						else {
							accept(Token.RPAREN, "parseStatement");
							accept(Token.SEMICOLON, "parseStatement");
							return new CallStmt(new ThisRef(current.position), exprList, current.position);
						}					
					}
					else if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						Expression expr1 = null;
						Expression expr2 = null;
						if (currentToken.kind == Token.RBRACKET) {						
							accept(Token.RBRACKET, "parseStatement");
						}
						else {
							expr1 = parseExpression();						
							accept(Token.RBRACKET, "parseStatement");
						}										
						accept(Token.EQUALS, "parseStatement");				
						expr2 = parseExpression();				
						accept(Token.SEMICOLON, "parseStatement");
						return new IxAssignStmt(new QualRef(new ThisRef(current.position), thisId, current.position), expr1, expr2, current.position);
					}
					break;
				}
				else if (currentToken.kind == Token.LBRACKET) {
					acceptIt();
					Expression rightExpr = null;
					if (currentToken.kind == Token.RBRACKET) {
						acceptIt();
						accept(Token.EQUALS, "parseStatement");
						rightExpr = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new AssignStmt(new ThisRef(current.position), rightExpr, current.position);
					}
					else {
						Expression i = parseExpression();
						accept(Token.RBRACKET, "parseStatement");
						accept(Token.EQUALS, "parseStatement");
						rightExpr = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new IxAssignStmt(new ThisRef(current.position), i, rightExpr, current.position);
					}
				}
				else if (currentToken.kind == Token.LPAREN) {
					acceptIt();
					if (currentToken.kind == Token.RPAREN) {
						acceptIt();
						accept(Token.SEMICOLON, "parseStatement");
						return new CallStmt(new ThisRef(current.position), new ExprList(), current.position);
					}
					else {
						ExprList equalsExpr = parseArgumentList();
						accept(Token.RPAREN, "parseStatement");
						accept(Token.SEMICOLON, "parseStatement");
						return new CallStmt(new ThisRef(current.position), equalsExpr, current.position);
					}										
				}		
				else if (currentToken.kind == Token.EQUALS) {
					acceptIt();
					Expression equalsExpr = parseExpression();
					accept(Token.SEMICOLON, "parseStatement");
					return new AssignStmt(new ThisRef(current.position), equalsExpr, current.position);
				}						
			case Token.BOOLEAN:
				current = currentToken; 
				parseType();
				Expression i_boolean = null;
				Expression e_boolean = null;
				Identifier id_boolean = null;
				if (currentToken.kind == Token.RBRACKET) {						
					accept(Token.RBRACKET, "parseStatement");
					id_boolean = parseIdentifier();
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						e_boolean = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.BOOLEAN, current.position), id_boolean.spelling, current.position), e_boolean, current.position);
					}
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new ArrayType(new BaseType(TypeKind.BOOLEAN, current.position), current.position), id_boolean.spelling, current.position), e_boolean, current.position);
					}
				}
				else {
					id_boolean = parseIdentifier();					
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						e_boolean = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.BOOLEAN, current.position), id_boolean.spelling, current.position), e_boolean, current.position);
					}		
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.BOOLEAN, current.position), id_boolean.spelling, current.position), null, current.position);
					}		
				}		
				return null;				
			case Token.INT: 
				current = currentToken;				
				parseType();
				Expression i_int = null;
				Expression e_int = null;
				Identifier id_int = null;
				if (currentToken.kind == Token.RBRACKET) {						
					accept(Token.RBRACKET, "parseStatement");
					id_int = parseIdentifier();
					if (currentToken.kind == Token.EQUALS) {						
						accept(Token.EQUALS, "parseStatement");
						e_int = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.INT, current.position), id_int.spelling, current.position), e_int, current.position);
					}
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new ArrayType(new BaseType(TypeKind.INT, current.position), current.position), id_int.spelling, current.position), e_int, current.position);
					}
				}
				else {
					id_int = parseIdentifier();					
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						e_int = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.INT, current.position), id_int.spelling, current.position), e_int, current.position);
					}		
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.INT, current.position), id_int.spelling, current.position), null, current.position);
					}		
				}		
				return null;
			case Token.RETURN:
				current = currentToken;
				acceptIt();
				if (currentToken.kind  != Token.SEMICOLON) {
					Expression returned = parseExpression();
					accept(Token.SEMICOLON, "parseClassDeclaration");
					return new ReturnStmt(returned, current.position);
				}
				else {
					accept(Token.SEMICOLON, "parseClassDeclaration");
					return new ReturnStmt(null, current.position);
				}	
			case Token.VOID:
				syntacticError("Unexpected Token in parseStatement", currentToken.spelling);
				return null;
			default:
				current = currentToken;
				syntacticError("Unexpected Token in parseParameterList", current.spelling);
			}
		return null;
	}

	// Reference ::= id | this | Reference . id 
	public Reference parseReference() throws SyntaxError {
		if(debug==true) System.out.println("Running parseReference");
		Token current = currentToken;
		if(currentToken.kind == Token.THIS) {
			Token thisToken = currentToken;
			acceptIt();
			if (currentToken.kind == Token.PERIOD) {
				acceptIt();
				return new QualRef(new ThisRef(thisToken.position), parseIdentifier(), current.position);
			}
			else {
				return new ThisRef(current.position);
			}
		}
		else {		
			Identifier ref = parseIdentifier();		
			if (currentToken.kind == Token.PERIOD) {
				acceptIt();
				return new QualRef(parseReference(), ref, current.position);
			}
			else {				
				return new IdRef(ref, current.position);							
			}
		}
	}
	
	public Expression parseExpression() throws SyntaxError{
		if(debug==true) System.out.println("Running parseExpression");
		Token current = currentToken;
		Expression expr1 = parseExpandedExpression();
		if (Token.isBinop(currentToken.spelling)) {
			Operator op = parseBinop(); 
			BinaryExpr binExpr = new BinaryExpr(op, expr1, parseExpression(), current.position);
			while(Token.isBinop(currentToken.spelling)){
				Token innerCurrent = currentToken;
				Operator opLocal = parseBinop(); 
				return new BinaryExpr(opLocal, binExpr, parseExpandedExpression(), innerCurrent.position);
			}
			return binExpr;
		}
		else if (Token.isUnop(currentToken.spelling)){
			syntacticError("unexpecte token in parseExpression", currentToken.spelling);
			return null;
			//return new UnaryExpr(parseUnop(), parseExpression(), currentToken.position);
		}		
		else {
			return expr1;
		}
	}
	public Expression parseExpandedExpression() throws SyntaxError{
		if(debug==true) System.out.println("Running parseExpandedExpression");
		Token current;
		switch(currentToken.kind) {
			case Token.FALSE:
				current = currentToken;
				acceptIt();
				return new LiteralExpr(new BooleanLiteral(current), current.position);
			case Token.TRUE:
				current = currentToken;
				acceptIt();
				return new LiteralExpr(new BooleanLiteral(current), current.position);	
			case Token.NUM:
				current = currentToken;
				parseNum();
				return new LiteralExpr(new IntLiteral(current), current.position);
			case Token.OPERATOR:
				current = currentToken;
				if (Token.isUnop(currentToken.spelling)) {
					return new UnaryExpr(parseUnop(), parseExpandedExpression(), current.position);
				}				
			case Token.NEW:		
				current = currentToken;
				acceptIt();
				Identifier id = parseIdentifier();
				if(currentToken.kind == Token.LBRACKET) {
					accept(Token.LBRACKET, "parseExpandedExpression");
					if (currentToken.kind == Token.RBRACKET) {
						accept(Token.RBRACKET, "parseExpandedExpression");
						switch(id.spelling) {
							case "int":
								return new NewArrayExpr(new BaseType(TypeKind.INT, current.position), null, current.position);
							case "boolean":
								return new NewArrayExpr(new BaseType(TypeKind.BOOLEAN, current.position), null, current.position);
							case "<id>":
								return new NewArrayExpr(new ClassType(id, current.position), null, current.position);
						}						
					}
					else {						
						Expression expr = parseExpression();
						accept(Token.RBRACKET, "parseExpandedExpression");
						switch(id.spelling) {
							case "int":
								return new NewArrayExpr(new BaseType(TypeKind.INT, current.position), expr, current.position);
							case "boolean":
								return new NewArrayExpr(new BaseType(TypeKind.BOOLEAN, current.position), expr, current.position);
							default:							
								return new NewArrayExpr(new ClassType(id, current.position), expr, current.position);
						}
					}										
				}
				else if (currentToken.kind == Token.LPAREN) {
					accept(Token.LPAREN, "parseExpandedExpression");
					if (currentToken.kind == Token.RPAREN) {
						accept(Token.RPAREN, "parseExpandedExpression");
						return new NewObjectExpr(new ClassType(id, current.position), current.position);
					}
					else {
						ExprList argList = parseArgumentList();
						accept(Token.RPAREN, "parseExpandedExpression");					
						return new NewObjectExpr(new ClassType(id, current.position), current.position);
					}					
				}
			case Token.LPAREN:
				current = currentToken;
				acceptIt();
				Expression expr = parseExpression();
				accept(Token.RPAREN, "parseExpandedExpression");
				return expr;
			case Token.THIS:
				current = currentToken;
				Reference ref = parseReference();
				if(currentToken.kind == Token.LPAREN) {
					acceptIt();
					ExprList argList = new ExprList();
					if(currentToken.kind != Token.RPAREN) {
						argList = parseArgumentList();
					}
					accept(Token.RPAREN, "parseExpandedExpression");
					return new CallExpr(ref, argList, current.position);
				}
				else if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					Expression exprLocal = parseExpression();
					accept(Token.RBRACKET, "parseExpandedExpression");
					return new IxExpr(ref, exprLocal, current.position);
				}
				else if (currentToken.kind == Token.PERIOD){	
					Identifier idRef = null;
					while(currentToken.kind == Token.PERIOD)  {
						acceptIt();
						idRef = parseIdentifier();
					}	
					return new RefExpr(ref, current.position);				 					
				}
				else {
					return new RefExpr(ref, current.position);
				}
			case Token.NULL:
				current = currentToken;
				acceptIt();
				return new NullExpr(current.position);
			default:
				current = currentToken;
				System.out.println("Here-----------------------"  + current.spelling);
				Reference r = parseReference();
				if(currentToken.kind == Token.LPAREN) {
					acceptIt();
					ExprList argList = new ExprList();
					if(currentToken.kind != Token.RPAREN) {
						argList = parseArgumentList();
					}
					accept(Token.RPAREN, "parseExpandedExpression");
					return new CallExpr(r, argList, current.position);
				}
				else if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					Expression exprLocal = parseExpression();
					accept(Token.RBRACKET, "parseExpandedExpression");
					return new IxExpr(r, exprLocal, current.position);
				}
				else if (currentToken.kind == Token.PERIOD){	
					Identifier idRef = null;
					while(currentToken.kind == Token.PERIOD)  {
						acceptIt();
						idRef = parseIdentifier();
					}	
					return new RefExpr(r, current.position);				 					
				}				
				else {
					return new RefExpr(r, current.position);
				}
		}
	}
	public Operator parseOp() throws SyntaxError {
		if(debug==true) System.out.println("Running parseOp");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			return new Operator(currentToken);
		} 
		else {
			syntacticError("Unexpected Token in parseOp", currentToken.spelling);
			return null;
		}
	}
	public Operator parseBinop() throws SyntaxError{
		if(debug==true) System.out.println("Running parseBinop");
		if (currentToken.kind == Token.OPERATOR) {
			if(debug==true) System.out.println(currentToken.spelling);
			previousTokenPosition = currentToken.position;
			Token current = currentToken;			
			currentToken = Scanner.scan();
			return new Operator(current);
		} 
		else {
			syntacticError("Unexpected Token in parseBinop", currentToken.spelling);
			return null;
		}
	}
	
	public Operator parseUnop() throws SyntaxError {
		if(debug==true) System.out.println("Running parseUnop");
		if (currentToken.kind == Token.OPERATOR) {
			if(debug==true) System.out.println(currentToken.spelling);
			previousTokenPosition = currentToken.position;			
			Token current = currentToken;
			currentToken = Scanner.scan();
			return new Operator(current);
		} 
		else {
			syntacticError("Unexpected Token in parseUnop", currentToken.spelling);
			return null;
		}
	}
}
