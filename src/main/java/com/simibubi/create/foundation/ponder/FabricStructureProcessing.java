package com.simibubi.create.foundation.ponder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

/**
 * Processing for structures exported on Forge to allow using the same ones on Forge and Fabric.
 */
public class FabricStructureProcessing {
	public static final MapCodec<Processor> PROCESSOR_CODEC = ResourceLocation.CODEC
			.fieldOf("structureId")
			.xmap(Processor::new, processor -> processor.structureId);

	public static final StructureProcessorType<Processor> PROCESSOR_TYPE = Registry.register(
			BuiltInRegistries.STRUCTURE_PROCESSOR,
			Create.asResource("fabric_structure_processor"),
			() -> PROCESSOR_CODEC
	);

	/**
	 * A predicate that makes all processes apply to all schematics.
	 */
	public static final ProcessingPredicate ALWAYS = (id, process) -> true;

	private static final Map<String, ProcessingPredicate> predicates = new HashMap<>();

	/**
	 * Register a {@link ProcessingPredicate} for a mod.
	 * Only one predicate may be registered for each mod.
	 * The predicate determines which {@link Process}es will be applied to which schematics.
	 */
	public static ProcessingPredicate register(String modId, ProcessingPredicate predicate) {
		ProcessingPredicate existing = predicates.get(modId);
		if (existing != null) {
			throw new IllegalStateException(
					"Tried to register ProcessingPredicate [%s] for mod '%s', while one already exists: [%s]"
							.formatted(predicate, modId, existing)
			);
		}
	    predicates.put(modId, predicate);
		return predicate;
	}

	@Internal
	public static void init() {
		register(Create.ID, ALWAYS);
	}

	public enum Process {
		FLUID_AMOUNTS
	}

	@FunctionalInterface
	public interface ProcessingPredicate {
		boolean shouldApplyProcess(ResourceLocation schematicId, Process process);
	}

	public static class Processor extends StructureProcessor {
		public final ResourceLocation structureId;

		public Processor(ResourceLocation structureId) {
			this.structureId = structureId;
		}

		@Nullable
		@Override
		public StructureTemplate.StructureBlockInfo processBlock(
				@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockPos pivot,
				@NotNull StructureBlockInfo blockInfo, @NotNull StructureBlockInfo relativeBlockInfo,
				@NotNull StructurePlaceSettings settings) {
			ProcessingPredicate predicate = predicates.get(structureId.getNamespace());
			if (predicate == null) // do nothing
				return relativeBlockInfo;

			CompoundTag nbt = relativeBlockInfo.nbt();
			if (nbt == null)
				return relativeBlockInfo;

			if (!predicate.shouldApplyProcess(structureId, Process.FLUID_AMOUNTS))
				return relativeBlockInfo;

			if ((AllBlocks.FLUID_TANK.has(relativeBlockInfo.state()) || AllBlocks.CREATIVE_FLUID_TANK.has(relativeBlockInfo.state()))
				&& nbt.contains("TankContent", Tag.TAG_COMPOUND)) {
				CompoundTag copy = nbt.copy();
				fixTankContent(level, copy.getCompound("TankContent"));
				return new StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), copy);
			} else if (AllBlocks.THRESHOLD_SWITCH.has(relativeBlockInfo.state())
				&& nbt.contains("CurrentMaxAmount", Tag.TAG_ANY_NUMERIC)) {
				CompoundTag copy = nbt.copy();
				fixThresholdAmount(copy, "OnAboveAmount");
				fixThresholdAmount(copy, "OffBelowAmount");
				fixThresholdAmount(copy, "CurrentMinAmount");
				fixThresholdAmount(copy, "CurrentAmount");
				fixThresholdAmount(copy, "CurrentMaxAmount");
				return new StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), copy);
			} else if (AllBlocks.BASIN.has(relativeBlockInfo.state())) {
				CompoundTag copy = nbt.copy();
				ListTag inputTanks = copy.getList("InputTanks", Tag.TAG_COMPOUND);
				if (!inputTanks.isEmpty()) {
					for (int i = 0; i < inputTanks.size(); i++) {
						CompoundTag compound = inputTanks.getCompound(i);
						CompoundTag content = compound.getCompound("TankContent");
						fixTankContent(level, content);
					}
				}
				ListTag outputTanks = copy.getList("OutputTanks", Tag.TAG_COMPOUND);
				if (!outputTanks.isEmpty()) {
					for (int i = 0; i < outputTanks.size(); i++) {
						CompoundTag compound = outputTanks.getCompound(i);
						CompoundTag content = compound.getCompound("TankContent");
						fixTankContent(level, content);
					}
				}

				return new StructureBlockInfo(relativeBlockInfo.pos(), relativeBlockInfo.state(), copy);
			}

			// no processes were applied
			return relativeBlockInfo;
		}

		@Override
		@NotNull
		protected StructureProcessorType<?> getType() {
			return PROCESSOR_TYPE;
		}
	}

	private static void fixThresholdAmount(CompoundTag nbt, String key) {
		if (nbt.contains(key, Tag.TAG_ANY_NUMERIC))
			nbt.putInt(key, (int) Math.round(nbt.getDouble(key) / 1000d * FluidConstants.BUCKET));
	}

	private static void fixTankContent(LevelReader level, CompoundTag content) {
		CompoundTag fluidTag = content.contains("Fluid", Tag.TAG_COMPOUND) ? content.getCompound("Fluid") : content;
		if (fluidTag.isEmpty())
			return;
		FluidStack stack;
		if (fluidTag.contains("FluidName", Tag.TAG_STRING)) {
			long amount = fluidTag.getLong("Amount");
			String name = fluidTag.getString("FluidName");
			if (amount <= 0 || name.equals("minecraft:empty"))
				return;

			ResourceLocation id = ResourceLocation.tryParse(name);
			Fluid fluid;
			if (name.equals("minecraft:milk") || name.equals("milk:still_milk")) {
				fluid = AllFluids.MILK.getSource();
			} else if (id == null) {
				return;
			} else {
				fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(Fluids.EMPTY);
			}
			if (fluid == Fluids.EMPTY)
				return;

			stack = new FluidStack(fluid, amount);
			if (fluid == AllFluids.POTION.getSource() && fluidTag.contains("Tag", Tag.TAG_COMPOUND)) {
				CompoundTag potionTag = fluidTag.getCompound("Tag");
				ResourceLocation potionId = ResourceLocation.tryParse(potionTag.getString("Potion"));
				if (potionId != null) {
					Optional<? extends net.minecraft.core.Holder<Potion>> potion = level.registryAccess()
						.lookupOrThrow(Registries.POTION)
						.get(ResourceKey.create(Registries.POTION, potionId));
					if (potion.isPresent()) {
						BottleType bottle = switch (potionTag.getString("Bottle")) {
							case "SPLASH" -> BottleType.SPLASH;
							case "LINGERING" -> BottleType.LINGERING;
							default -> BottleType.REGULAR;
						};
						stack = PotionFluid.of(amount, new PotionContents(potion.get()), bottle);
					}
				}
			}
		} else {
			Optional<FluidStack> optionalStack = FluidStack.parse(level.registryAccess(), fluidTag);
			if (optionalStack.isEmpty())
				return;
			stack = optionalStack.get();
		}
		long amount = stack.getAmount();
		double buckets = amount / 1000d;
		long fixedAmount = Math.round(buckets * FluidConstants.BUCKET);
		stack.setAmount(fixedAmount);
		Set.copyOf(content.getAllKeys()).forEach(content::remove);
		content.put("Fluid", stack.save(level.registryAccess()));
	}
}
