package miniJava;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.SourceFile;
import miniJava.SyntacticAnalyzer.SyntaxError;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ASTDisplay;
import miniJava.AbstractSyntaxTrees.ASTIdentify;

public class Compiler {

	static String objectName = "obj.mjava";

	private static Scanner scanner;
	private static Parser parser;
    private static ErrorReporter reporter;
    private static ASTDisplay display;
    private static AST result;
    private static ASTIdentify identifier;
    
    public static boolean getDebugLevel(String[] args) {
        try {
            if (args.length == 2) {
                System.out.println("Debug set to true");
                return true;
            }
            else if (args.length == 2) {
                System.out.println("Debug set to true");
                return false;
            }
        }
        catch (Exception o) {
            System.out.println("Debug set to false");
            return false;
        }
        return false;
    }

	public static void main(String[] args){
		if (args.length > 2 || args.length == 0) {
			System.out.println("Wrong number of arguments");
			System.exit(1);
		}
        String sourceName = args[0];
        SourceFile source = new SourceFile(sourceName);
        boolean debug = getDebugLevel(args);
        
		try {
            scanner  = new Scanner(source);
            reporter = new ErrorReporter();
            parser   = new Parser(scanner, reporter, debug);
            display  = new ASTDisplay();
            identifier = new ASTIdentify();
            
            System.out.println("Starting syntactic analysis...");
            result = parser.parse();
            System.out.println("Starting Identification...");
            String idErrors = identifier.visit(result);
            System.out.println("Syntactic analysis complete");

            if (reporter.numErrors > 0 || idErrors != null) {
                System.out.println(idErrors);
                System.exit(4);                
            }
            else {
                System.out.println("Valid Program");
                if(debug) display.showTree(result);
                System.exit(0);
            }
        } 
        catch (SyntaxError e) {
            System.out.println("Syntax error occurred");
			System.exit(4);
        }
        catch (Exception e) {
            System.out.println("File " + args[0] + " not found");
            System.exit(4);
        }
	}
}