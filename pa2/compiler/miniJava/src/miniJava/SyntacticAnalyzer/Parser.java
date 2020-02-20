package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.SyntaxError;
import miniJava.SyntacticAnalyzer.Scanner;

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
			return null; 
		}
	}

	//ClassDeclaration ::= class id { ( FieldDeclaration | MethodDeclaration )* }
	public ClassDecl parseClassDeclaration() throws SyntaxError{
		FieldDeclList fieldList = new FieldDeclList();
		MethodDeclList methodList = new MethodDeclList();

		if(debug==true) System.out.println("Running parseClassDeclaration");
		accept(Token.CLASS, "parseClassDeclaration");
		parseIdentifier();
		accept(Token.LCURLY, "parseClassDeclaration");
		Declarators decls;
		while(currentToken.kind != Token.RCURLY){
			decls = parseDeclarators();
			parseIdentifier();
			switch(currentToken.kind){
				case Token.SEMICOLON:
					acceptIt();
					fieldList.add(new FieldDecl(decls.isPrivate, decls.isStatic, decls.type, currentToken.spelling, currentToken.position));
					break;
				case Token.EQUALS:
					acceptIt();
					parseExpression();
					accept(Token.SEMICOLON, "parseClassDeclaration");
					fieldList.add(new FieldDecl(decls.isPrivate, decls.isStatic, decls.type, currentToken.spelling, currentToken.position));
					break;
				case Token.LPAREN:  
					acceptIt();
					ParameterDeclList parameterList = null;
					if(currentToken.kind != Token.RPAREN){
						parameterList = parseParameterList();
						accept(Token.RPAREN, "parseClassDeclaration");
					}
					else if (currentToken.kind == Token.RPAREN) {
						accept(Token.RPAREN, "parseClassDeclaration");
					}
					accept(Token.LCURLY, "parseClassDeclaration");

					StatementList statementList = new StatementList();
					while(currentToken.kind != Token.RETURN && currentToken.kind != Token.RCURLY ){
						statementList.add(parseStatement());
					}					
					accept(Token.RCURLY, "parseClassDeclaration");
					methodList.add(new MethodDecl(null, parameterList, statementList, currentToken.position));
					break;
				default: 
					syntacticError("Unexpected Token in parseClassDecleration", currentToken.spelling);
					break;
			}
		}
		accept(Token.RCURLY, "parseClassDeclaration");
		ClassDecl declaration = new ClassDecl(null, fieldList, methodList, currentToken.position);
		return declaration;
	}
	
	public Identifier parseIdentifier() throws SyntaxError {
		if(debug==true) System.out.println("Running parseIdentifier");
		if (currentToken.kind == Token.IDENTIFIER) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			return new Identifier(currentToken);
		} 
		else if (currentToken.kind == Token.BOOLEAN) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			return new Identifier(currentToken);
		}
		else if (currentToken.kind == Token.INT) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			return new Identifier(currentToken);
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
		boolean isPrivate;
		boolean isStatic;
		if (currentToken.kind == Token.PRIVATE) {
			acceptIt();
			isPrivate = true;
		}
		else {
			isPrivate = false;
		}
		if (currentToken.kind == Token.STATIC) {
			acceptIt();
			isStatic = true;
		}
		else {
			isStatic = false;
		}
		return new Declarators(isPrivate, isStatic, parseType(), currentToken.spelling, currentToken.position);
	}
	
	//Type ::= int | boolean | id | ( int | id ) [] 
	// if the type if Boolean/void we can just accept
	//if the type is int or id we need to figure out if it is an int/id/int array/id array
	public TypeDenoter parseType() throws SyntaxError{
		if(debug==true) System.out.println("Running parseType");
		while (currentToken.kind != Token.IDENTIFIER) {
			switch(currentToken.kind){
				case Token.INT:
					acceptIt();
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						accept(Token.RBRACKET, "parseType");	
						return new ArrayType(new BaseType(TypeKind.INT, currentToken.position), currentToken.position);
					}
					else {
						return new BaseType(TypeKind.INT, currentToken.position);
					}					
				case Token.IDENTIFIER:
					parseIdentifier();
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						accept(Token.RBRACKET, "parseType");	
						return new ArrayType(new ClassType(new Identifier(currentToken), currentToken.position), currentToken.position);					}
					else {
						return new ClassType(new Identifier(currentToken), currentToken.position);					
					}	
				case Token.BOOLEAN: 
					acceptIt();
					if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						accept(Token.RBRACKET, "parseType");
						return new ArrayType(new BaseType(TypeKind.BOOLEAN, currentToken.position), currentToken.position);					
					}
					else {
						return new BaseType(TypeKind.BOOLEAN, currentToken.position);					
					}	
				case Token.VOID: 
					acceptIt();
					return new BaseType(TypeKind.VOID, currentToken.position);
				default:
					syntacticError("Unexpected Token in parseType", currentToken.spelling);
					return new BaseType(TypeKind.ERROR, currentToken.position);		
			}
		}
		return null;
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
		parameters.add(new ParameterDecl(parseType(), parseIdentifier().spelling, currentToken.position));
		while(currentToken.kind == Token.COMMA){
			acceptIt();
			parameters.add(new ParameterDecl(parseType(), parseIdentifier().spelling, currentToken.position));
		}
		return parameters;
	}
	
	public Statement parseStatement() throws SyntaxError{
		if(debug==true) System.out.println("Running parseStatement");
		switch(currentToken.kind){
			case Token.LCURLY:
				acceptIt();
				StatementList list = new StatementList();
				while(currentToken.kind != Token.RCURLY){
					list.add(parseStatement());
				} 
				accept(Token.RCURLY, "parseStatement");
				return new BlockStmt(list, currentToken.position);
			case Token.WHILE:
				acceptIt();
				accept(Token.LPAREN, "parseStatement");
				Expression expr = parseExpression();
				accept(Token.RPAREN, "parseStatement");
				return new WhileStmt(expr, parseStatement(), currentToken.position);			
			case Token.IF:
				acceptIt();
				accept(Token.LPAREN, "parseStatement");
				Expression cond = parseExpression();
				accept(Token.RPAREN, "parseStatement");
				Statement first = parseStatement();
				if(currentToken.kind == Token.ELSE){
					acceptIt();
					Statement second = parseStatement();
					return new IfStmt(cond, first, second, currentToken.position);
				}
				else {
					return new IfStmt(cond, first, currentToken.position);
				}	
			case Token.IDENTIFIER:
				Reference id = parseReference();
				if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					if(currentToken.kind == Token.RBRACKET){
						acceptIt();
						Reference name = null;
						if (currentToken.kind == Token.IDENTIFIER) {
							name = parseReference();
						}				
						Expression arrayExpr = null;		
						if (currentToken.kind == Token.EQUALS) {
							accept(Token.EQUALS, "parseStatement");
							arrayExpr = parseExpression();
							accept(Token.SEMICOLON, "parseStatement");
							return new IxAssignStmt(name, null, arrayExpr, currentToken.position);
						}
						else if (currentToken.kind == Token.SEMICOLON) {
							accept(Token.SEMICOLON, "parseStatement");
							return new IxAssignStmt(name, null, arrayExpr, currentToken.position);
						}				
					}
					else {
						Expression i = parseExpression();
						accept(Token.RBRACKET, "parseStatement");
						accept(Token.EQUALS, "parseStatement");					
						Expression arrayExpr = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new IxAssignStmt(id, i, arrayExpr, currentToken.position);									
					}
				}
				else if(currentToken.kind == Token.PERIOD){					
					acceptIt();
					Reference ref = parseReference();					
					if(currentToken.kind == Token.LPAREN){
						acceptIt();
						ExprList exprList = null;
						if(currentToken.kind != Token.RPAREN){
							exprList = parseArgumentList();
						}
						accept(Token.RPAREN, "parseStatement");
						accept(Token.SEMICOLON, "parseStatement");
						return new CallStmt(ref, exprList, currentToken.position);
					}
					else if(currentToken.kind == Token.LBRACKET){
						acceptIt();
						if(currentToken.kind == Token.RBRACKET){
							acceptIt();
							Reference name = null;
							if (currentToken.kind == Token.IDENTIFIER) {
								name = parseReference();
							}				
							Expression arrayExpr = null;		
							if (currentToken.kind == Token.EQUALS) {
								accept(Token.EQUALS, "parseStatement");
								arrayExpr = parseExpression();
								accept(Token.SEMICOLON, "parseStatement");
								return new IxAssignStmt(name, null, arrayExpr, currentToken.position);
							}
							else if (currentToken.kind == Token.SEMICOLON) {
								accept(Token.SEMICOLON, "parseStatement");
								return new IxAssignStmt(name, null, arrayExpr, currentToken.position);
							}				
						}
						else {
							Expression i = parseExpression();
							accept(Token.RBRACKET, "parseStatement");
							accept(Token.EQUALS, "parseStatement");					
							Expression arrayExpr = parseExpression();
							accept(Token.SEMICOLON, "parseStatement");
							return new IxAssignStmt(id, i, arrayExpr, currentToken.position);									
						}							
					}							
				}
				else if(currentToken.kind == Token.LPAREN){
					acceptIt();
					ExprList exprList = null;
					if(currentToken.kind != Token.RPAREN){
						exprList = parseArgumentList();
					}
					accept(Token.RPAREN, "parseStatement");
					accept(Token.SEMICOLON, "parseStatement");
					return new CallStmt(id, exprList, currentToken.position);
				}
				else if(currentToken.kind == Token.EQUALS){
					acceptIt();
					Expression assign = parseExpression();
					accept(Token.SEMICOLON, "parseStatement");
					return new AssignStmt(id, assign, currentToken.position);
				}
				else if(currentToken.kind == Token.IDENTIFIER){
					Reference ref = parseReference();
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						Expression classExpr = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.CLASS, currentToken.position), currentToken.spelling, currentToken.position), classExpr, currentToken.position);
					}
					else {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.CLASS, currentToken.position), currentToken.spelling, currentToken.position), null, currentToken.position);
					}
				}
				break;
			case Token.THIS:
				acceptIt();
				accept(Token.PERIOD, "parseStatement");
				Reference ref = parseReference();
				if(currentToken.kind == Token.LPAREN){
					acceptIt();
					ExprList exprList = null;
					if(currentToken.kind != Token.RPAREN){
						exprList = parseArgumentList();
						accept(Token.RPAREN, "parseStatement");
						accept(Token.SEMICOLON, "parseStatement");
						return new CallStmt(ref, exprList, currentToken.position);
					}
					else {
						accept(Token.RPAREN, "parseStatement");
						accept(Token.SEMICOLON, "parseStatement");
						return new CallStmt(new ThisRef(currentToken.position), exprList, currentToken.position);
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
					return new IxAssignStmt(new ThisRef(currentToken.position), expr1, expr2, currentToken.position);
				}
				break;
			case Token.BOOLEAN: 
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
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.BOOLEAN, currentToken.position), id_boolean.spelling, currentToken.position), e_boolean, currentToken.position);
					}
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new ArrayType(new BaseType(TypeKind.BOOLEAN, currentToken.position), currentToken.position), id_boolean.spelling, currentToken.position), e_boolean, currentToken.position);
					}
				}
				else {
					id_boolean = parseIdentifier();					
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						e_boolean = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.BOOLEAN, currentToken.position), id_boolean.spelling, currentToken.position), e_boolean, currentToken.position);
					}		
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.BOOLEAN, currentToken.position), id_boolean.spelling, currentToken.position), null, currentToken.position);
					}		
				}		
				return null;				
			case Token.INT: 
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
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.INT, currentToken.position), id_int.spelling, currentToken.position), e_int, currentToken.position);
					}
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new ArrayType(new BaseType(TypeKind.INT, currentToken.position), currentToken.position), id_int.spelling, currentToken.position), e_int, currentToken.position);
					}
				}
				else {
					id_int = parseIdentifier();					
					if (currentToken.kind == Token.EQUALS) {
						accept(Token.EQUALS, "parseStatement");
						e_int = parseExpression();
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.INT, currentToken.position), id_int.spelling, currentToken.position), e_int, currentToken.position);
					}		
					else if (currentToken.kind == Token.SEMICOLON) {
						accept(Token.SEMICOLON, "parseStatement");
						return new VarDeclStmt(new VarDecl(new BaseType(TypeKind.INT, currentToken.position), id_int.spelling, currentToken.position), null, currentToken.position);
					}		
				}		
				return null;
			// case Token.VOID: 
			// 	parseType();
			// 	parseIdentifier();
			// 	accept(Token.EQUALS, "parseStatement");
			// 	parseExpression();
			// 	accept(Token.SEMICOLON, "parseStatement");
			// 	return new AssignStmt(r, e, posn)
			// 	break;
			case Token.RETURN:
				acceptIt();
				Expression returned = parseExpression();
				accept(Token.SEMICOLON, "parseClassDeclaration");
				return new ReturnStmt(returned, currentToken.position);
			default:
				syntacticError("Unexpected Token in parseParameterList", currentToken.spelling);
			}
		return null;
	}

	// Reference ::= id | this | Reference . id 
	public Reference parseReference() throws SyntaxError {
		if(debug==true) System.out.println("Running parseReference");
		if(currentToken.kind == Token.THIS) {
			acceptIt();
			accept(Token.PERIOD, "parseReference");
			return new ThisRef(currentToken.position);
		}
		else {	
			Reference ref = parseReference();
			if (currentToken.kind == Token.PERIOD) {
				acceptIt();
				return new QualRef(ref, parseIdentifier(), currentToken.position);
			}
			else {
				return new IdRef(parseIdentifier(), currentToken.position);
			}
		}
	}
	
	public Expression parseExpression() throws SyntaxError{
		if(debug==true) System.out.println("Running parseExpression");
		Expression expr1 = parseExpandedExpression();
		if (currentToken.kind == Token.OPERATOR) {
			if (Token.isBinop(currentToken.spelling)) {
				Operator op = parseBinop(); 
				Expression expr2 = parseExpandedExpression();
				BinaryExpr binExpr = new BinaryExpr(op, expr1, expr2, currentToken.position);
				while(currentToken.kind == Token.OPERATOR){
					Operator opLocal = parseBinop(); 
					Expression exprLocal = parseExpandedExpression();
					binExpr = new BinaryExpr(opLocal, binExpr, exprLocal, currentToken.position);
				}
				return binExpr;
			}
			else if (Token.isUnop(currentToken.spelling)){
				return new UnaryExpr(parseUnop(), parseExpression(), currentToken.position);
			}
		}
		else {
			return expr1;
		}
		return null;
	}
	public Expression parseExpandedExpression() throws SyntaxError{
		if(debug==true) System.out.println("Running parseExpandedExpression");
		switch(currentToken.kind) {
			case Token.FALSE:
				acceptIt();
				return new LiteralExpr(new IntLiteral(currentToken), currentToken.position);
			case Token.TRUE:
				acceptIt();
				return new LiteralExpr(new IntLiteral(currentToken), currentToken.position);	
			case Token.NUM:
				parseNum();
				return new LiteralExpr(new IntLiteral(currentToken), currentToken.position);
			case Token.OPERATOR:
				if (Token.isBinop(currentToken.spelling)) {
					return parseExpression();
				}
				else {
					return new UnaryExpr(parseUnop(), parseExpandedExpression(), currentToken.position);
				}
				
			case Token.NEW:
				acceptIt();
				Identifier id = parseIdentifier();
				if(currentToken.kind == Token.LBRACKET) {
					accept(Token.LBRACKET, "parseExpandedExpression");
					if (currentToken.kind == Token.RBRACKET) {
						accept(Token.RBRACKET, "parseExpandedExpression");
						switch(id.spelling) {
							case "int":
								return new NewArrayExpr(new BaseType(TypeKind.INT, currentToken.position), null, currentToken.position);
							case "boolean":
								return new NewArrayExpr(new BaseType(TypeKind.BOOLEAN, currentToken.position), null, currentToken.position);
							case "<id>":
								return new NewArrayExpr(new ClassType(id, currentToken.position), null, currentToken.position);
						}						
					}
					else {
						Expression expr = parseExpression();
						accept(Token.RBRACKET, "parseExpandedExpression");
						switch(id.spelling) {
							case "int":
								return new NewArrayExpr(new BaseType(TypeKind.INT, currentToken.position), expr, currentToken.position);
							case "boolean":
								return new NewArrayExpr(new BaseType(TypeKind.BOOLEAN, currentToken.position), expr, currentToken.position);
							case "<id>":
								return new NewArrayExpr(new ClassType(id, currentToken.position), expr, currentToken.position);
						}
					}										
				}
				else if (currentToken.kind == Token.LPAREN) {
					accept(Token.LPAREN, "parseExpandedExpression");
					if (currentToken.kind == Token.RPAREN) {
						accept(Token.RPAREN, "parseExpandedExpression");
						return new NewObjectExpr(new ClassType(id, currentToken.position), currentToken.position);
					}
					else {
						parseArgumentList();
						accept(Token.RPAREN, "parseExpandedExpression");
						return new NewObjectExpr(new ClassType(id, currentToken.position), currentToken.position);
					}					
				}
				// while (currentToken.kind == Token.PERIOD) {
				// 	acceptIt();
				// 	parseExpression();
				// }
				// break;
			case Token.LPAREN:
				acceptIt();
				Expression expr = parseExpression();
				accept(Token.RPAREN, "parseExpandedExpression");
				return expr;
			default:
				Reference r = parseReference();
				if(currentToken.kind == Token.LPAREN) {
					acceptIt();
					ExprList argList = null;
					if(currentToken.kind != Token.RPAREN) {
						argList = parseArgumentList();
					}
					accept(Token.RPAREN, "parseExpandedExpression");
					return new CallExpr(r, argList, currentToken.position);
				}
				else if(currentToken.kind == Token.LBRACKET){
					acceptIt();
					Expression exprLocal = parseExpression();
					accept(Token.RBRACKET, "parseExpandedExpression");
					return new IxExpr(r, exprLocal, currentToken.position);
				}
				else if (currentToken.kind == Token.PERIOD){	
					Identifier idRef = null;
					while(currentToken.kind == Token.PERIOD)  {
						acceptIt();
						idRef = parseIdentifier();
					}	
					return new RefExpr(r, currentToken.position);				 					
				}
				break;
		}
		return null;
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
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			return new Operator(currentToken);
		} 
		else {
			syntacticError("Unexpected Token in parseBinop", currentToken.spelling);
			return null;
		}
	}
	
	public Operator parseUnop() throws SyntaxError {
		if(debug==true) System.out.println("Running parseUnop");
		if (currentToken.kind == Token.OPERATOR) {
			previousTokenPosition = currentToken.position;
			if(debug==true) System.out.println(currentToken.spelling);
			currentToken = Scanner.scan();
			return new Operator(currentToken);
		} 
		else {
			syntacticError("Unexpected Token in parseUnop", currentToken.spelling);
			return null;
		}
	}
}
