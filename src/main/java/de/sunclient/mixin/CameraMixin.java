package de.sunclient.mixin;

import de.sunclient.Freelook;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Freelook: Die Kamera benutzt die Freelook-Blickrichtung statt der Spieler-Blickrichtung. */
@Mixin(class_4184.class)
public class CameraMixin {
	@Redirect(method = "method_19321",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5705(F)F"),
		require = 0)
	private float sunclient$yaw(class_1297 entity, float partialTick) {
		if (Freelook.active && entity == class_310.method_1551().field_1724) return Freelook.yaw;
		return entity.method_5705(partialTick);
	}

	@Redirect(method = "method_19321",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5695(F)F"),
		require = 0)
	private float sunclient$pitch(class_1297 entity, float partialTick) {
		if (Freelook.active && entity == class_310.method_1551().field_1724) return Freelook.pitch;
		return entity.method_5695(partialTick);
	}
}
