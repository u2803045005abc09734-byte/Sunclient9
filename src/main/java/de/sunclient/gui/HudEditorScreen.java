package de.sunclient.gui;

import de.sunclient.SunConfig;
import de.sunclient.hud.HudManager;
import de.sunclient.hud.HudModule;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;

/** HUD-Editor: HUD-Elemente anklicken, ziehen und einzeln skalieren. */
public class HudEditorScreen extends SunScreen {
	private HudModule dragging = null;
	private HudModule selected = null;
	private int offX = 0;
	private int offY = 0;
	private boolean wasDown = false;

	public HudEditorScreen(class_437 parent) {
		super("HUD bearbeiten", parent);
	}

	private static boolean isMouseDown() {
		long window = GLFW.glfwGetCurrentContext();
		return window != 0L && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
	}

	private void changeSelectedScale(float amount) {
		if (selected == null) return;
		selected.changeScale(amount);
		SunConfig.save();
	}

	@Override
	protected void method_25426() {
		int center = this.field_22789 / 2;
		int y = this.field_22790 - 28;
		addButton("− Größe", center - 150, y, 72, () -> changeSelectedScale(-0.1f));
		addButton("+ Größe", center - 74, y, 72, () -> changeSelectedScale(0.1f));
		addButton("Reset Pos.", center + 2, y, 72, () -> {
			for (HudModule m : HudManager.MODULES) m.resetPosition();
			SunConfig.save();
		});
		addButton("Fertig", center + 78, y, 72, this::method_25419);
		// Maustaste kann noch vom Klick auf den Menü-Button gedrückt sein.
		wasDown = isMouseDown();
		dragging = null;
	}

	@Override
	public void method_25394(net.minecraft.class_332 g, int mouseX, int mouseY, float delta) {
		super.method_25394(g, mouseX, mouseY, delta);
		class_310 mc = mc();
		g.method_27534(this.field_22793,
			class_2561.method_43470("Element anklicken und ziehen · Größe unten anpassen"),
			this.field_22789 / 2, 30, 0xFFBBBBBB);
		String selectedLabel = selected == null ? "Ausgewählt: keines" :
			"Ausgewählt: " + selected.name + " · Größe: " + String.format(java.util.Locale.ROOT, "%.1f×", selected.scale());
		g.method_27534(this.field_22793, class_2561.method_43470(selectedLabel), this.field_22789 / 2, 42,
			selected == null ? 0xFF888888 : 0xFFFFC107);

		boolean down = isMouseDown();
		boolean overButtons = mouseY > this.field_22790 - 34;
		HudManager.editing = true;
		try {
			if (down && !wasDown && !overButtons) {
				for (int i = HudManager.MODULES.size() - 1; i >= 0; i--) {
					HudModule m = HudManager.MODULES.get(i);
					if (!m.isEnabled()) continue;
					int[] p = HudManager.position(m, mc);
					if (mouseX >= p[0] && mouseX < p[0] + m.displayWidth(mc)
						&& mouseY >= p[1] && mouseY < p[1] + m.displayHeight(mc)) {
						selected = m;
						dragging = m;
						offX = mouseX - p[0];
						offY = mouseY - p[1];
						break;
					}
				}
			}

			if (!down && dragging != null) {
				dragging = null;
				SunConfig.save();
			}

			if (dragging != null) {
				int sw = mc.method_22683().method_4486();
				int sh = mc.method_22683().method_4502();
				SunConfig.ModuleState s = dragging.state();
				s.x = Math.max(0, Math.min(mouseX - offX, Math.max(0, sw - dragging.displayWidth(mc))));
				s.y = Math.max(0, Math.min(mouseY - offY, Math.max(0, sh - dragging.displayHeight(mc))));
			}

			for (HudModule m : HudManager.MODULES) {
				if (!m.isEnabled()) continue;
				int[] p = HudManager.position(m, mc);
				int w = m.displayWidth(mc);
				int h = m.displayHeight(mc);
				HudManager.drawAt(g, mc, m, p[0], p[1]);
				HudManager.outline(g, p[0], p[1], w, h,
					m == selected ? 0xFFFFC107 : 0x88FFFFFF);
				g.method_51433(mc.field_1772, m.name, p[0], Math.max(0, p[1] - 10),
					m == selected ? 0xFFFFC107 : 0xFFAAAAAA, false);
			}
		} finally {
			HudManager.editing = false;
		}
		wasDown = down;
	}

	@Override
	public void method_25419() {
		HudManager.editing = false;
		SunConfig.save();
		super.method_25419();
	}
}
