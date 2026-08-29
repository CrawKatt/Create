package com.simibubi.create;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.ftb.FTBIntegration;
import com.simibubi.create.compat.sodium.SodiumCompat;
import com.simibubi.create.compat.trinkets.Trinkets;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHandler;
import com.simibubi.create.content.contraptions.render.ContraptionRenderInfo;
import com.simibubi.create.content.contraptions.render.ContraptionRenderInfoManager;
import com.simibubi.create.content.decoration.encasing.CasingConnectivity;
import com.simibubi.create.content.equipment.armor.RemainingAirOverlay;
import com.simibubi.create.content.equipment.bell.BasicParticleData;
import com.simibubi.create.content.equipment.bell.SoulPulseEffectHandler;
import com.simibubi.create.content.equipment.bell.SoulBaseParticle;
import com.simibubi.create.content.equipment.bell.SoulParticle;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonRenderHandler;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.equipment.zapper.ZapperRenderHandler;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer;
import com.simibubi.create.content.logistics.packagerLink.WiFiParticle;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import com.simibubi.create.content.schematics.client.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.TrainHUD;
import com.simibubi.create.content.trains.track.TrackPlacementOverlay;
import com.simibubi.create.foundation.ClientResourceReloadListener;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsClient;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItems;
import com.simibubi.create.foundation.model.ModelSwapper;
import com.simibubi.create.foundation.events.ClientEvents;
import com.simibubi.create.foundation.events.InputEvents;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.gui.CreateMainMenuScreen;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.createmod.catnip.config.ui.ConfigScreen;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class CreateClient implements ClientModInitializer {

	public static final ModelSwapper MODEL_SWAPPER = new ModelSwapper();
	public static final CasingConnectivity CASING_CONNECTIVITY = new CasingConnectivity();

	public static final ClientSchematicLoader SCHEMATIC_SENDER = new ClientSchematicLoader();
	public static final SchematicHandler SCHEMATIC_HANDLER = new SchematicHandler();
	public static final SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();
	public static final SuperGlueSelectionHandler GLUE_HANDLER = new SuperGlueSelectionHandler();

	public static final ZapperRenderHandler ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
	public static final PotatoCannonRenderHandler POTATO_CANNON_RENDER_HANDLER = new PotatoCannonRenderHandler();
	public static final SoulPulseEffectHandler SOUL_PULSE_EFFECT_HANDLER = new SoulPulseEffectHandler();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final ValueSettingsClient VALUE_SETTINGS_HANDLER = new ValueSettingsClient();

	public static void registerBasicParticleFactory(ParticleType<BasicParticleData> type, BasicParticleData data) {
		if (data instanceof BasicParticleData.WiFiData) {
			ParticleFactoryRegistry.getInstance().register(type,
				sprites -> (options, level, x, y, z, vx, vy, vz) ->
					new WiFiParticle(level, x, y, z, vx, vy, vz, sprites));
		} else if (data instanceof BasicParticleData.SoulBaseData) {
			ParticleFactoryRegistry.getInstance().register(type,
				sprites -> (options, level, x, y, z, vx, vy, vz) ->
					new SoulBaseParticle(level, x, y, z, vx, vy, vz, sprites));
		} else {
			ParticleFactoryRegistry.getInstance().register(type,
				sprites -> (options, level, x, y, z, vx, vy, vz) ->
					new SoulParticle(level, x, y, z, vx, vy, vz, sprites, options));
		}
	}

	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

	public static <T extends Block> void registerCasingConnectivity(T entry,
			BiConsumer<T, CasingConnectivity> consumer) {
		consumer.accept(entry, CASING_CONNECTIVITY);
	}

	public static void registerBlockModel(Block entry,
			Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> factory) {
		MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), factory.get());
	}

	public static void registerItemModel(Item entry,
			Supplier<NonNullFunction<BakedModel, ? extends BakedModel>> factory) {
		MODEL_SWAPPER.getCustomItemModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), factory.get());
	}

	public static <B> void registerConnectedTextures(Block entry, Supplier<B> factory) {
		ConnectedTextureBehaviour behavior = (ConnectedTextureBehaviour) factory.get();
		MODEL_SWAPPER.getCustomBlockModels()
			.register(RegisteredObjectsHelper.getKeyOrThrow(entry), model -> new CTModel(model, behavior));
	}

	public static <T extends Item, P, R> void registerCustomRenderedItem(ItemBuilder<T, P> builder,
			Supplier<Supplier<R>> factory) {
		builder.onRegister(entry -> {
			CustomRenderedItemModelRenderer renderer = (CustomRenderedItemModelRenderer) factory.get().get();
			BuiltinItemRendererRegistry.INSTANCE.register(entry, renderer);
			CustomRenderedItems.register(entry);
		});
	}

	@Override
	public void onInitializeClient() {
		AllInstanceTypes.init();

		MODEL_SWAPPER.registerListeners();

		ZAPPER_RENDER_HANDLER.registerListeners();
		POTATO_CANNON_RENDER_HANDLER.registerListeners();

		Mods.FTBLIBRARY.executeIfInstalled(() -> () -> FTBIntegration.init());

		// clientInit start

		//BUFFER_CACHE.registerCompartment(CachedBufferer.GENERIC_BLOCK);
		//BUFFER_CACHE.registerCompartment(CachedPartialBuffers.partial);
		//BUFFER_CACHE.registerCompartment(CachedBufferer.DIRECTIONAL_PARTIAL);
		//BUFFER_CACHE.registerCompartment(KineticBlockEntityRenderer.KINETIC_BLOCK);
		//BUFFER_CACHE.registerCompartment(WaterWheelRenderer.WATER_WHEEL);
		//BUFFER_CACHE.registerCompartment(ContraptionRenderInfo.CONTRAPTION, 20);
		//BUFFER_CACHE.registerCompartment(WorldSectionElement.DOC_WORLD_SECTION, 20);

		SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.PARTIAL);
		SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.DIRECTIONAL_PARTIAL);
		SuperByteBufferCache.getInstance().registerCompartment(KineticBlockEntityRenderer.KINETIC_BLOCK);
		SuperByteBufferCache.getInstance().registerCompartment(WaterWheelRenderer.WATER_WHEEL);
		SuperByteBufferCache.getInstance().registerCompartment(ContraptionRenderInfo.CONTRAPTION, 20);

		AllKeys.register();
		AllPartialModels.init();


		//AllPonderTags.register();
		//PonderIndex.register();
		PonderIndex.addPlugin(new CreatePonderPlugin());

		setupConfigUIBackground();

		// fabric exclusive
		registerOverlays();
		ClientEvents.register();
		InputEvents.register();
		RenderTypes.init();
//		ArmorTextureRegistry.register(AllArmorMaterials.COPPER, CopperArmorItem.TEXTURE);
		AllFluids.initRendering();
		initCompat();
	}

	@SuppressWarnings("Convert2MethodRef") // may cause class loading issues if changed
	private static void initCompat() {
		Mods.TRINKETS.executeIfInstalled(() -> () -> Trinkets.clientInit());
		Mods.SODIUM.executeIfInstalled(() -> () -> SodiumCompat.init());
	}

	private static void registerOverlays() {
		HudRenderCallback.EVENT.register(RemainingAirOverlay.INSTANCE::render);
		HudRenderCallback.EVENT.register(TrainHUD.OVERLAY::render);
		HudRenderCallback.EVENT.register(GoggleOverlayRenderer.OVERLAY::render);
		HudRenderCallback.EVENT.register(BlueprintOverlayRenderer.OVERLAY::render);
		HudRenderCallback.EVENT.register(LinkedControllerClientHandler.OVERLAY::render);
		HudRenderCallback.EVENT.register(SCHEMATIC_HANDLER::render);
		HudRenderCallback.EVENT.register(ToolboxHandlerClient.OVERLAY::render);
		HudRenderCallback.EVENT.register(VALUE_SETTINGS_HANDLER::render);
		HudRenderCallback.EVENT.register(TrackPlacementOverlay.INSTANCE::render);
	}

	private static void setupConfigUIBackground() {
		ConfigScreen.backgrounds.put(Create.ID, (screen, graphics, partialTicks) -> {
			CreateMainMenuScreen.PANORAMA.render(graphics, screen.width, screen.height, 1, partialTicks);

			//RenderSystem.setShaderTexture(0, CreateMainMenuScreen.PANORAMA_OVERLAY_TEXTURES);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			graphics.blit(CreateMainMenuScreen.PANORAMA_OVERLAY_TEXTURES, 0, 0, screen.width, screen.height, 0.0F, 0.0F, 16, 128, 16, 128);

			graphics.fill(0, 0, screen.width, screen.height, 0x90_282c34);
		});

		ConfigScreen.shadowState = AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.Y);

		BaseConfigScreen.setDefaultActionFor(Create.ID, base -> base
				.withButtonLabels("Client Settings", "World Generation Settings", "Gameplay Settings")
				.withSpecs(AllConfigs.client().specification, AllConfigs.common().specification, AllConfigs.server().specification)
		);
	}

	public static void invalidateRenderers() {
		SCHEMATIC_HANDLER.updateRenderers();
		ContraptionRenderInfoManager.resetAll();
	}

	public static void checkGraphicsFanciness() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;

		if (mc.options.graphicsMode().get() != GraphicsStatus.FABULOUS)
			return;

		if (AllConfigs.client().ignoreFabulousWarning.get())
			return;

        MutableComponent text = ComponentUtils.wrapInSquareBrackets(Component.literal("WARN"))
			.withStyle(ChatFormatting.GOLD)
			.append(Component.literal(" Some of Create's visual features will not be available while Fabulous graphics are enabled!"))
			.withStyle(style -> {
                return style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/create dismissFabulousWarning"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.literal("Click here to disable this warning")));
            });

		mc.player.displayClientMessage(text, false);
	}

}
