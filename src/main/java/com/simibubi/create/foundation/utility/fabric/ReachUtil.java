package com.simibubi.create.foundation.utility.fabric;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ReachUtil {
	public static double reach(LivingEntity entity) {
		return entity.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
	}
}
