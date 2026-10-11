package de.sunclient.mixin;

import de.sunclient.Stats;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Zählt jeden echten Frame (für genaue FPS und CPS), unabhängig vom HUD-Rendering. */
@Mixin(class_310.class)
public class MinecraftMixin {
	@Inject(method = "method_1523", at = @At("HEAD"), require = 0)
	private void sunclient$onFrame(boolean tick, CallbackInfo ci) {
		Stats.onFrame();
	}
}
