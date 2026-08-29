package com.simibubi.create.foundation.mixin;

import java.util.LinkedHashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.events.CommonEvents;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;

// vanilla 1.21.1 no longer supports adding repository sources after construction,
// so inject Create's dynamic data pack here instead
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

	@Mutable
	@Final
	@Shadow
	private Set<RepositorySource> sources;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void create$addDynamicData(RepositorySource[] vanillaSources, CallbackInfo ci) {
		RepositorySource dynamic = CommonEvents.consumeDynamicPackSource();
		if (dynamic == null)
			return;
		Set<RepositorySource> copy = new LinkedHashSet<>(sources);
		copy.add(dynamic);
		this.sources = copy;
	}
}
