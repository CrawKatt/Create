package com.simibubi.create.foundation.mixin.fabric.gametest;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.mixin.accessor.GameTestHelperAccessor;

import io.github.fabricators_of_create.porting_lib.gametest.infrastructure.ExtendedTestFunction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;

@Mixin(value = ExtendedTestFunction.class, remap = false)
public class PortingLibGameTestHelperMixin {
	@Inject(method = "asConsumer", at = @At("RETURN"), cancellable = true)
	private static void create$useDeclaredHelper(Method method,
		CallbackInfoReturnable<Consumer<GameTestHelper>> cir) {
		Class<?> helperType = method.getParameterTypes()[0];
		if (helperType == GameTestHelper.class)
			return;

		Consumer<GameTestHelper> test = cir.getReturnValue();
		cir.setReturnValue(original -> {
			if (helperType.isInstance(original)) {
				test.accept(original);
				return;
			}
			// beta.91 validates @CustomGameTestHelper but never constructs that helper.
			GameTestHelperAccessor access = (GameTestHelperAccessor) original;
			try {
				GameTestHelper helper = (GameTestHelper) helperType.getConstructor(GameTestInfo.class)
					.newInstance(access.getTestInfo());
				GameTestHelperAccessor customAccess = (GameTestHelperAccessor) helper;
				customAccess.setFinalCheckAdded(access.getFinalCheckAdded());
				try {
					test.accept(helper);
				} finally {
					access.setFinalCheckAdded(customAccess.getFinalCheckAdded());
				}
			} catch (ReflectiveOperationException e) {
				throw new IllegalArgumentException("Cannot construct GameTest helper " + helperType.getName(), e);
			}
		});
	}
}
