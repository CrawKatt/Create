package com.simibubi.create;

import static net.minecraft.world.item.Items.BUCKET;
import static net.minecraft.world.item.Items.GLASS_BOTTLE;
import static net.minecraft.world.item.Items.HONEY_BOTTLE;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;

import io.github.fabricators_of_create.porting_lib.tags.Tags;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import io.github.fabricators_of_create.porting_lib.event.common.FluidPlaceBlockCallback;

@SuppressWarnings("UnstableApiUsage")
public class AllFluids {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key());
	}
	// Fabric: since a honey block is 4 bottles, we can't use the default 1/3 (27000)
	// we can't make a block take 108000, since then it can't fit in the basin
	public static final long HONEY_BOTTLE_AMOUNT = FluidConstants.BLOCK / 4;

	// fabric: various Attributes/Types replaced with corresponding handlers

	public static final FluidEntry<PotionFluid> POTION =
			REGISTRATE.virtualFluid("potion", /*PotionFluidAttributes::new,*/ PotionFluid::createSource, PotionFluid::createFlowing)
					.lang("Potion")
					.fluidAttributes(PotionFluidVariantAttributeHandler::new)
					.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
			.lang("Builder's Tea")
		.tag(AllFluidTags.TEA.tag)
			.fluidAttributes(() -> new CreateAttributeHandler("fluid.create.tea"))
			.onRegisterAfter(Registries.ITEM, tea -> {
				Fluid still = tea.getSource();
				FluidStorage.combinedItemApiProvider(AllItems.BUILDERS_TEA.get()).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(still), FluidConstants.BOTTLE));
				FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
						new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(AllItems.BUILDERS_TEA.get()), still, FluidConstants.BOTTLE));
			})
			.register();

	public static final FluidEntry<SimpleFlowableFluid.Flowing> HONEY =
			REGISTRATE.standardFluid("honey")
					.lang("Honey")
					.fluidProperties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.flowSpeed(3)
							.blastResistance(100f))
					.fluidAttributes(() -> new CreateAttributeHandler("block.create.honey", 2000, 1400))
					.tag(Tags.Fluids.HONEY, FluidTags.WATER) // fabric: water tag controls physics
					.source(SimpleFlowableFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
					.bucket()
					.tag(AllTags.commonItemTag("buckets"), AllItemTags.HONEY_BUCKETS.tag)
					.build()
					.onRegisterAfter(Registries.ITEM, honey -> {
						Fluid source = honey.getSource();
						FluidStorage.combinedItemApiProvider(HONEY_BOTTLE).register(context ->
								new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(source), HONEY_BOTTLE_AMOUNT));
						FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
								new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(HONEY_BOTTLE), source, HONEY_BOTTLE_AMOUNT));
						FluidStorage.combinedItemApiProvider(source.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(source), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(source.getBucket()), source, FluidConstants.BUCKET));
					})
					.register();

		public static final FluidEntry<SimpleFlowableFluid.Flowing> CHOCOLATE =
				REGISTRATE.standardFluid("chocolate")
						.lang("Chocolate")
						.tag(AllFluidTags.CHOCOLATE.tag, FluidTags.WATER) // fabric: water tag controls physics
					.fluidProperties(p -> p.levelDecreasePerBlock(2)
								.tickRate(25)
								.flowSpeed(3)
								.blastResistance(100f))
						.fluidAttributes(() -> new CreateAttributeHandler("block.create.chocolate", 1500, 1400))
						.source(SimpleFlowableFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
						.bucket()
						.tag(AllTags.commonItemTag("buckets"), AllItemTags.CHOCOLATE_BUCKETS.tag)
						.build()
						.onRegisterAfter(Registries.ITEM, chocolate -> {
						Fluid source = chocolate.getSource();
						// transfer values
						FluidStorage.combinedItemApiProvider(source.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(source), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(source.getBucket()), source, FluidConstants.BUCKET));
					})
						.register();

	// fabric: replaces the abandoned milk-lib dependency
	public static final TagKey<Fluid> MILK_FLUID_TAG = AllTags.commonFluidTag("milk");

	public static final FluidEntry<SimpleFlowableFluid.Flowing> MILK =
			REGISTRATE.standardFluid("milk")
					.lang("Milk")
					.tag(MILK_FLUID_TAG, FluidTags.WATER) // fabric: water tag controls physics
					.fluidProperties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.flowSpeed(3)
							.blastResistance(100f))
					.fluidAttributes(() -> new CreateAttributeHandler("block.create.milk", 1000, 1030))
					.source(SimpleFlowableFluid.Source::new)
					.bucket()
					.build()
					.register();

	// Load this class

	public static void register() {
	}

	@Environment(EnvType.CLIENT)
	public static void initRendering() {
		// FluidRenderHandler has sane defaults and is handled by registrate
		// potion is the only fluid with custom variant rendering
		PotionFluidVariantRenderHandler handler = new PotionFluidVariantRenderHandler();
		PotionFluid potionFluid = AllFluids.POTION.get();
		FluidVariantRendering.register(potionFluid.getFlowing(), handler);
		FluidVariantRendering.register(potionFluid.getSource(), handler);
	}

	public static void registerFluidInteractions() {
		// fabric: no fluid interaction API, use legacy method
		FluidPlaceBlockCallback.EVENT.register(AllFluids::whenFluidsMeet);
	}

	public static BlockState whenFluidsMeet(LevelAccessor world, BlockPos pos, BlockState blockState) {
		FluidState fluidState = blockState.getFluidState();

		if (fluidState.isSource() && FluidHelper.isLava(fluidState.getType()))
			return null;

		for (Direction direction : Iterate.directions) {
			FluidState metFluidState =
					fluidState.isSource() ? fluidState : world.getFluidState(pos.relative(direction));
			if (!metFluidState.is(FluidTags.WATER))
				continue;
			BlockState lavaInteraction = AllFluids.getLavaInteraction(metFluidState);
			if (lavaInteraction == null)
				continue;
			return lavaInteraction;
		}
		return null;
	}

	@Nullable
	public static BlockState getLavaInteraction(FluidState fluidState) {
		Fluid fluid = fluidState.getType();
		if (fluid.isSame(HONEY.get()))
			return AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
					.get()
					.defaultBlockState();
		if (fluid.isSame(CHOCOLATE.get()))
			return AllPaletteStoneTypes.SCORIA.getBaseBlock()
					.get()
					.defaultBlockState();
		return null;
	}

//	/**
//	 * Removing alpha from tint prevents optifine from forcibly applying biome
//	 * colors to modded fluids (Makes translucent fluids disappear)
//	 */
//	private static class NoColorFluidAttributes extends FluidAttributes {
//
//		protected NoColorFluidAttributes(Builder builder, Fluid fluid) {
//			super(builder, fluid);
//		}
//
//		@Override
//		public int getColor(BlockAndTintGetter world, BlockPos pos) {
//			return 0x00ffffff;
//		}
//
//	}

	@Environment(EnvType.CLIENT)
	public static class PotionFluidVariantRenderHandler implements FluidVariantRenderHandler {
		@Override
		public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
			PotionContents contents = fluidVariant.getComponentMap()
				.get(DataComponents.POTION_CONTENTS);
			return (contents == null ? PotionContents.EMPTY : contents).getColor() | 0xff000000;
		}

		@Override
		public void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipContext) {
			PotionFluidHandler.addPotionTooltip(fluidVariant, tooltip::add, 1);
		}
	}

	private static class PotionFluidVariantAttributeHandler implements FluidVariantAttributeHandler {
		@Override
		public Component getName(FluidVariant fluidVariant) {
			return Component.translatable(getTranslationKey(fluidVariant));
		}

		public String getTranslationKey(FluidVariant stack) {
			DataComponentMap components = stack.getComponentMap();
			BottleType bottleType = components.get(AllDataComponents.POTION_FLUID_BOTTLE_TYPE);
			ItemLike itemFromBottleType =
					PotionFluidHandler.itemFromBottleType(bottleType == null ? BottleType.REGULAR : bottleType);
			PotionContents contents = components.get(DataComponents.POTION_CONTENTS);
			if (contents == null)
				return "create.potion.invalid";
			return Potion.getName(contents.potion(), itemFromBottleType.asItem()
					.getDescriptionId() + ".effect.");
		}
	}

	private record CreateAttributeHandler(Component name, int viscosity, boolean lighterThanAir) implements FluidVariantAttributeHandler {
		private CreateAttributeHandler(String key, int viscosity, int density) {
			this(Component.translatable(key), viscosity, density <= 0);
		}

		public CreateAttributeHandler(String key) {
			this(key, FluidConstants.WATER_VISCOSITY, 1000);
		}

		@Override
		public Component getName(FluidVariant fluidVariant) {
			return name.copy();
		}

		@Override
		public int getViscosity(FluidVariant variant, @Nullable Level world) {
			return viscosity;
		}

		@Override
		public boolean isLighterThanAir(FluidVariant variant) {
			return lighterThanAir;
		}
	}
}
