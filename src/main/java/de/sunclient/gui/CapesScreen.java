package de.sunclient.gui;

import de.sunclient.SunConfig;
import de.sunclient.cape.CapeManager;
import net.minecraft.class_2561;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Sun-Capes-Menü: PNG auswählen und optional mit Freunden über den eigenen Cape-Server teilen. */
public class CapesScreen extends SunScreen {
	private class_342 serverField;
	private class_342 keyField;
	private class_4185 syncToggle;

	public CapesScreen(class_437 parent) {
		super("Custom Capes", parent);
	}

	@Override
	protected void method_25426() {
		int cx = this.field_22789 / 2;
		int w = Math.min(330, this.field_22789 - 24);
		int left = cx - w / 2;
		int y = 67;

		addButton("PNG-Cape auswählen", left, y, w, CapeManager::chooseCape);

		serverField = new class_342(this.field_22793, left, y + 59, w, 20,
			class_2561.method_43470("https://dein-cape-server.example"));
		serverField.method_1852(SunConfig.INSTANCE.capeServerUrl == null ? "" : SunConfig.INSTANCE.capeServerUrl);
		serverField.method_1890(s -> s.length() <= 240);
		method_37063(serverField);

		keyField = new class_342(this.field_22793, left, y + 105, w, 20,
			class_2561.method_43470("Gemeinsamer Freunde-Schlüssel"));
		keyField.method_1852(SunConfig.INSTANCE.capeServerKey == null ? "" : SunConfig.INSTANCE.capeServerKey);
		keyField.method_1890(s -> s.length() <= 180);
		method_37063(keyField);

		addButton("Server-Einstellungen speichern", left, y + 132, w, this::saveSettings);
		syncToggle = addButton(syncLabel(), left, y + 158, w, this::toggleSync);
		addButton("Cape jetzt hochladen", left, y + 184, w, () -> {
			saveSettings();
			CapeManager.syncOwnCape();
		});
		addButton("Cape-Ordner öffnen", left, y + 210, w, CapeManager::openCapeFolder);
		addButton("Cape entfernen", left, y + 236, w, CapeManager::removeCape);
		addButton("Zurück", left, y + 266, w, this::method_25419);
	}

	private String syncLabel() {
		return "Freunde-Sync: " + (SunConfig.INSTANCE.capeSyncEnabled ? "AN" : "AUS");
	}

	private void saveSettings() {
		if (serverField != null) SunConfig.INSTANCE.capeServerUrl = serverField.method_1882().trim();
		if (keyField != null) SunConfig.INSTANCE.capeServerKey = keyField.method_1882().trim();
		while (SunConfig.INSTANCE.capeServerUrl.endsWith("/")) {
			SunConfig.INSTANCE.capeServerUrl = SunConfig.INSTANCE.capeServerUrl.substring(0, SunConfig.INSTANCE.capeServerUrl.length() - 1);
		}
		SunConfig.save();
	}

	private void toggleSync() {
		saveSettings();
		SunConfig.INSTANCE.capeSyncEnabled = !SunConfig.INSTANCE.capeSyncEnabled;
		SunConfig.save();
		syncToggle.method_25355(class_2561.method_43470(syncLabel()));
		if (SunConfig.INSTANCE.capeSyncEnabled) CapeManager.syncOwnCape();
	}

	@Override
	public void method_25394(net.minecraft.class_332 g, int mouseX, int mouseY, float delta) {
		int center = this.field_22789 / 2;
		int left = center - Math.min(165, (this.field_22789 - 24) / 2);
		int right = center + Math.min(165, (this.field_22789 - 24) / 2);
		g.method_25294(left, 37, right, Math.min(this.field_22790 - 14, 365), 0xE50B111B);
		g.method_25294(left, 37, right, 39, 0xFFFFA33B);
		g.method_27534(this.field_22793, class_2561.method_43470("PNG · 64 × 32 Pixel · max. 128 KiB"), center, 54, 0xFFBFC8D7);
		super.method_25394(g, mouseX, mouseY, delta);
		g.method_27534(this.field_22793, class_2561.method_43470("Cape-Server-URL (für Freunde-Sync)"), center, 116, 0xFFFFFFFF);
		g.method_27534(this.field_22793, class_2561.method_43470("Gemeinsamer Schlüssel"), center, 162, 0xFFFFFFFF);
		g.method_27534(this.field_22793, class_2561.method_43470(CapeManager.status()), center,
			Math.min(this.field_22790 - 27, 353), 0xFFFFC16D);
		g.method_27534(this.field_22793, class_2561.method_43470("Synchronisierte Capes sehen nur Sun-Client-Spieler mit demselben Server/Schlüssel."),
			center, Math.min(this.field_22790 - 15, 376), 0xFFBBC4D2);
	}
}
