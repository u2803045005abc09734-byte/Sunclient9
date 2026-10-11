package de.sunclient.hud;

import de.sunclient.Stats;
import de.sunclient.gui.HudEditorScreen;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_9779;

public final class HudRenderer {
	private HudRenderer() {}

	public static void register() {
		HudRenderCallback.EVENT.register(HudRenderer::render);
	}

	private static void render(class_332 g, class_9779 delta) {
		Stats.onHudFrame();
		class_310 mc = class_310.method_1551();
		if (mc.field_1724 == null || mc.field_1690.field_1842) return;
		// Im HUD-Editor zeichnet der Editor die Module selbst
		if (mc.field_1755 instanceof HudEditorScreen) return;
		HudManager.renderAll(g, mc);
	}
}
