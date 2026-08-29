package com.simibubi.create.foundation.utility;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.EnvType;

import io.github.fabricators_of_create.porting_lib.common.util.EnvExecutor;

@ApiStatus.Internal
@Deprecated(forRemoval = true, since = "1.21")
public class DistExecutor {
	/**
	 * This will be removed once there is more time to refactor its uses, this is not considered API,
	 * it is only for internal use and legacy reasons, this WILL be removed shortly, and you should not rely on or copy this code
	 */
	@ApiStatus.Internal
	@Deprecated(forRemoval = true, since = "1.21")
	public static <T> T unsafeCallWhenOn(EnvType dist, Supplier<Callable<T>> toRun) {
		return EnvExecutor.callWhenOn(dist, toRun);
	}
}
