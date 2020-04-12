package miniJava;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.CodeGenerator.ASTCodeGen;
import miniJava.ContextualAnalyzer.ASTDisplay;
import miniJava.ContextualAnalyzer.ASTIdentify;
import miniJava.ContextualAnalyzer.IdentificationError;
import miniJava.ContextualAnalyzer.TypeError;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.SourceFile;
import miniJava.SyntacticAnalyzer.SyntaxError;

public class Compiler {

	static String objectName = "obj.mjava";
    private static boolean debug;
    private static SourceFile source;
	private static Scanner scanner;
	private static Parser parser;
    private static ErrorReporter parseReporter;
    private static ErrorReporter idReporter;
    private static ErrorReporter typeReporter;
    private static ASTDisplay display;
    private static AST ast;
    private static ASTIdentify identifier;
    private static ASTCodeGen codeGenerator;
    
    public static void getDebugLevel(String[] args) {
        try {
            if (args.length == 2) {
                debug = true;
                display("Debug set to true");
            }
            else {
                debug = false;
            }
        }
        catch (Exception o) {            
            debug = false;
            display("Debug set to false");
        }
    }

    public static void display(String text) {
        if (debug) {
            System.out.println(text);
        }
    }

    public static void displayAST() {
        if (debug) {
            display("Resulting AST:");
            display.showTree(ast);
        }
    }

    public static void checkErrors() {
        if (parseReporter.numErrors > 0) {
            display("Parse errors occcurred");
            System.exit(4);         
        }
        else if (parseReporter.numErrors == 0 && identifier.idReporter.numErrors > 0) {
            display("Identification errors occcurred");
            System.exit(4);   
        }
        else if (parseReporter.numErrors == 0 && identifier.idReporter.numErrors == 0 && identifier.typeReporter.numErrors > 0) {
            display("Type checking errors occcurred");
            System.exit(4);   
        }
        else {
            System.out.println("Valid Program");
            return;
        }
    }

	public static void main(String[] args){
		if (args.length > 2 || args.length == 0) {
			System.out.println("Wrong number of arguments");
			System.exit(1);
        }
        getDebugLevel(args);        
		try {
            source = new SourceFile(args[0]);
            //source = new SourceFile(System.getProperty("user.dir") + args[0]);
            scanner  = new Scanner(source);
            parseReporter = new ErrorReporter();
            idReporter = new ErrorReporter();
            typeReporter = new ErrorReporter();
            parser   = new Parser(scanner, parseReporter, debug);
            display("Syntactic analysis complete...");
            // display("Initializing ast display...");
            // display  = new ASTDisplay();                        
            display("Starting Syntactic analysis...");
            ast = parser.parse();
            display = new ASTDisplay();
            display("Syntactic analysis Complete...");
            display("Starting Contextual analysis...");
            identifier = new ASTIdentify(idReporter, typeReporter, ast);
            displayAST();
            identifier.visit(ast);
            display("Contextual analysis complete");
            display("Checking for Compilation errors");            
            checkErrors();
            display("No compilation errors found, compiling to " + args[0].replace(".java", ".mJAM"));
            codeGenerator = new ASTCodeGen(args[0], ast);
            codeGenerator.generate();
            display("Compilation complete");
            System.exit(0);
        } 
        catch (SyntaxError e) {
            System.out.println("Syntax error occurred");
			System.exit(4);
        }
        catch (IdentificationError e) {
            System.out.println("Identification error occurred");
			System.exit(4);
        }
        catch (TypeError e) {
            System.out.println("Type error occurred");
			System.exit(4);
        }
	}
}