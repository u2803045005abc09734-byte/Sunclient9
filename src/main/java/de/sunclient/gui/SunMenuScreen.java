package de.sunclient.gui;

import de.sunclient.SunConfig;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/** Custom Sun-Client-Menü im dunklen/amberfarbenen Stil; geöffnet mit Rechts-Shift. */
public class SunMenuScreen extends SunScreen {
	public SunMenuScreen(class_437 parent) {
		super("Sun Client", parent);
	}

	@Override
	protected void method_25426() {
		int cx = this.field_22789 / 2;
		int buttonWidth = Math.min(250, this.field_22789 - 36);
		int x = cx - buttonWidth / 2;
		int y = Math.max(92, Math.min(138, this.field_22790 / 2 - 88));
		int step = 27;
		addButton("HUD bearbeiten", x, y, buttonWidth, () -> mc().method_1507(new HudEditorScreen(this)));
		addButton("HUD & Rüstung", x, y + step, buttonWidth, () -> mc().method_1507(new ModulesScreen(this)));
		addButton("Funktionen", x, y + step * 2, buttonWidth, () -> mc().method_1507(new FeaturesScreen(this)));
		addButton("Modrinth Resource Packs", x, y + step * 3, buttonWidth, () -> mc().method_1507(new PackBrowserScreen(this)));
		addButton("Custom Capes", x, y + step * 4, buttonWidth, () -> mc().method_1507(new CapesScreen(this)));
		addButton("Schließen", x, y + step * 5 + 5, buttonWidth, this::method_25419);
	}

	@Override
	public void method_25394(class_332 g, int mouseX, int mouseY, float delta) {
		int cx = this.field_22789 / 2;
		int panelWidth = Math.min(390, this.field_22789 - 18);
		int left = cx - panelWidth / 2;
		int right = cx + panelWidth / 2;
		g.method_25294(0, 0, this.field_22789, this.field_22790, 0x7904080F);
		g.method_25294(left, 34, right, Math.min(this.field_22790 - 22, 405), 0xE90D121C);
		g.method_25294(left, 34, right, 36, 0xFFFFA33B);
		g.method_25294(left, 34, left + 3, Math.min(this.field_22790 - 22, 405), 0xFFFFA33B);
		g.method_27534(this.field_22793, class_2561.method_43470("☀  SUN CLIENT"), cx, 48, 0xFFFFB858);
		g.method_27534(this.field_22793, class_2561.method_43470("CUSTOMIZE YOUR GAME"), cx, 66, 0xFFC6CEDA);
		super.method_25394(g, mouseX, mouseY, delta);
		g.method_27534(this.field_22793,
			class_2561.method_43470("Minecraft 1.21.11  ·  Rechts-Shift  ·  HUD " + (SunConfig.INSTANCE.hudBackground ? "ON" : "OFF")),
			cx, this.field_22790 - 34, 0xFFB7C2D1);
	}
}
