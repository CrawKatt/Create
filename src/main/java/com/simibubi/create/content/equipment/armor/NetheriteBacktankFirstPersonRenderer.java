package com.simibubi.create.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;

import io.github.fabricators_of_create.porting_lib.client_events.event.client.RenderArmEvent;

public class NetheriteBacktankFirstPersonRenderer {

	private static final ResourceLocation BACKTANK_ARMOR_LOCATION =
		Create.asResource("textures/models/armor/netherite_diving_arm.png");

	private static boolean rendererActive = false;

	public static void clientTick() {
		Minecraft mc = Minecraft.getInstance();
		rendererActive =
			mc.player != null && AllItems.NETHERITE_BACKTANK.isIn(mc.player.getItemBySlot(EquipmentSlot.CHEST));
	}

	public static void onRenderPlayerHand(RenderArmEvent event) {
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource buffer = event.getMultiBufferSource();
		HumanoidArm arm = event.getArm();
		AbstractClientPlayer player = event.getPlayer();

		if (!rendererActive)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (!(mc.getEntityRenderDispatcher()
			.getRenderer(player) instanceof PlayerRenderer pr))
			return;

		PlayerModel<AbstractClientPlayer> model = pr.getModel();
		model.attackTime = 0.0F;
		model.crouching = false;
		model.swimAmount = 0.0F;
		model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		ModelPart armPart = arm == HumanoidArm.LEFT ? model.leftSleeve : model.rightSleeve;
		armPart.xRot = 0.0F;
		armPart.render(poseStack, buffer.getBuffer(RenderType.entitySolid(BACKTANK_ARMOR_LOCATION)),
			LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		event.setCanceled(true);
	}

}
