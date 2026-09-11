package com.simibubi.create;

import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.client.ConflictSafeKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public enum AllKeys {

	TOOL_MENU("toolmenu", GLFW.GLFW_KEY_LEFT_ALT, "Focus Schematic Overlay"),
	ACTIVATE_TOOL(GLFW.GLFW_KEY_LEFT_CONTROL),
	TOOLBELT("toolbelt", GLFW.GLFW_KEY_LEFT_ALT, "Access Nearby Toolboxes"),
	ROTATE_MENU("rotate_menu", GLFW.GLFW_KEY_UNKNOWN, "Open Block Rotation Menu"),

	SHIFT_MODIFIER("shift_modifier", GLFW.GLFW_KEY_LEFT_SHIFT, "Shift Modifier", true),
	CTRL_MODIFIER("ctrl_modifier", GLFW.GLFW_KEY_LEFT_CONTROL, "Ctrl Modifier", true),
	ALT_MODIFIER("alt_modifier", GLFW.GLFW_KEY_LEFT_ALT, "Alt Modifier", true),
	;

	private KeyMapping keybind;
	private final String description;
	private final String translation;
	private final int key;
	private final boolean modifiable;
	private final boolean conflictSafe;

	AllKeys(int defaultKey) {
		this("", defaultKey, "", true);
	}

	AllKeys(String description, int defaultKey, String translation) {
		this(description, defaultKey, translation, false);
	}

	AllKeys(String description, int defaultKey, String translation, boolean conflictSafe) {
		this.description = Create.ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.translation = translation;
		this.conflictSafe = conflictSafe;
	}

	public static void provideLang(BiConsumer<String, String> consumer) {
		for (AllKeys key : values())
			if (key.modifiable)
				consumer.accept(key.description, key.translation);
	}

	public static void register() {
		for (AllKeys key : values()) {
			key.keybind = key.conflictSafe
				? new ConflictSafeKeyMapping(key.description, key.key, Create.NAME)
				: new KeyMapping(key.description, key.key, Create.NAME);
			if (!key.modifiable)
				continue;
			KeyBindingHelper.registerKeyBinding(key.keybind);
		}
	}

	// fabric: sometimes after opening the toolbox menu, alt gets stuck as pressed until a screen is opened.
	// why is this needed? why did this only just now break? Good questions! I wish I knew.
	public static void fixBinds() {
		long window = Minecraft.getInstance().getWindow().getWindow();
		for (AllKeys key : values()) {
			if (key.keybind == null || key.keybind.isUnbound())
				continue;
			key.keybind.setDown(InputConstants.isKeyDown(window, key.getBoundCode()));
		}
	}

	public KeyMapping getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		if (!modifiable)
			return isKeyDown(key);
		return keybind.isDown();
	}

	public String getBoundKey() {
		return keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public int getBoundCode() {
		return KeyBindingHelper.getBoundKeyOf(keybind)
				.getValue();
	}

	public static boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(), key);
	}

	public static boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance()
			.getWindow()
			.getWindow(), button) == 1;
	}

	public static boolean ctrlDown() {
		return isKeyDown(CTRL_MODIFIER.getBoundCode());
	}

	public static boolean shiftDown() {
		return isKeyDown(SHIFT_MODIFIER.getBoundCode());
	}

	public static boolean altDown() {
		return isKeyDown(ALT_MODIFIER.getBoundCode());
	}

}
