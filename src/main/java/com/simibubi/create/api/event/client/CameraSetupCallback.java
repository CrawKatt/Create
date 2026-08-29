package com.simibubi.create.api.event.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class CameraSetupCallback {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class,
		callbacks -> info -> {
			for (Callback callback : callbacks)
				callback.onCameraSetup(info);
		});

	@FunctionalInterface
	public interface Callback {
		void onCameraSetup(CameraInfo info);
	}

	public static class CameraInfo {
		public float yaw;
		public float pitch;
		public float roll;

		public CameraInfo(float yaw, float pitch, float roll) {
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
		}
	}
}
