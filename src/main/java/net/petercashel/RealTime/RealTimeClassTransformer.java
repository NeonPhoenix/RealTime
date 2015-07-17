package net.petercashel.RealTime;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.relauncher.FMLRelaunchLog;


public class RealTimeClassTransformer implements net.minecraft.launchwrapper.IClassTransformer {
	
	private static boolean debug = true;

	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {

		if (arg0.equals("aqo")) {
			if (debug) System.out.println("*********RealTime INSIDE OBFUSCATED WORLDPROVIDER TRANSFORMER ABOUT TO PATCH: " + arg0);
			return patchClassASMWorldProvider(arg0, arg2, true);
		}

		if (arg0.equals("net.minecraft.world.WorldProvider")) {
			if (debug) System.out.println("*********RealTime INSIDE WORLDPROVIDER TRANSFORMER ABOUT TO PATCH: " + arg0);
			return patchClassASMWorldProvider(arg0, arg2, false);
		}

		return arg2;
	}

	//patchClassASM for WorldProvider. Specifically named due to upcoming expansion to properly support weather.
	public byte[] patchClassASMWorldProvider(String name, byte[] bytes, boolean obfuscated) {

		String targetMethodName = "";
		String targetMethodName2 = "";

		if(obfuscated == true) {
			// These are obfuscated method names. These are pulled from modding resources inside the MCP.
			// It's quite normal for obfuscated method names to have the same name so long as the parameters
			// are different.
			
			//calculateCelestialAngle
			targetMethodName ="a";
			//getMoonPhase
			targetMethodName2 ="a";
			
		} else {
			targetMethodName ="calculateCelestialAngle";
			targetMethodName2 ="getMoonPhase";
		}

		//set up ASM class manipulation stuff. Consult the ASM docs for details
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);



		//Now we loop over all of the methods declared inside the class until we get to the targetMethodName

		// find method to inject into
		Iterator<MethodNode> methods = classNode.methods.iterator();
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if (debug) System.out.println("*********RealTime Method Name: "+m.name + " Desc:" + m.desc);

			// calculateCelestialAngle
			if (m.name.equals(targetMethodName) && m.desc.equals("(JF)F")) {
				if (debug) System.out.println("*********RealTime Inside target method!");

				InsnList toInject = new InsnList();

				toInject.add(new VarInsnNode(Opcodes.LLOAD, 1));
				toInject.add(new VarInsnNode(Opcodes.FLOAD, 3));
				toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/petercashel/RealTime/RealTime", "calculateRealTime", "(JF)F"));
				toInject.add(new InsnNode(Opcodes.FRETURN));

				//	                    LLOAD 1
				//	                    FLOAD 3
				//	                    INVOKESTATIC net/petercashel/RealTime/RealTime.calculateRealTime (JF)F
				//	                    FRETURN

				m.instructions.clear();
				m.instructions.add(toInject);


				if (debug) System.out.println("RealTime Patching Complete!");
			}

			// getMoonPhase
			if (m.name.equals(targetMethodName2) && m.desc.equals("(J)I")) {
				if (debug) System.out.println("*********RealTime Inside target method!");

				InsnList toInject = new InsnList();

				toInject.add(new VarInsnNode(Opcodes.LLOAD, 1));
				toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/petercashel/RealTime/RealTime", "getMoonPhase", "(J)I"));
				toInject.add(new InsnNode(Opcodes.IRETURN));

				//	                    LLOAD 1
				//	                    FLOAD 3
				//	                    INVOKESTATIC net/petercashel/RealTime/RealTime.getMoonPhase (J)I
				//	                    FRETURN

				m.instructions.clear();
				m.instructions.add(toInject);


				if (debug) System.out.println("RealTime Patching Complete!");
			}


		}

		//ASM specific for cleaning up and returning the final bytes for JVM processing.
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
