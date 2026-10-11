package de.sunclient.gui;

import de.sunclient.SunConfig;
import de.sunclient.hud.HudManager;
import de.sunclient.hud.HudModule;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** HUD-Module einschalten und Einstellungen ändern. */
public class ModulesScreen extends SunScreen {
	public ModulesScreen(class_437 parent) {
		super("HUD-Module", parent);
	}

	private static String armorLabel() {
		return "Rüstung: " + (SunConfig.INSTANCE.armorHorizontal ? "Horizontal" : "Vertikal");
	}

	@Override
	protected void method_25426() {
		int left = this.field_22789 / 2 - 155;
		int right = this.field_22789 / 2 + 5;
		int top = 40;
		int step = 24;

		int i = 0;
		for (HudModule m : HudManager.MODULES) {
			int x = (i % 2 == 0) ? left : right;
			int y = top + (i / 2) * step;
			toggle(x, y, 150, m.name, m::isEnabled, m::setEnabled);
			i++;
		}

		int rows = (HudManager.MODULES.size() + 1) / 2;
		int by = top + rows * step + 8;
		class_4185[] armor = new class_4185[1];
		armor[0] = addButton(armorLabel(), this.field_22789 / 2 - 115, by, 230, () -> {
			SunConfig.INSTANCE.armorHorizontal = !SunConfig.INSTANCE.armorHorizontal;
			armor[0].method_25355(class_2561.method_43470(armorLabel()));
			SunConfig.save();
		});
		addButton("Zurück", this.field_22789 / 2 - 100, by + 26, 200, this::method_25419);
	}
}
