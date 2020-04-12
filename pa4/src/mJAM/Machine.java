package mJAM;

/**
 * Defines names and sizes of mJAM instructions and primitives
 * @author prins
 * @version COMP 520 V2.3
 */
public final class Machine {
	
	/**
	 * mJAM instructions
	 */
	public enum Op {
		/**
		 * Load a value onto the execution stack (Op, length, reg, displacement)
		 */
	    LOAD,
		/**
		 * Load the data address d offset from register r onto the execution stack (Op, length, reg, displacement)
		 */
		LOADA,
		/**
		 * Pop a data address from the stack, fetch an n word object from that address and push it back onto the stack (Op, length, reg, displacement)
		 */
		LOADI,
		/**
		 * Push the 1 word literal value d onto the stack (Op, length, reg, displacement)
		 */
		LOADL,
		/**
		 *Pop an n word object from the stack and store it at address d offset from register r (Op, length, reg, displacement)
		 */
		STORE,
		/**
		 * Pop an address from the stack, then pop an N word object from the stack and store at that address (Op, length, reg, displacement)
		 */
		STOREI,
		/**
		 * direct call of instance method, call routine at code address d offset from register r (Op, length, reg, displacement)
		 */
		CALL,
		/**
		 * indirect call of instance method, pop a code address from the stack and call routine at that address (Op, length, reg, displacement)
		 */
		CALLI,
		/**
		 * dynamic call of instance method (Op, length, reg, displacement)
		 */
		CALLD,
		/**
		 * Rturn from the current routine, pop an n word object from the stack, pop the topmost frame, pop d arguments, push result r onto stack (Op, length, reg, displacement)
		 */
		RETURN,
		/**
		 * push d words onto the stack (Op, length, reg, displacement)
		 */
		PUSH,
		/**
		 * pop an n word result from the stack, pop d more words, push result r onto stack (Op, length, reg, displacement)
		 */
		POP,
		/**
		 * jump to code address d offset from register r (Op, length, reg, displacement)
		 */
		JUMP,
		/**
		 * pop an instruction address from the stack and jump to that instruction (Op, length, reg, displacement)
		 */
		JUMPI,
		/**
		 * pop a one word value from the stack, jump to code address d offset from register r if that value equals n (Op, length, reg, displacement)
		 */
		JUMPIF,
		/**
		 * stop execution (Op, length, reg, displacement)
		 */
		HALT;
	}
	public static Op [] intToOp = Op.values();


	/**
	 * mJAM registers
	 */
	public enum Reg {
		/**
		 * zero, not used
		 */
		ZR, 
		/**
		 * code base, address of first instruction
		 */
		CB, 
		/**
		 * code top, address of last possible instruction
		 */
		CT,
		/**
		 * code pointer, address of current instruction
		 */
		CP,
		/**
		 * primitives base, address of last possible primitive operator
		 */
		PB,
		/**
		 *  primitives top, address of first possible primitive operator
		 */
		PT,
		/**
		 * execution stack base, address of first static field store
		 */
		SB,
		/**
		 *  execution stack top, address of most recent var or parameter decl
		 */
		ST,
		/**
		 * locals base (frame pointer), address of beginning of most recent call  
		 */
		LB,
		/**
		 * heap base, address of first possible object instance
		 */
		HB,
		/**
		 * heap top, address of most recent object instance
		 */
		HT,  
		/**
		 * object base (instance pointer), address of beginning of an instance on the heap
		 */
		OB;
	}
	public static Reg [] intToReg = Reg.values();

	/**
	 * mJAM primitives
	 */
	public enum Prim {
	    id,
	    not,
	    and,
	    or,
	    succ,
	    pred,
	    neg,
	    add,
	    sub,
	    mult,
	    div,
	    mod,
	    lt,
	    le,
	    ge,
	    gt,
	    eq,
	    ne,
	    eol,
	    eof,
	    get,
	    put,
	    geteol,
	    puteol,
	    getint,
	    putint,
	    putintnl,
	    alloc,
	    dispose,
	    newobj,
	    newarr,
	    arraylen,
	    arrayref,
	    arrayupd,
	    fieldref,
	    fieldupd;
	}
	public static Prim [] intToPrim = Prim.values();

	public static int primToInt(Prim p) {
		int counter = 0;
		for (Prim prim: intToPrim) {
			if (prim == p) {
				return counter;
			}
			counter++;
		}
		return -1;
	}

	// range for int constants
  	public final static long
    minintRep = -2147483648,
    maxintRep =  2147483647;

  
	// CODE STORE REGISTERS

	/**
	 * Address of first instruction in code segment
	 */
	public final static int CB = 0; // start of code space
	
	/**
	 * Address of last instruction in primitive segment
	 */
	public final static int PB = 1024; // size of code space reserved for instructions
	
	/**
	 * Address of first instruction in primitive segment
	 */
	public final static int PT = PB + Prim.values().length;  // code space reserved for primitives

	// CODE STORE
	public static Instruction[] code = new Instruction[PB];
	public static int CT = CB;
	
	public static void initCodeGen() {
		CT = CB;
	}
	
	/**
	 * Places an instruction, with the given fields, into the next position in the code store
	 * @param op - operation
	 * @param n - length 
	 * @param r - register
	 * @param d - displacement
	 */
	public static void emit(Op op, int n, Reg r, Prim d) {
		emit(op.ordinal(), n, r.ordinal(), d.ordinal());
	}

	/**
	 * emit operation with single literal argument d (n,r not used).  These are
	 * operations like LOADL 44, PUSH 3, and CALLD 1
	 */
	public static void emit(Op op, int d) {
		emit(op.ordinal(), 0, 0, d);
	}
  
	/**
	 * emit "call primitive operation" (operation built-in to mJAM).  This 
	 * generates  CALL primitiveop[PB]
	 */
	public static void emit(Prim d) {
		emit(Op.CALL.ordinal(), 0, Machine.Reg.PB.ordinal(), d.ordinal());
	}
	
	/**
	 * emit operations without arguments.  These are operations like 
	 * LOADI and STOREI
	 */
	public static void emit(Op op) {
		emit(op, 0, 0, 0);
	}
	
	/**
	 * emit operation with register r and integer displacement.  These are
	 * operations like JUMP 25[CB] and LOAD 6[LB]
	 */
	public static void emit(Op op, Reg r, int d) {
		emit(op.ordinal(), 0, r.ordinal(), d);
	}
	
	/**
	 * emit operation with n field, and register r and integer displacement.  These are
	 * operations like JUMPIF (1) 25[CB].  In the assembly code the value of n is shown 
	 * in parens.
	 */
	public static void emit(Op op, int n, Reg r, int d) {
		emit(op.ordinal(), n, r.ordinal(), d);
	}
	
	/**
	 * emit operation with integer n, r, d.  These are operations
	 * like RETURN (1) 3  and HALT (4) 0.  For RETURN the value
	 * of d is the number of caller args to pop off the callers 
	 * stack and n is the number of values to return at caller stack
	 * top.   n must be 0 or 1.
	 */
	public static void emit(Op op, int n, int r, int d) {
		emit(op.ordinal(), n, r, d);
	}
	
	/**
	 * helper operation for emit using integer values
	 */
	private static void emit (int op, int n, int r, int d) {
		if (n > 255) {
			System.out.println("length of operand can't exceed 255 words");
			n = 255; // to allow code generation to continue
		}
		if (CT >= Machine.PB)
			System.out.println("mJAM: code segment capacity exceeded");
		
		Instruction nextInstr = new Instruction(op, n, r, d);
			Machine.code[CT] = nextInstr;
			CT = CT + 1;
	}

	/**
	 * @return address (relative to CB) of next instruction to be generated
	 */
	public static int nextInstrAddr() {
		return CT;
	}

	/**
	 * Update the displacement component of the (JUMP or CALL) instruction at addr
	 * @param addr
	 * @param displacement
	 */
	public static void patch(int addr, int displacement) {
		if (addr < 0 || addr >= CT) {
			System.out.println("patch:  address of instruction to be patched is out of range");
			return;
		}
		if (displacement < 0 || displacement > CT) {
			System.out.println("patch:  target address of patch is out of range");
			return;
		}
		Machine.code[addr].d = displacement;
		return;
	}

	// DATA REPRESENTATION

	public final static int
		booleanSize = 1,
		characterSize = 1,
		integerSize = 1,
		addressSize = 1,
		linkDataSize = 3 * addressSize,   // caller's LB, OB, RA
		falseRep = 0,
		trueRep = 1,
		nullRep = 0;

}
