package com.simibubi.create.foundation.mixin.compat;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Exercises the compiled bridge without bootstrapping a Minecraft client. */
public class ModernKeyBindingCompatTest {

	public interface ModernKeys {
		default boolean isActiveAndMatches(Object key) {
			return Boolean.TRUE.equals(key);
		}
	}

	public interface AetherKeys {
		default boolean isActiveAndMatches(Object key) {
			return true;
		}
	}

	public static void main(String[] args) throws Exception {
		try {
			((ModernKeys) binding("ConflictingKeys", null)).isActiveAndMatches(true);
			throw new AssertionError("The unpatched conflicting defaults must reproduce AbstractMethodError");
		} catch (AbstractMethodError expected) {
			// Same JVM dispatch failure as the reported crash.
		}

		ClassNode mixin = new ClassNode();
		try (InputStream input = ModernKeyBindingCompatTest.class.getClassLoader().getResourceAsStream(
			"com/simibubi/create/foundation/mixin/compat/ModernKeyBindingMixin.class")) {
			if (input == null)
				throw new AssertionError("Compiled compatibility mixin is missing");
			new ClassReader(input).accept(mixin, 0);
		}
		MethodNode bridge = mixin.methods.stream()
			.filter(method -> method.name.equals("isActiveAndMatches"))
			.findFirst().orElseThrow();
		if (!bridge.desc.equals("(Lcom/mojang/blaze3d/platform/InputConstants$Key;)Z"))
			throw new AssertionError("Unexpected bridge signature: " + bridge.desc);

		// Transplant the actual compiled method, changing only the optional API types to fixtures.
		bridge.desc = "(Ljava/lang/Object;)Z";
		bridge.localVariables = null;
		int delegates = 0;
		for (var instruction : bridge.instructions) {
			if (instruction instanceof MethodInsnNode call) {
				if (call.getOpcode() != Opcodes.INVOKESPECIAL || !call.itf
					|| !call.owner.equals("committee/nova/mkb/api/IKeyBinding")
					|| !call.name.equals("isActiveAndMatches"))
					throw new AssertionError("Bridge must call the ModernKeyBinding interface default directly");
				call.owner = Type.getInternalName(ModernKeys.class);
				call.desc = bridge.desc;
				delegates++;
			}
		}
		if (delegates != 1)
			throw new AssertionError("Expected exactly one ModernKeyBinding default call");

		Object resolved = binding("ResolvedKeys", bridge);
		for (boolean decision : new boolean[] {true, false}) {
			if (((ModernKeys) resolved).isActiveAndMatches(decision) != decision
				|| ((AetherKeys) resolved).isActiveAndMatches(decision) != decision)
				throw new AssertionError("Both interfaces must preserve the ModernKeyBinding decision");
		}
		System.out.println("Keybinding compatibility: reproduced original error; compiled bridge preserves both decisions through both interfaces.");
	}

	private static Object binding(String name, MethodNode bridge) throws ReflectiveOperationException {
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		String internalName = ModernKeyBindingCompatTest.class.getPackageName().replace('.', '/') + "/" + name;
		writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object",
			new String[] {Type.getInternalName(ModernKeys.class), Type.getInternalName(AetherKeys.class)});
		var constructor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		constructor.visitCode();
		constructor.visitVarInsn(Opcodes.ALOAD, 0);
		constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		constructor.visitInsn(Opcodes.RETURN);
		constructor.visitMaxs(0, 0);
		constructor.visitEnd();
		if (bridge != null)
			bridge.accept(writer);
		writer.visitEnd();
		return MethodHandles.lookup().defineClass(writer.toByteArray()).getConstructor().newInstance();
	}
}
