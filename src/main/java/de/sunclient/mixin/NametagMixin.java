package de.sunclient.mixin;

import de.sunclient.SunConfig;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Überschreibt die Player-Renderer-Entscheidung, damit der eigene Nametag in Third Person sichtbar bleibt. */
@Mixin(value = class_1007.class, remap = false)
public abstract class NametagMixin {
	@Inject(method = "method_74935", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
	private void sunclient$showOwnNametag(class_11890 entity, double squaredDistanceToCamera,
		CallbackInfoReturnable<Boolean> cir) {
		class_310 mc = class_310.method_1551();
		if (SunConfig.INSTANCE.showOwnNametag
			&& mc.field_1724 != null
			&& entity == mc.field_1724
			&& mc.field_1690.method_31044().ordinal() != 0) {
			cir.setReturnValue(true);
		}
	}
}
