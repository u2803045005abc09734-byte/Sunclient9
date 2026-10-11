package de.sunclient;

import de.sunclient.gui.SunMenuScreen;
import de.sunclient.hud.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;

public class SunClient implements ClientModInitializer {
	public static class_304 menuKey;
	public static class_304 zoomKey;
	public static class_304 sprintToggleKey;
	public static class_304 freelookKey;

	/** FOV vor dem Zoomen, null = nicht am Zoomen. */
	private static Integer savedFov = null;

	@Override
	public void onInitializeClient() {
		SunConfig.load();

		menuKey = KeyBindingHelper.registerKeyBinding(new class_304(
			"key.sunclient.menu", class_3675.class_307.field_1668, GLFW.GLFW_KEY_RIGHT_SHIFT, class_304.class_11900.field_62556));
		zoomKey = KeyBindingHelper.registerKeyBinding(new class_304(
			"key.sunclient.zoom", class_3675.class_307.field_1668, GLFW.GLFW_KEY_C, class_304.class_11900.field_62556));
		sprintToggleKey = KeyBindingHelper.registerKeyBinding(new class_304(
			"key.sunclient.togglesprint", class_3675.class_307.field_1668, GLFW.GLFW_KEY_R, class_304.class_11900.field_62556));
		freelookKey = KeyBindingHelper.registerKeyBinding(new class_304(
			"key.sunclient.freelook", class_3675.class_307.field_1668, GLFW.GLFW_KEY_LEFT_ALT, class_304.class_11900.field_62556));

		HudRenderer.register();
		ClientTickEvents.END_CLIENT_TICK.register(SunClient::onTick);
	}

	private static void onTick(class_310 mc) {
		SunConfig cfg = SunConfig.INSTANCE;

		while (menuKey.method_1436()) {
			if (mc.field_1755 == null) mc.method_1507(new SunMenuScreen(null));
		}
		while (sprintToggleKey.method_1436()) {
			cfg.toggleSprint = !cfg.toggleSprint;
			SunConfig.save();
		}

		if (mc.field_1724 == null) {
			savedFov = null;
			Freelook.active = false;
			return;
		}

		if (cfg.pingMeasure) Stats.ensureTcpThread();

		// Toggle Sprint
		if (cfg.toggleSprint && mc.field_1755 == null && mc.field_1690.field_1894.method_1434()) {
			mc.field_1690.field_1867.method_23481(true);
		}

		// Zoom
		boolean zooming = cfg.zoomEnabled && zoomKey.method_1434() && mc.field_1755 == null;
		if (zooming) {
			if (savedFov == null) savedFov = mc.field_1690.method_41808().method_41753();
			mc.field_1690.method_41808().method_41748(Math.max(30, Math.min(110, cfg.zoomFov)));
		} else if (savedFov != null) {
			mc.field_1690.method_41808().method_41748(savedFov);
			savedFov = null;
		}

		// Freelook (Taste halten)
		boolean look = cfg.freelookEnabled && freelookKey.method_1434() && mc.field_1755 == null;
		if (look) {
			Freelook.begin(mc);
		} else {
			Freelook.end(mc);
		}
	}
}
