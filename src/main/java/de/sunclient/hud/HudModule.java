package de.sunclient.hud;

import de.sunclient.SunConfig;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** Ein verschiebbares, skalierbares HUD-Element. */
public abstract class HudModule {
	public final String id;
	public final String name;
	private final int defX;
	private final int defY;

	protected HudModule(String id, String name, int defX, int defY) {
		this.id = id;
		this.name = name;
		this.defX = defX;
		this.defY = defY;
	}

	public SunConfig.ModuleState state() {
		return SunConfig.INSTANCE.state(id, defX, defY);
	}

	public float scale() {
		return Math.max(0.5f, Math.min(3.0f, state().scale));
	}

	public void changeScale(float amount) {
		SunConfig.ModuleState s = state();
		s.scale = Math.max(0.5f, Math.min(3.0f, Math.round((scale() + amount) * 10.0f) / 10.0f));
	}

	public boolean isEnabled() {
		return state().enabled;
	}

	public void setEnabled(boolean enabled) {
		state().enabled = enabled;
	}

	public void resetPosition() {
		SunConfig.ModuleState s = state();
		s.x = defX;
		s.y = defY;
	}

	public int displayWidth(class_310 mc) {
		return Math.max(1, (int) Math.ceil(width(mc) * scale()));
	}

	public int displayHeight(class_310 mc) {
		return Math.max(1, (int) Math.ceil(height(mc) * scale()));
	}

	/** false = gerade nichts anzuzeigen (z. B. Sprint-Status). Im Editor gibt es eine Vorschau. */
	public boolean hasContent(class_310 mc) {
		return true;
	}

	public abstract int width(class_310 mc);

	public abstract int height(class_310 mc);

	public abstract void draw(class_332 g, class_310 mc, int x, int y);
}
