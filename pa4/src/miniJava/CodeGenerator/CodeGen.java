package miniJava.CodeGenerator;

import mJAM.Machine;
import mJAM.Machine.*;
import miniJava.AbstractSyntaxTrees.AST;
import mJAM.ObjectFile;
import mJAM.Disassembler;
import mJAM.Interpreter;

public class CodeGen {

    public static void generate(String inputFileName, AST ast) {
        String objectCodeFileName = inputFileName.replace(".java", ".mJAM");
        Machine.initCodeGen();
		
	    //generate call to main
		Machine.emit(Op.LOADL,0);            			// array length 0
		Machine.emit(Prim.newarr);           			// empty String array argument
		int mainCallAddr = Machine.nextInstrAddr(); 	// record instr addr where main is called                                                // "main" is called
		Machine.emit(Op.CALL,Reg.CB,-1);     			// static call main (address to be patched)
		Machine.emit(Op.HALT,0,0,0);         			// end execution
		                           			
	    //write code to object code file (.mJAM)
		ObjectFile objF = new ObjectFile(objectCodeFileName);
		System.out.print("Writing object code file " + objectCodeFileName + " ... ");
		if (objF.write()) {
			System.out.println("FAILED!");
			return;
		}
		else {
            System.out.println("SUCCEEDED");
        }
						
        // create asm file using disassembler (.asm)
        System.out.print("Writing assembly file " + objectCodeFileName + " ... ");
        Disassembler d = new Disassembler(objectCodeFileName);
        if (d.disassemble()) {
            System.out.println("FAILED!");
            return;
        }
        else {
            System.out.println("SUCCEEDED");
        }                
        
        //run code using debugger
        String asmCodeFileName = objectCodeFileName.replace(".mJAM",".asm");
        System.out.println("Running code in debugger ... ");
        Interpreter.debug(objectCodeFileName, asmCodeFileName);
        System.out.println("*** mJAM execution completed");
	}
}
