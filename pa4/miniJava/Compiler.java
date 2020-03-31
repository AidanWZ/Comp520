package miniJava;

import miniJava.AbstractSyntaxTrees.AST;
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
    private static String idErrors;
    
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
            System.exit(0);
        }
    }

	public static void main(String[] args){
		if (args.length > 2 || args.length == 0) {
			System.out.println("Wrong number of arguments");
			System.exit(1);
        }
        getDebugLevel(args);        
		try {
            display("getting sourcefile...");
            source = new SourceFile(args[0]);
            //source = new SourceFile(System.getProperty("user.dir") + args[0]);
            //source.getContents();
            display("Initializing scanner...");
            scanner  = new Scanner(source);
            display("Initializing syntax error reporter...");
            parseReporter = new ErrorReporter();
            display("Initializing id error reporter...");
            idReporter = new ErrorReporter();
            display("Initializing type error reporter...");
            typeReporter = new ErrorReporter();
            display("Initializing parser...");
            parser   = new Parser(scanner, parseReporter, debug);
            display("Initializing ast display...");
            display  = new ASTDisplay();            

            display("Starting syntactic analysis...");
            ast = parser.parse();
            display("Initializing ast identify...");
            identifier = new ASTIdentify(idReporter, typeReporter, ast);
            displayAST();
            display("Starting Identification/Type Checking...");
            idErrors = identifier.visit(ast);
            display("Syntactic analysis complete");

            display("Checking for errors");
            checkErrors();
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
        // catch (NullPointerException e) {
        //     System.out.println(e);
        //     System.exit(4);
        // }
        // catch (Exception e) {
        //     System.out.println("File " + System.getProperty("user.dir") + args[0] + " not found");
        //     System.out.println(e);
        //     System.exit(4);
        // }
	}
}