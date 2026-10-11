package de.sunclient;

import net.minecraft.class_310;
import net.minecraft.class_5498;

/** Freelook: Kamera frei drehen, während die Spielerblickrichtung gleich bleibt. */
public final class Freelook {
	private Freelook() {}

	public static boolean active = false;
	public static float yaw = 0f;
	public static float pitch = 0f;
	private static class_5498 previous = null;

	public static void begin(class_310 mc) {
		if (active || mc.field_1724 == null) return;
		active = true;
		yaw = mc.field_1724.method_36454();
		pitch = mc.field_1724.method_36455();
		previous = mc.field_1690.method_31044();
		mc.field_1690.method_31043(class_5498.field_26665);
	}

	public static void end(class_310 mc) {
		if (!active) return;
		active = false;
		if (previous != null) mc.field_1690.method_31043(previous);
		previous = null;
	}

	/** Aktualisiert die freie Kamera mit bereits in Grad umgerechneten Drehwerten. */
	public static void turnDegrees(float yawDelta, float pitchDelta) {
		yaw += yawDelta;
		pitch += pitchDelta;
		if (pitch > 90f) pitch = 90f;
		if (pitch < -90f) pitch = -90f;
	}

	/** Mausbewegung (wie Entity.turn): dy = horizontal, dx = vertikal. */
	public static void turn(double dy, double dx) {
		yaw += (float) dy * 0.15f;
		pitch += (float) dx * 0.15f;
		if (pitch > 90f) pitch = 90f;
		if (pitch < -90f) pitch = -90f;
	}
}
