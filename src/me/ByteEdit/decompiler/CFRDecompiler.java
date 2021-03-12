package me.ByteEdit.decompiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.benf.cfr.reader.PluginRunner;
import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.util.MiscConstants.Version;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.objectweb.asm.tree.ClassNode;

public class CFRDecompiler implements IDecompiler {

	public static final HashMap<String, String> options = new HashMap<>();

	static {
		options.put("aexagg", "false");
		options.put("allowcorrecting", "true");
		options.put("arrayiter", "true");
		options.put("caseinsensitivefs", "false");
		options.put("clobber", "false");
		options.put("collectioniter", "true");
		options.put("commentmonitors", "false");
		options.put("decodeenumswitch", "true");
		options.put("decodefinally", "true");
		options.put("decodelambdas", "true");
		options.put("decodestringswitch", "true");
		options.put("dumpclasspath", "false");
		options.put("eclipse", "true");
		options.put("elidescala", "false");
		options.put("forcecondpropagate", "false");
		options.put("forceexceptionprune", "false");
		options.put("forcereturningifs", "false");
		options.put("forcetopsort", "false");
		options.put("forcetopsortaggress", "false");
		options.put("forloopaggcapture", "false");
		options.put("hidebridgemethods", "false");
		options.put("hidelangimports", "true");
		options.put("hidelongstrings", "false");
		options.put("hideutf", "true");
		options.put("innerclasses", "true");
		options.put("j14classobj", "false");
		options.put("labelledblocks", "true");
		options.put("lenient", "false");
		options.put("liftconstructorinit", "true");
		options.put("override", "true");
		options.put("pullcodecase", "false");
		options.put("recover", "true");
		options.put("recovertypeclash", "false");
		options.put("recovertypehints", "false");
		options.put("relinkconststring", "true");
		options.put("removebadgenerics", "true");
		options.put("removeboilerplate", "true");
		options.put("removedeadmethods", "true");
		options.put("removeinnerclasssynthetics", "true");
		options.put("rename", "false");
		options.put("renamedupmembers", "false");
		options.put("renameenumidents", "false");
		options.put("renameillegalidents", "false");
		options.put("showinferrable", "false");
		options.put("silent", "false");
		options.put("stringbuffer", "false");
		options.put("stringbuilder", "true");
		options.put("sugarasserts", "true");
		options.put("sugarboxing", "true");
		options.put("sugarenums", "true");
		options.put("tidymonitors", "true");
		options.put("usenametable", "true");

		try {
			Field f = Version.class.getDeclaredField("version");
			f.setAccessible(true);
			f.set(null, "ByteEdit");
		} catch (Exception ex) {
		}
	}

	@Override
	public String decompile(ClassNode cn, Map<String, ClassNode> classNodes) {
		return doDecompilation(cn, IDecompiler.getBytes(cn), classNodes);
	}

	private static String doDecompilation(ClassNode cn, byte[] b, Map<String, ClassNode> classNodes) {
		try {
			HashMap<String, String> ops = options;
			ClassFileSource cfs = new ClassFileSourceImpl(new OptionsImpl(Collections.EMPTY_MAP)) {

				@Override
				public void informAnalysisRelativePathDetail(String a, String b) {
				}

				@Override
				public String getPossiblyRenamedPath(String path) {
					return path;
				}

				@Override
				public Pair<byte[], String> getClassFileContent(String path) throws IOException {
					String name = path.substring(0, path.length() - 6);
					if (name.equals(cn.name))
						return Pair.make(b, name);
					// try loading other classes from jar
					ClassNode node;
					synchronized (classNodes) {
						node = classNodes.get(name + ".class");
					}
					if (node != null)
						return Pair.make(IDecompiler.getBytes(node), name);
					return super.getClassFileContent(path);
				}

				@Override
				public Collection<String> addJar(String arg0) {
					throw new RuntimeException();
				}
			};
			String decompilation = new PluginRunner(ops, cfs).getDecompilationFor(cn.name);
			System.gc(); // cfr has a performance bug
			return decompilation;
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		}
	}
}
