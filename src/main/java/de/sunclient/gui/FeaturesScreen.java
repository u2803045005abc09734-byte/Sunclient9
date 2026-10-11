package de.sunclient.gui;

import de.sunclient.SunConfig;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Funktionen: Zoom, Toggle Sprint, Freelook, Ping-Modus, HUD-Hintergrund. */
public class FeaturesScreen extends SunScreen {
	private static final int[] FOV_STEPS = {30, 40, 50, 60};

	public FeaturesScreen(class_437 parent) {
		super("Funktionen", parent);
	}

	private static String pingLabel() {
		return SunConfig.INSTANCE.pingMeasure ? "Ping: Messung (TCP)" : "Ping: Server (Tab-Liste)";
	}

	private static String fovLabel() {
		return "Zoom-FOV: " + SunConfig.INSTANCE.zoomFov;
	}

	@Override
	protected void method_25426() {
		SunConfig c = SunConfig.INSTANCE;
		int left = this.field_22789 / 2 - 155;
		int right = this.field_22789 / 2 + 5;
		int top = 44;
		int step = 24;

		toggle(left, top, 150, "Toggle Sprint", () -> c.toggleSprint, v -> c.toggleSprint = v);
		toggle(right, top, 150, "Zoom", () -> c.zoomEnabled, v -> c.zoomEnabled = v);
		toggle(left, top + step, 150, "Freelook", () -> c.freelookEnabled, v -> c.freelookEnabled = v);
		toggle(right, top + step, 150, "HUD-Hintergrund", () -> c.hudBackground, v -> c.hudBackground = v);
		toggle(left, top + 2 * step, 150, "Eigener Nametag (3. Person)", () -> c.showOwnNametag, v -> c.showOwnNametag = v);

		class_4185[] ping = new class_4185[1];
		ping[0] = addButton(pingLabel(), left, top + 3 * step, 150, () -> {
			c.pingMeasure = !c.pingMeasure;
			ping[0].method_25355(class_2561.method_43470(pingLabel()));
			SunConfig.save();
		});

		class_4185[] fov = new class_4185[1];
		fov[0] = addButton(fovLabel(), right, top + 3 * step, 150, () -> {
			int next = FOV_STEPS[0];
			for (int i = 0; i < FOV_STEPS.length; i++) {
				if (FOV_STEPS[i] == c.zoomFov) {
					next = FOV_STEPS[(i + 1) % FOV_STEPS.length];
					break;
				}
			}
			c.zoomFov = next;
			fov[0].method_25355(class_2561.method_43470(fovLabel()));
			SunConfig.save();
		});

		addButton("Zurück", this.field_22789 / 2 - 100, top + 4 * step + 12, 200, this::method_25419);
	}
}
