package com.simibubi.create.foundation.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import committee.nova.mkb.api.IKeyBinding;

@Mixin(KeyMapping.class)
public abstract class ModernKeyBindingMixin implements IKeyBinding {

	// Aether and ModernKeyBinding provide conflicting default implementations.
	@Unique
	@Override
	public boolean isActiveAndMatches(InputConstants.Key keyCode) {
		return IKeyBinding.super.isActiveAndMatches(keyCode);
	}
}
