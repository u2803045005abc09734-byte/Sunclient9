package de.sunclient.hud;

import de.sunclient.SunConfig;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** HUD-Modul mit einer Textzeile. */
public abstract class TextModule extends HudModule {
	private static final int PAD = 2;

	protected TextModule(String id, String name, int defX, int defY) {
		super(id, name, defX, defY);
	}

	/** Anzuzeigender Text oder null, wenn gerade nichts angezeigt werden soll. */
	protected abstract String text(class_310 mc);

	private String safeText(class_310 mc) {
		String t = text(mc);
		return t == null ? "" : t;
	}

	@Override
	public boolean hasContent(class_310 mc) {
		return text(mc) != null;
	}

	@Override
	public int width(class_310 mc) {
		return mc.field_1772.method_1727(safeText(mc)) + 2 * PAD;
	}

	@Override
	public int height(class_310 mc) {
		return 9 + 2 * PAD;
	}

	@Override
	public void draw(class_332 g, class_310 mc, int x, int y) {
		if (SunConfig.INSTANCE.hudBackground) {
			g.method_25294(x, y, x + width(mc), y + height(mc), 0x66000000);
		}
		g.method_51433(mc.field_1772, safeText(mc), x + PAD, y + PAD, 0xFFFFFFFF, true);
	}
}
