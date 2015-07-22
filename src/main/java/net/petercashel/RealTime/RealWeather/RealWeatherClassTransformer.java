package net.petercashel.RealTime.RealWeather;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.relauncher.FMLRelaunchLog;


public class RealWeatherClassTransformer implements net.minecraft.launchwrapper.IClassTransformer {

	final static boolean isDebugEnvironment = Boolean.parseBoolean(System.getenv("JavaDebugEnvironment"));
	private static boolean debugTargetOnly = false;

	// Static class to record all the names of classes, methods and fields for ASM

	//Class:
	//World
	static String World = "net.minecraft.world.World";
	static String WorldOBF = "ahb";

	//Methods:
	//updateWeatherBody
	static String updateWeatherBody = "updateWeatherBody";
	static String updateWeatherBodyOBF  = "updateWeatherBody"; // This may be forge added...
	//Sig 
	static String updateWeatherBodySig = "()V";
	static String updateWeatherBodySigOBF = "()V";

	// Static class to record all the names of classes, methods and fields for ASM 

	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {

		if (arg0.equals(WorldOBF)) {
			if (isDebugEnvironment) System.out.println("*********RealTime RealWeather INSIDE OBFUSCATED WORLD TRANSFORMER ABOUT TO PATCH: " + arg0);
			return patchClassASMWorld(arg0, arg2, true);
		}

		if (arg0.equals(World)) {
			if (isDebugEnvironment) System.out.println("*********RealTime RealWeather INSIDE WORLD TRANSFORMER ABOUT TO PATCH: " + arg0);
			return patchClassASMWorld(arg0, arg2, false);
		}


		if (arg0.equals("net.petercashel.RealTime.RealWeather.RealWeatherWorld")) {
			if (isDebugEnvironment) System.out.println("*********RealTime RealWeather INSIDE TRANSFORMER Dumping!: " + arg0);
			return patchClassASMWorld(arg0, arg2, false);
		}

		return arg2;
	}

	//patchClassASM for WorldProvider. Specifically named due to upcoming expansion to properly support weather.
	public byte[] patchClassASMWorld(String name, byte[] bytes, boolean obfuscated) {

		String targetMethodName = "";
		String targetMethodSig = "";
		String calledSig = "";

		if(obfuscated == true) {
			// These link to static string references to the obfuscated method names. 
			// These are pulled from modding resources inside the MCP.
			// It's quite normal for obfuscated method names to have the same name
			// so long as the parameters are different.

			//updateWeatherBody
			targetMethodName = updateWeatherBodyOBF;
			targetMethodSig = updateWeatherBodySigOBF;
			calledSig = "(Lnet/minecraft/world/WorldProvider;Lnet/minecraft/world/storage/WorldInfo;Ljava/util/Random;FFFFZ)V";

		} else {
			targetMethodName = updateWeatherBody;
			targetMethodSig = updateWeatherBodySig;
			calledSig = "(Lnet/minecraft/world/WorldProvider;Lnet/minecraft/world/storage/WorldInfo;Ljava/util/Random;FFFFZ)V";
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
			if (isDebugEnvironment) {
				if (!debugTargetOnly) {
					System.out.println("*********RealTime RealWeather Method Name: "+m.name + " Desc:" + m.desc);
				} else {
					if (m.desc.equals(targetMethodSig)) {
						System.out.println("*********RealTime RealWeather Method Name: "+m.name + " Desc:" + m.desc);
					}					
				}				
			}

			// updateWeatherBody
			if (m.name.equals(targetMethodName) && m.desc.equals(targetMethodSig)) {
				if (isDebugEnvironment) System.out.println("*********RealTime RealWeather Inside target method!");

				InsnList toInject = new InsnList();
				if (!obfuscated) {
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "provider", "Lnet/minecraft/world/WorldProvider;"));
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "worldInfo", "Lnet/minecraft/world/storage/WorldInfo;"));
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "rand", "Ljava/util/Random;"));
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "thunderingStrength", "F"));
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "rainingStrength", "F"));
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "prevThunderingStrength", "F"));
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "prevRainingStrength", "F"));
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "isRemote", "Z"));
					toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/petercashel/RealTime/RealWeather/RealWeatherWorld", "updateWeatherBody", calledSig, false));
					toInject.add(new InsnNode(Opcodes.RETURN));
				} else {
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_73011_w", "Laqo;")); //provider
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_72986_A", "Lays;")); //worldInfo
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_73012_v", "Ljava/util/Random;")); //rand
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_73017_q", "F")); //thunderingStrength
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_73004_o", "F")); //rainingStrength
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_73018_p", "F")); //prevThunderingStrength
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_73003_n", "F")); //prevRainingStrength
					toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "ahb", "field_72995_K", "Z")); //isRemote
					toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/petercashel/RealTime/RealWeather/RealWeatherWorld", "updateWeatherBody", calledSig, false));
					toInject.add(new InsnNode(Opcodes.RETURN));
				}
				m.instructions.clear();
				m.instructions.add(toInject);


				if (isDebugEnvironment) System.out.println("RealTime RealWeather Patching Complete!");
			}


		}

		//ASM specific for cleaning up and returning the final bytes for JVM processing.
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
