package de.sunclient.hud;

import de.sunclient.Stats;
import de.sunclient.SunConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_310;
import net.minecraft.class_332;

public final class HudManager {
	private HudManager() {}

	/** true nur während des HUD-Editors: Module zeigen Beispielinhalte. */
	public static boolean editing = false;
	public static final List<HudModule> MODULES = new ArrayList<>();

	static {
		MODULES.add(new TextModule("fps", "FPS", 4, 4) {
			@Override protected String text(class_310 mc) { return "FPS: " + Stats.fps(); }
		});
		MODULES.add(new TextModule("cps", "CPS", 4, 17) {
			@Override protected String text(class_310 mc) { return "CPS: " + Stats.cpsLeft() + " | " + Stats.cpsRight(); }
		});
		MODULES.add(new TextModule("ping", "Ping", 4, 30) {
			@Override protected String text(class_310 mc) {
				int ping = Stats.ping(mc);
				return ping < 0 ? "Ping: -" : "Ping: " + ping + " ms";
			}
		});
		MODULES.add(new TextModule("coords", "Koordinaten", 4, 43) {
			@Override protected String text(class_310 mc) {
				if (mc.field_1724 == null) return "XYZ: 0.0 / 64.0 / 0.0";
				return String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f",
					mc.field_1724.method_23317(), mc.field_1724.method_23318(), mc.field_1724.method_23321());
			}
		});
		MODULES.add(new TextModule("direction", "Richtung", 4, 56) {
			@Override protected String text(class_310 mc) {
				if (mc.field_1724 == null) return "Richtung: North";
				String dir = mc.field_1724.method_5735().method_10151();
				return "Richtung: " + dir.substring(0, 1).toUpperCase(Locale.ROOT) + dir.substring(1);
			}
		});
		MODULES.add(new TextModule("sprint", "Sprint-Status", 4, 69) {
			@Override protected String text(class_310 mc) {
				if (editing || SunConfig.INSTANCE.toggleSprint) return "[Sprint: Aktiviert]";
				if (mc.field_1690.field_1867.method_1434()) return "[Sprint: Gehalten]";
				return null;
			}
		});
		MODULES.add(new KeystrokesModule());
		MODULES.add(new ArmorModule());
	}

	/** Position eines Moduls, einschließlich seiner Skalierung, auf dem Bildschirm begrenzt. */
	public static int[] position(HudModule m, class_310 mc) {
		SunConfig.ModuleState s = m.state();
		int sw = mc.method_22683().method_4486();
		int sh = mc.method_22683().method_4502();
		int x = Math.max(0, Math.min(s.x, Math.max(0, sw - m.displayWidth(mc))));
		int y = Math.max(0, Math.min(s.y, Math.max(0, sh - m.displayHeight(mc))));
		return new int[] {x, y};
	}

	/** Zeichnet jedes Modul innerhalb seiner eigenen Transformationsmatrix. */
	public static void drawAt(class_332 g, class_310 mc, HudModule m, int x, int y) {
		var matrices = g.method_51448();
		matrices.pushMatrix();
		matrices.translate((float) x, (float) y);
		float scale = m.scale();
		matrices.scale(scale, scale);
		m.draw(g, mc, 0, 0);
		matrices.popMatrix();
	}

	public static void renderAll(class_332 g, class_310 mc) {
		for (HudModule m : MODULES) {
			if (!m.isEnabled() || !m.hasContent(mc)) continue;
			int[] p = position(m, mc);
			drawAt(g, mc, m, p[0], p[1]);
		}
	}

	/** Rahmen aus vier dünnen Rechtecken. */
	public static void outline(class_332 g, int x, int y, int w, int h, int color) {
		g.method_25294(x - 1, y - 1, x + w + 1, y, color);
		g.method_25294(x - 1, y + h, x + w + 1, y + h + 1, color);
		g.method_25294(x - 1, y, x, y + h, color);
		g.method_25294(x + w, y, x + w + 1, y + h, color);
	}
}
