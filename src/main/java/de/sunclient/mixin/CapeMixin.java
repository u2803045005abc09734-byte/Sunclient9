package de.sunclient.mixin;

import de.sunclient.cape.CapeManager;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fügt das ausgewählte/geladene Cape in den Player-Renderstate ein, bevor Minecraft es rendert. */
@Mixin(value = class_1007.class, remap = false)
public abstract class CapeMixin {
	@Inject(method = "method_62609", at = @At("HEAD"), remap = false, require = 0)
	private void sunclient$applyCustomCape(class_11890 player, class_10055 state, float tickProgress, CallbackInfo ci) {
		CapeManager.applyCape(player, state);
	}
}
