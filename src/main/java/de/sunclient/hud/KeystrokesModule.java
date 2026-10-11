package de.sunclient.hud;

import de.sunclient.Stats;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** WASD, Maustasten (mit CPS) und Leertaste. */
public class KeystrokesModule extends HudModule {
	private static final int S = 20;
	private static final int GAP = 2;

	public KeystrokesModule() {
		super("keystrokes", "Keystrokes", 4, 90);
	}

	@Override
	public int width(class_310 mc) {
		return 3 * S + 2 * GAP;
	}

	@Override
	public int height(class_310 mc) {
		return 3 * S + 3 * GAP + 12;
	}

	private static void box(class_332 g, class_310 mc, int x, int y, int w, int h, String label, boolean down) {
		g.method_25294(x, y, x + w, y + h, down ? 0xAAFFFFFF : 0x66000000);
		int tw = mc.field_1772.method_1727(label);
		g.method_51433(mc.field_1772, label, x + (w - tw) / 2, y + (h - 8) / 2, down ? 0xFF000000 : 0xFFFFFFFF, false);
	}

	@Override
	public void draw(class_332 g, class_310 mc, int x, int y) {
		box(g, mc, x + S + GAP, y, S, S, "W", mc.field_1690.field_1894.method_1434());
		box(g, mc, x, y + S + GAP, S, S, "A", mc.field_1690.field_1913.method_1434());
		box(g, mc, x + S + GAP, y + S + GAP, S, S, "S", mc.field_1690.field_1881.method_1434());
		box(g, mc, x + 2 * (S + GAP), y + S + GAP, S, S, "D", mc.field_1690.field_1849.method_1434());

		int mouseY = y + 2 * (S + GAP);
		int mw = (width(mc) - GAP) / 2;
		box(g, mc, x, mouseY, mw, S, "LMB " + Stats.cpsLeft(), Stats.leftDown());
		box(g, mc, x + mw + GAP, mouseY, mw, S, "RMB " + Stats.cpsRight(), Stats.rightDown());

		box(g, mc, x, mouseY + S + GAP, width(mc), 12, "-----", mc.field_1690.field_1903.method_1434());
	}
}
