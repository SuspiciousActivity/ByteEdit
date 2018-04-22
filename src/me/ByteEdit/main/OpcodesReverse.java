package me.ByteEdit.main;

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

}