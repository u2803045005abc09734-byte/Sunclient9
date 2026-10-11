package de.sunclient.mixin;

import de.sunclient.Freelook;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: Mausbewegung dreht die Kamera, nicht den Spieler. */
@Mixin(class_1297.class)
public class EntityMixin {
	@Inject(method = "method_64578", at = @At("HEAD"), cancellable = true, require = 0)
	private void sunclient$rotate(float yaw, boolean relativeYaw, float pitch, boolean relativePitch, CallbackInfo ci) {
		class_310 mc = class_310.method_1551();
		if (!Freelook.active || mc.field_1724 == null || (Object) this != mc.field_1724) return;
		float yawDelta = relativeYaw ? yaw : yaw - mc.field_1724.method_36454();
		float pitchDelta = relativePitch ? pitch : pitch - mc.field_1724.method_36455();
		Freelook.turnDegrees(yawDelta, pitchDelta);
		ci.cancel();
	}
}
