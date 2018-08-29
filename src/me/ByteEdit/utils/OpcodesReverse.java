package me.ByteEdit.utils;

public class OpcodesReverse {
	
	public static String reverseOpcode(int opcode) {
		switch (opcode) {
			case 0:
				return "nop";
			case 1:
				return "aconst_null";
			case 2:
				return "iconst_m1";
			case 3:
				return "iconst_0";
			case 4:
				return "iconst_1";
			case 5:
				return "iconst_2";
			case 6:
				return "iconst_3";
			case 7:
				return "iconst_4";
			case 8:
				return "iconst_5";
			case 9:
				return "lconst_0";
			case 10:
				return "lconst_1";
			case 11:
				return "fconst_0";
			case 12:
				return "fconst_1";
			case 13:
				return "fconst_2";
			case 14:
				return "dconst_0";
			case 15:
				return "dconst_1";
			case 16:
				return "bipush";
			case 17:
				return "sipush";
			case 18:
				return "ldc";
			case 21:
				return "iload";
			case 22:
				return "lload";
			case 23:
				return "fload";
			case 24:
				return "dload";
			case 25:
				return "aload";
			case 46:
				return "iaload";
			case 47:
				return "laload";
			case 48:
				return "faload";
			case 49:
				return "daload";
			case 50:
				return "aaload";
			case 51:
				return "baload";
			case 52:
				return "caload";
			case 53:
				return "saload";
			case 54:
				return "istore";
			case 55:
				return "lstore";
			case 56:
				return "fstore";
			case 57:
				return "dstore";
			case 58:
				return "astore";
			case 79:
				return "iastore";
			case 80:
				return "lastore";
			case 81:
				return "fastore";
			case 82:
				return "dastore";
			case 83:
				return "aastore";
			case 84:
				return "bastore";
			case 85:
				return "castore";
			case 86:
				return "sastore";
			case 87:
				return "pop";
			case 88:
				return "pop2";
			case 89:
				return "dup";
			case 90:
				return "dup_x1";
			case 91:
				return "dup_x2";
			case 92:
				return "dup2";
			case 93:
				return "dup2_x1";
			case 94:
				return "dup2_x2";
			case 95:
				return "swap";
			case 96:
				return "iadd";
			case 97:
				return "ladd";
			case 98:
				return "fadd";
			case 99:
				return "dadd";
			case 100:
				return "isub";
			case 101:
				return "lsub";
			case 102:
				return "fsub";
			case 103:
				return "dsub";
			case 104:
				return "imul";
			case 105:
				return "lmul";
			case 106:
				return "fmul";
			case 107:
				return "dmul";
			case 108:
				return "idiv";
			case 109:
				return "ldiv";
			case 110:
				return "fdiv";
			case 111:
				return "ddiv";
			case 112:
				return "irem";
			case 113:
				return "lrem";
			case 114:
				return "frem";
			case 115:
				return "drem";
			case 116:
				return "ineg";
			case 117:
				return "lneg";
			case 118:
				return "fneg";
			case 119:
				return "dneg";
			case 120:
				return "ishl";
			case 121:
				return "lshl";
			case 122:
				return "ishr";
			case 123:
				return "lshr";
			case 124:
				return "iushr";
			case 125:
				return "lushr";
			case 126:
				return "iand";
			case 127:
				return "land";
			case 128:
				return "ior";
			case 129:
				return "lor";
			case 130:
				return "ixor";
			case 131:
				return "lxor";
			case 132:
				return "iinc";
			case 133:
				return "i2l";
			case 134:
				return "i2f";
			case 135:
				return "i2d";
			case 136:
				return "l2i";
			case 137:
				return "l2f";
			case 138:
				return "l2d";
			case 139:
				return "f2i";
			case 140:
				return "f2l";
			case 141:
				return "f2d";
			case 142:
				return "d2i";
			case 143:
				return "d2l";
			case 144:
				return "d2f";
			case 145:
				return "i2b";
			case 146:
				return "i2c";
			case 147:
				return "i2s";
			case 148:
				return "lcmp";
			case 149:
				return "fcmpl";
			case 150:
				return "fcmpg";
			case 151:
				return "dcmpl";
			case 152:
				return "dcmpg";
			case 153:
				return "ifeq";
			case 154:
				return "ifne";
			case 155:
				return "iflt";
			case 156:
				return "ifge";
			case 157:
				return "ifgt";
			case 158:
				return "ifle";
			case 159:
				return "if_icmpeq";
			case 160:
				return "if_icmpne";
			case 161:
				return "if_icmplt";
			case 162:
				return "if_icmpge";
			case 163:
				return "if_icmpgt";
			case 164:
				return "if_icmple";
			case 165:
				return "if_acmpeq";
			case 166:
				return "if_acmpne";
			case 167:
				return "goto";
			case 168:
				return "jsr";
			case 169:
				return "ret";
			case 170:
				return "tableswitch";
			case 171:
				return "lookupswitch";
			case 172:
				return "ireturn";
			case 173:
				return "lreturn";
			case 174:
				return "freturn";
			case 175:
				return "dreturn";
			case 176:
				return "areturn";
			case 177:
				return "return";
			case 178:
				return "getstatic";
			case 179:
				return "putstatic";
			case 180:
				return "getfield";
			case 181:
				return "putfield";
			case 182:
				return "invokevirtual";
			case 183:
				return "invokespecial";
			case 184:
				return "invokestatic";
			case 185:
				return "invokeinterface";
			case 186:
				return "invokedynamic";
			case 187:
				return "new";
			case 188:
				return "newarray";
			case 189:
				return "anewarray";
			case 190:
				return "arraylength";
			case 191:
				return "athrow";
			case 192:
				return "checkcast";
			case 193:
				return "instanceof";
			case 194:
				return "monitorenter";
			case 195:
				return "monitorexit";
			case 197:
				return "multianewarray";
			case 198:
				return "ifnull";
			case 199:
				return "ifnonnull";
			default:
				return "Unknown Opcode: " + opcode;
		}
	}
	
	public static String generateCompletionDesc(String opcode) {
		return "<html><b><u>" + opcode + "</u></b><br>" + generateDesc(opcode) + "<br><br><b>Arguments:</b><br><code>"
				+ generateByteDesc(opcode) + "</code><br><br><b>Stack:</b><br><code>" + generateStackDesc(opcode) + "</code></html>";
	}
	
	private static String generateDesc(String opcode) {
		switch (opcode) {
			case "dcmpl":
			case "dcmpg":
				return "compare two doubles";
			case "fcmpl":
			case "fcmpg":
				return "compare two floats";
			case "lookupswitch":
				return "a target address is looked up from a table using a key and execution continues from the instruction at that address";
			case "isub":
				return "int subtract";
			case "if_icmpeq":
				return "if ints are equal, go to instruction at <code>label id</code>";
			case "aaload":
				return "load onto the stack a reference from an array";
			case "ior":
				return "bitwise int OR";
			case "laload":
				return "load a long from an array";
			case "iconst_m1":
				return "load the int value -1 onto the stack";
			case "if_icmpne":
				return "if ints are not equal, go to instruction at <code>label id</code>";
			case "invokevirtual":
				return "invoke virtual method on object <code>objectref</code> and puts the result on the stack (might be void), identified by <code>desc</code> and <code>name</code>";
			case "freturn":
				return "return a float";
			case "lstore":
				return "store a long <code>value</code> in a local variable <code>index</code>";
			case "multianewarray":
				return "create a new array of <code>dimension</code> dimensions of type identified by class reference in constant pool <code>desc</code>";
			case "fsub":
				return "subtract two floats";
			case "monitorenter":
				return "enter monitor for object (grab the lock - start of synchronized() section)";
			case "sastore":
				return "store short to array";
			case "astore":
				return "store a reference into a local variable <code>index</code>";
			case "bastore":
				return "store a byte or Boolean value into an array";
			case "if_icmpge":
				return "if <code>value1</code> is greater than or equal to <code>value2</code>, go to instruction at <code>label id</code>";
			case "iload":
				return "load an int <code>value</code> from a local variable <code>index</code>";
			case "pop2":
				return "discard the top two values on the stack (or one value, if it is a double or long)";
			case "putfield":
				return "set field to <code>value</code> in an object <code>objectref</code>, where the field is identified by <code>desc</code>";
			case "anewarray":
				return "create a new array of references of length <code>count</code> and component type the constant pool";
			case "athrow":
				return "throws an error or exception (notice that the rest of the stack is cleared, leaving only a reference to the Throwable)";
			case "dup2":
				return "duplicate top two stack words (two values, if <code>value1</code> is not double nor long; a single value, if <code>value1</code> is double or long)";
			case "caload":
				return "load a char from an array";
			case "fconst_0":
				return "push <code>0.0f</code> on the stack";
			case "if_icmpgt":
				return "if <code>value1</code> is greater than <code>value2</code>, go to instruction at <code>label id</code>";
			case "fconst_1":
				return "push <code>1.0f</code> on the stack";
			case "fdiv":
				return "divide two floats";
			case "frem":
				return "get the remainder from a division between two floats";
			case "iaload":
				return "load an int from an array";
			case "jsr":
				return "jump to subroutine at <code>label id</code> and place the return address on the stack";
			case "sipush":
				return "push a short onto the stack as an integer <code>value</code>";
			case "getfield":
				return "get a field <code>value</code> of an object <code>objectref</code>, where the field is identified by <code>desc</code>";
			case "aconst_null":
				return "push a <code>null</code> reference onto the stack";
			case "aload":
				return "load a reference onto the stack from a local variable <code>index</code>";
			case "invokespecial":
				return "invoke instance method on object <code>objectref</code> and puts the result on the stack (might be void), identified by <code>desc</code> and <code>name</code>";
			case "lor":
				return "bitwise OR of two longs";
			case "pop":
				return "discard the top value on the stack";
			case "goto":
				return "goes to another instruction at <code>label id</code>";
			case "fadd":
				return "add two floats";
			case "iflt":
				return "if <code>value</code> is less than 0, go to instruction at <code>label id</code>";
			case "monitorexit":
				return "exit monitor for object (release the lock - end of synchronized() section)";
			case "f2d":
				return "convert a float to a double";
			case "castore":
				return "store a char into an array";
			case "fneg":
				return "negate a float";
			case "areturn":
				return "return a reference from a method";
			case "f2i":
				return "convert a float to an int";
			case "lxor":
				return "bitwise XOR of two longs";
			case "fconst_2":
				return "push <code>2.0f</code> on the stack";
			case "baload":
				return "load a byte or Boolean value from an array";
			case "f2l":
				return "convert a float to a long";
			case "ixor":
				return "int xor";
			case "ireturn":
				return "return an integer from a method";
			case "lsub":
				return "subtract two longs";
			case "ifle":
				return "if <code>value</code> is less than or equal to 0, go to instruction at <code>label id</code>";
			case "lcmp":
				return "push 0 if the two longs are the same, 1 if <code>value1</code> is greater than value2, -1 otherwise";
			case "dsub":
				return "subtract a double from another";
			case "ifnonnull":
				return "if <code>value</code> is not null, go to instruction at <code>label id</code>";
			case "if_acmpeq":
				return "if references are equal, go to instruction at <code>label id</code>";
			case "lload":
				return "load a long value from a local variable <code>index</code>";
			case "arraylength":
				return "get the length of an array";
			case "if_acmpne":
				return "if references are not equal, go to instruction at <code>label id</code>";
			case "return":
				return "return void from method";
			case "ddiv":
				return "divide two doubles";
			case "dneg":
				return "negate a double";
			case "dastore":
				return "store a double into an array";
			case "ishr":
				return "int arithmetic shift right";
			case "ladd":
				return "add two longs";
			case "ifge":
				return "if <code>value</code> is greater than or equal to 0, go to instruction at <code>label id</code>";
			case "imul":
				return "multiply two integers";
			case "newarray":
				return "create new array with <code>count</code> elements of primitive type identified by <code>type</code>";
			case "aastore":
				return "store into a reference in an array";
			case "invokestatic":
				return "invoke a static method and puts the result on the stack (might be void), identified by <code>desc</code> and <code>name</code>";
			case "checkcast":
				return "checks whether an <code>objectref</code> is of a certain type, the class reference identified by <code>desc</code>";
			case "dadd":
				return "add two doubles";
			case "lneg":
				return "negate a long";
			case "ldiv":
				return "divide two longs";
			case "i2b":
				return "convert an int into a byte";
			case "drem":
				return "get the remainder from a division between two doubles";
			case "i2d":
				return "convert an int into a double";
			case "i2c":
				return "convert an int into a character";
			case "fload":
				return "load a float <code>value</code> from a local variable <code>index</code>";
			case "i2f":
				return "convert an int into a float";
			case "lrem":
				return "remainder of division of two longs";
			case "ishl":
				return "int shift left";
			case "irem":
				return "logical int remainder";
			case "ret":
				return "continue execution from address taken from a local variable <code>index</code> (the asymmetry with jsr is intentional)";
			case "new":
				return "create new object of type identified by <code>desc</code>";
			case "fstore":
				return "store a float <code>value</code> into a local variable <code>index</code>";
			case "i2l":
				return "convert an int into a long";
			case "ifne":
				return "if <code>value</code> is not 0, go to instruction at <code>label id</code>";
			case "swap":
				return "swaps two top words on the stack (note that <code>value1</code> and <code>value2</code> must not be double or long)";
			case "dreturn":
				return "return a double from a method";
			case "iadd":
				return "add two ints";
			case "i2s":
				return "convert an int into a short";
			case "invokedynamic":
				return "invokes a dynamic method and puts the result on the stack (might be void), identified by <code>desc</code> and <code>name</code>";
			case "fmul":
				return "multiply two floats";
			case "istore":
				return "store int <code>value</code> into variable <code>index</code>";
			case "dup_x1":
				return "insert a copy of the top value into the stack two values from the top. <code>value1</code> and <code>value2</code> must not be of the type double or long.";
			case "dup_x2":
				return "insert a copy of the top value into the stack two (if <code>value2</code> is double or long it takes up the entry of <code>value3</code>, too) or three values (if <code>value2</code> is neither double nor long) from the top";
			case "faload":
				return "load a float from an array";
			case "iastore":
				return "store an int into an array";
			case "ineg":
				return "negate int";
			case "ifeq":
				return "if <code>value</code> is 0, go to instruction at <code>label id</code>";
			case "idiv":
				return "divide two integers";
			case "lushr":
				return "bitwise shift right of a long <code>value1</code> by int <code>value2</code> positions, unsigned";
			case "dup2_x1":
				return "duplicate two words and insert beneath third word (see explanation above)";
			case "dup2_x2":
				return "duplicate two words and insert beneath fourth word";
			case "lastore":
				return "store a long to an array";
			case "iconst_5":
				return "load the int value 5 onto the stack";
			case "getstatic":
				return "get a static field <code>value</code> of a class, where the field is identified by <code>desc</code>";
			case "putstatic":
				return "set static field to <code>value</code> in a class, where the field is identified by <code>desc</code>";
			case "fastore":
				return "store a float in an array";
			case "daload":
				return "load a double from an array";
			case "nop":
				return "perform no operation";
			case "dmul":
				return "multiply two doubles";
			case "dstore":
				return "store a double <code>value</code> into a local variable <code>index</code>";
			case "if_icmple":
				return "if <code>value1</code> is less than or equal to <code>value2</code>, go to instruction at <code>label id</code>";
			case "bipush":
				return "push a <code>byte</code> onto the stack as an integer <code>value</code>";
			case "d2f":
				return "convert a double to a float";
			case "iconst_0":
				return "load the int value 0 onto the stack";
			case "ifnull":
				return "if <code>value</code> is null, go to instruction at <code>label id</code>";
			case "land":
				return "bitwise AND of two longs";
			case "d2i":
				return "convert a double to an int";
			case "dconst_0":
				return "push the constant <code>0.0</code> (a <code>double</code>) onto the stack";
			case "iconst_3":
				return "load the int value 3 onto the stack";
			case "lconst_1":
				return "push <code>1L</code> (the number one with type <code>long</code>) onto the stack";
			case "dconst_1":
				return "push the constant <code>1.0</code> (a <code>double</code>) onto the stack";
			case "iconst_4":
				return "load the int value 4 onto the stack";
			case "lconst_0":
				return "push <code>0L</code> (the number zero with type <code>long</code>) onto the stack";
			case "iconst_1":
				return "load the int value 1 onto the stack";
			case "d2l":
				return "convert a double to a long";
			case "iconst_2":
				return "load the int value 2 onto the stack";
			case "l2d":
				return "convert a long to a double";
			case "l2f":
				return "convert a long to a float";
			case "l2i":
				return "convert a long to a int";
			case "lshr":
				return "bitwise shift right of a long <code>value1</code> by int <code>value2</code> positions";
			case "iinc":
				return "increment local variable <code>index</code> by <code>const</code>";
			case "ldc":
				return "push a constant <code>value</code>";
			case "lmul":
				return "multiply two longs";
			case "if_icmplt":
				return "if <code>value1</code> is less than <code>value2</code>, go to instruction at <code>label id</code>";
			case "instanceof":
				return "determines if an object <code>objectref</code> is of a given type, identified by <code>desc</code>";
			case "iushr":
				return "int logical shift right";
			case "saload":
				return "load short from array";
			case "lreturn":
				return "return a long value";
			case "tableswitch":
				return "a target address is looked up from a table using a key and execution continues from the instruction at that address";
			case "dload":
				return "load a double <code>value</code> from a local variable <code>index</code>";
			case "lshl":
				return "bitwise shift left of a long <code>value1</code> by int <code>value2</code> positions";
			case "ifgt":
				return "if <code>value</code> is greater than 0, go to instruction at <code>label id</code>";
			case "invokeinterface":
				return "invokes an interface method on object <code>objectref</code> and puts the result on the stack (might be void), identified by <code>desc</code> and <code>name</code>";
			case "dup":
				return "duplicate the value on top of the stack";
			case "iand":
				return "perform a bitwise AND on two integers";
			case "public":
				return "access modifier <code>public</code>";
			case "private":
				return "access modifier <code>private</code>";
			case "protected":
				return "access modifier <code>protected</code>";
			case "final":
				return "access modifier <code>final</code>";
			case "class":
				return "keyword <code>class</code>";
			case "enum":
				return "keyword <code>enum</code>";
			case "static":
				return "access modifier <code>static</code>";
			case "strictfp":
				return "keyword <code>strictfp</code>";
			case "throws":
				return "keyword <code>throws</code>";
			case "synthetic":
				return "access modifier <code>synthetic</code>";
			case "bridge":
				return "access modifier <code>bridge</code>";
			case "label":
				return "creates a label with id <code>label id</code>, use with<br><code>// label [label id]</code>";
			case "line":
				return "creates a line number with number <code>line number</code>, use with<br><code>// line [number]</code>";
			case "extends":
				return "keyword <code>extends</code>";
			case "implements":
				return "keyword <code>implements</code>";
			default:
				return "No description available.";
		}
	}
	
	private static String generateByteDesc(String opcode) {
		switch (opcode) {
			case "if_icmpeq":
			case "if_icmpne":
			case "if_icmpge":
			case "if_icmpgt":
			case "jsr":
			case "goto":
			case "iflt":
			case "ifle":
			case "ifnonnull":
			case "if_acmpeq":
			case "if_acmpne":
			case "ifge":
			case "ifne":
			case "ifeq":
			case "if_icmple":
			case "ifnull":
			case "if_icmplt":
			case "ifgt":
			case "label":
				return "label id";
			case "lstore":
			case "astore":
			case "iload":
			case "aload":
			case "lload":
			case "fload":
			case "ret":
			case "fstore":
			case "istore":
			case "dstore":
			case "dload":
				return "index";
			case "invokevirtual":
			case "putfield":
			case "getfield":
			case "invokespecial":
			case "invokestatic":
			case "invokedynamic":
			case "getstatic":
			case "putstatic":
			case "invokeinterface":
				return "desc, name";
			case "anewarray":
			case "checkcast":
			case "new":
				return "classname";
			case "multianewarray":
				return "desc, dimension";
			case "lookupswitch":
				return "lookupswitch node";
			case "sipush":
				return "short";
			case "newarray":
				return "type";
			case "bipush":
				return "byte";
			case "iinc":
				return "index, const";
			case "ldc":
				return "value";
			case "instanceof":
				return "desc";
			case "tableswitch":
				return "tableswitch node";
			case "line":
				return "line number";
			default:
				return "No arguments.";
		}
	}
	
	private static String generateStackDesc(String opcode) {
		switch (opcode) {
			case "isub":
			case "ior":
			case "fsub":
			case "fdiv":
			case "frem":
			case "lor":
			case "fadd":
			case "lxor":
			case "ixor":
			case "lsub":
			case "lcmp":
			case "dsub":
			case "fcmpl":
			case "ddiv":
			case "ishr":
			case "ladd":
			case "imul":
			case "fcmpg":
			case "dadd":
			case "ldiv":
			case "drem":
			case "lrem":
			case "ishl":
			case "irem":
			case "iadd":
			case "fmul":
			case "idiv":
			case "lushr":
			case "dmul":
			case "land":
			case "lshr":
			case "dcmpl":
			case "dcmpg":
			case "lmul":
			case "iushr":
			case "lshl":
			case "iand":
				return "value1, value2 -> result";
			case "sastore":
			case "bastore":
			case "castore":
			case "dastore":
			case "aastore":
			case "iastore":
			case "lastore":
			case "fastore":
				return "arrayref, index, value ->";
			case "monitorenter":
			case "astore":
			case "monitorexit":
				return "objectref ->";
			case "f2d":
			case "fneg":
			case "f2i":
			case "f2l":
			case "dneg":
			case "lneg":
			case "i2b":
			case "i2d":
			case "i2c":
			case "i2f":
			case "i2l":
			case "i2s":
			case "ineg":
			case "d2f":
			case "d2i":
			case "d2l":
			case "l2d":
			case "l2f":
			case "l2i":
				return "value -> result";
			case "iload":
			case "sipush":
			case "lload":
			case "fload":
			case "getstatic":
			case "bipush":
			case "ldc":
			case "dload":
				return "-> value";
			case "lstore":
			case "pop":
			case "iflt":
			case "ifle":
			case "ifnonnull":
			case "ifge":
			case "fstore":
			case "ifne":
			case "istore":
			case "ifeq":
			case "putstatic":
			case "dstore":
			case "ifnull":
			case "ifgt":
				return "value ->";
			case "freturn":
			case "ireturn":
			case "dreturn":
			case "lreturn":
				return "value -> [empty]";
			case "if_icmpeq":
			case "if_icmpne":
			case "if_icmpge":
			case "if_icmpgt":
			case "if_acmpeq":
			case "if_acmpne":
			case "if_icmple":
			case "if_icmplt":
				return "value1, value2 ->";
			case "aaload":
			case "laload":
			case "caload":
			case "iaload":
			case "baload":
			case "faload":
			case "daload":
			case "saload":
				return "arrayref, index -> value";
			case "invokevirtual":
			case "invokespecial":
			case "invokeinterface":
				return "objectref, [arg1, arg2, ...] -> result";
			case "anewarray":
			case "newarray":
				return "count -> arrayref";
			case "aload":
			case "new":
				return "-> objectref";
			case "lookupswitch":
				return "key ->";
			case "iconst_m1":
				return "-> -1";
			case "multianewarray":
				return "count1, [count2,...] -> arrayref";
			case "pop2":
				return "{value2, value1} ->";
			case "putfield":
				return "objectref, value ->";
			case "athrow":
				return "objectref -> [empty], objectref";
			case "dup2":
				return "{value2, value1} -> {value2, value1}, {value2, value1}";
			case "fconst_0":
				return "-> 0.0f";
			case "fconst_1":
				return "-> 1.0f";
			case "jsr":
				return "-> address";
			case "getfield":
				return "objectref -> value";
			case "aconst_null":
				return "-> null";
			case "areturn":
				return "objectref -> [empty]";
			case "fconst_2":
				return "-> 2.0f";
			case "arraylength":
				return "arrayref -> length";
			case "return":
				return "-> [empty]";
			case "invokestatic":
				return "[arg1, arg2, ...] -> result";
			case "checkcast":
				return "objectref -> objectref";
			case "swap":
				return "value2, value1 -> value1, value2";
			case "invokedynamic":
				return "[arg1, [arg2 ...]] -> result";
			case "dup_x1":
				return "value2, value1 -> value1, value2, value1";
			case "dup_x2":
				return "value3, value2, value1 -> value1, value3, value2, value1";
			case "dup2_x1":
				return "value3, {value2, value1} -> {value2, value1}, value3, {value2, value1}";
			case "dup2_x2":
				return "{value4, value3}, {value2, value1} -> {value2, value1}, {value4, value3}, {value2, value1}";
			case "iconst_5":
				return "-> 5";
			case "iconst_0":
				return "-> 0";
			case "dconst_0":
				return "-> 0.0";
			case "iconst_3":
				return "-> 3";
			case "lconst_1":
				return "-> 1L";
			case "dconst_1":
				return "-> 1.0";
			case "iconst_4":
				return "-> 4";
			case "lconst_0":
				return "-> 0L";
			case "iconst_1":
				return "-> 1";
			case "iconst_2":
				return "-> 2";
			case "instanceof":
				return "objectref -> result";
			case "tableswitch":
				return "index ->";
			case "dup":
				return "value -> value, value";
			default:
				return "Stack doesn't change.";
		}
	}
}
