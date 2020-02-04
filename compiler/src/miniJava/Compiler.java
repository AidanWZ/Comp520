package miniJava;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.SourceFile;
import miniJava.SyntacticAnalyzer.SyntaxError;
import miniJava.ErrorReporter;

public class Compiler {

	static String objectName = "obj.mjava";

	private static Scanner scanner;
	private static Parser parser;
    private static ErrorReporter reporter;
    
	public static void main(String[] args){
		if (args.length != 1) {
			System.out.println("Wrong number of arguments");
			System.exit(1);
		}
        String sourceName = args[0];
        SourceFile source = new SourceFile(sourceName);
		try {
			if (source == null) {
                System.out.println("File not found: " + sourceName);
                System.exit(1);
            }
            scanner  = new Scanner(source);
            reporter = new ErrorReporter();
            parser   = new Parser(scanner, reporter);
            System.out.println("Starting syntactic analysis...");
            parser.parseProgram();
            System.out.println("Syntactic analysis complete");

            if (reporter.numErrors > 0) {
                System.out.println("Invalid program");
                System.exit(4);                
            }
            else {
                System.out.println("Valid Program");
                System.exit(0);
            }
        } 
        catch (SyntaxError e) {
			System.exit(4);
		}
	}
}