package de.sunclient.cape;

import de.sunclient.SunConfig;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_10055;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_11890;
import net.minecraft.class_12079;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_8685;

/** Locales Cape-Management mit optionaler Synchronisierung über einen selbst gehosteten Freunde-Server. */
public final class CapeManager {
	private static final HttpClient HTTP = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();
	private static final Path CAPE_DIR = FabricLoader.getInstance().getConfigDir().resolve("sunclient/capes");
	private static final Path SELECTED_FILE = CAPE_DIR.resolve("selected.png");
	private static final class_2960 LOCAL_TEXTURE_ID = class_2960.method_60655("sunclient", "capes/local");
	private static final Map<UUID, class_2960> REMOTE_TEXTURES = new ConcurrentHashMap<>();
	private static final Map<UUID, byte[]> PENDING_TEXTURES = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> REQUESTED_AT = new ConcurrentHashMap<>();
	private static volatile class_2960 localTexture;
	private static volatile String status = "Wähle ein Cape-PNG aus.";

	private CapeManager() {}

	public static Path capeDirectory() { return CAPE_DIR; }
	public static String status() { return status; }
	public static boolean hasLocalCape() { return Files.isRegularFile(SELECTED_FILE); }

	public static void chooseCape() {
		try {
			Files.createDirectories(CAPE_DIR);
			FileDialog dialog = new FileDialog((Frame) null, "Sun Client – PNG-Cape auswählen", FileDialog.LOAD);
			dialog.setFilenameFilter((dir, name) -> name != null && name.toLowerCase(Locale.ROOT).endsWith(".png"));
			dialog.setVisible(true);
			if (dialog.getFile() == null || dialog.getDirectory() == null) {
				status = "Auswahl abgebrochen.";
				return;
			}
			Path selected = Path.of(dialog.getDirectory(), dialog.getFile());
			byte[] bytes = Files.readAllBytes(selected);
			validateCape(bytes);
			Files.copy(selected, SELECTED_FILE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			localTexture = null;
			loadLocalTexture();
			status = "Cape geladen: " + selected.getFileName();
		} catch (Exception e) {
			status = "Cape-Fehler: " + safeMessage(e);
		}
	}

	public static void openCapeFolder() {
		try {
			Files.createDirectories(CAPE_DIR);
			if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(CAPE_DIR.toFile());
			else status = "Cape-Ordner: " + CAPE_DIR;
		} catch (Exception e) {
			status = "Ordner konnte nicht geöffnet werden: " + safeMessage(e);
		}
	}

	public static void removeCape() {
		try {
			Files.deleteIfExists(SELECTED_FILE);
			localTexture = null;
			class_310 mc = class_310.method_1551();
			if (mc.method_1531() != null) mc.method_1531().method_4615(LOCAL_TEXTURE_ID);
			status = "Lokales Cape entfernt.";
			syncRemoveOwnCape();
		} catch (Exception e) {
			status = "Cape konnte nicht entfernt werden: " + safeMessage(e);
		}
	}

	/** Auf dem Render-Thread: synchronisierte Texture übernehmen und das Cape im Player-Renderstate setzen. */
	public static void applyCape(class_11890 player, class_10055 state) {
		if (player == null || state == null || !SunConfig.INSTANCE.capeEnabled) return;
		class_310 mc = class_310.method_1551();
		UUID uuid = player.method_5667();
		class_2960 texture = null;

		if (mc.field_1724 != null && uuid.equals(mc.field_1724.method_5667())) {
			loadLocalTexture();
			texture = localTexture;
		} else if (SunConfig.INSTANCE.capeSyncEnabled) {
			byte[] pending = PENDING_TEXTURES.remove(uuid);
			if (pending != null) {
				try {
					texture = registerTexture(mc, textureId(uuid), pending);
					REMOTE_TEXTURES.put(uuid, texture);
				} catch (Exception e) {
					status = "Remote-Cape ungültig: " + safeMessage(e);
				}
			}
			if (texture == null) texture = REMOTE_TEXTURES.get(uuid);
			if (texture == null) requestRemoteCape(uuid);
		}

		if (texture != null && state.field_53520 != null) {
			class_12079.class_10726 cape = new class_12079.class_10726(texture);
			class_8685.class_11892 override = new class_8685.class_11892(
				Optional.empty(), Optional.of(cape), Optional.empty(), Optional.empty());
			state.field_53520 = state.field_53520.method_74883(override);
		}
	}

	private static void loadLocalTexture() {
		if (localTexture != null || !SunConfig.INSTANCE.capeEnabled || !Files.isRegularFile(SELECTED_FILE)) return;
		try {
			class_310 mc = class_310.method_1551();
			localTexture = registerTexture(mc, LOCAL_TEXTURE_ID, Files.readAllBytes(SELECTED_FILE));
			status = "Lokales Cape ist aktiv.";
		} catch (Exception e) {
			status = "Cape konnte nicht geladen werden: " + safeMessage(e);
		}
	}

	private static class_2960 registerTexture(class_310 mc, class_2960 id, byte[] bytes) throws IOException {
		validateCape(bytes);
		class_1011 image = class_1011.method_49277(bytes);
		class_1043 texture = new class_1043(() -> "Sun Client Custom Cape", image);
		mc.method_1531().method_4616(id, texture);
		return id;
	}

	private static class_2960 textureId(UUID uuid) {
		return class_2960.method_60655("sunclient", "capes/" + uuid.toString().replace("-", ""));
	}

	/** Standard-Minecraft-Cape-Atlas: 64x32 PNG, maximal 128 KiB. */
	private static void validateCape(byte[] bytes) throws IOException {
		if (bytes == null || bytes.length < 24 || bytes.length > 128 * 1024) {
			throw new IOException("PNG muss zwischen 24 Bytes und 128 KiB groß sein.");
		}
		byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		for (int i = 0; i < pngSignature.length; i++) {
			if (bytes[i] != pngSignature[i]) throw new IOException("Die Datei ist kein PNG.");
		}
		try (class_1011 img = class_1011.method_49277(bytes)) {
			if (img.method_4307() != 64 || img.method_4323() != 32) {
				throw new IOException("Cape muss genau 64 x 32 Pixel groß sein.");
			}
		}
	}

	public static void syncOwnCape() {
		if (!SunConfig.INSTANCE.capeSyncEnabled) {
			status = "Freunde-Sync ist ausgeschaltet.";
			return;
		}
		String base = baseUrl();
		String key = SunConfig.INSTANCE.capeServerKey == null ? "" : SunConfig.INSTANCE.capeServerKey.trim();
		class_310 mc = class_310.method_1551();
		if (base.isEmpty() || key.isEmpty() || mc.field_1724 == null) {
			status = "Server-URL, Gruppenschlüssel und ein eingeloggter Spieler werden benötigt.";
			return;
		}
		if (!Files.isRegularFile(SELECTED_FILE)) {
			status = "Wähle zuerst ein Cape-PNG aus.";
			return;
		}
		try {
			byte[] bytes = Files.readAllBytes(SELECTED_FILE);
			validateCape(bytes);
			String uuid = mc.field_1724.method_5667().toString();
			HttpRequest req = HttpRequest.newBuilder(URI.create(base + "/api/capes/" + uuid + ".png"))
				.timeout(Duration.ofSeconds(10)).header("X-SunClient-Key", key)
				.header("Content-Type", "image/png").PUT(HttpRequest.BodyPublishers.ofByteArray(bytes)).build();
			status = "Cape wird hochgeladen …";
			HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenAccept(res -> {
				status = res.statusCode() >= 200 && res.statusCode() < 300
					? "Cape wurde mit dem Freunde-Server synchronisiert."
					: "Upload fehlgeschlagen (HTTP " + res.statusCode() + ").";
			}).exceptionally(ex -> { status = "Server nicht erreichbar: " + safeMessage(ex); return null; });
		} catch (Exception e) {
			status = "Upload fehlgeschlagen: " + safeMessage(e);
		}
	}

	private static void syncRemoveOwnCape() {
		if (!SunConfig.INSTANCE.capeSyncEnabled || baseUrl().isEmpty() || SunConfig.INSTANCE.capeServerKey == null
			|| SunConfig.INSTANCE.capeServerKey.isBlank()) return;
		class_310 mc = class_310.method_1551();
		if (mc.field_1724 == null) return;
		String uuid = mc.field_1724.method_5667().toString();
		try {
			HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl() + "/api/capes/" + uuid + ".png"))
				.timeout(Duration.ofSeconds(8)).header("X-SunClient-Key", SunConfig.INSTANCE.capeServerKey.trim())
				.DELETE().build();
			HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding());
		} catch (Exception ignored) {}
	}

	private static void requestRemoteCape(UUID uuid) {
		if (!SunConfig.INSTANCE.capeSyncEnabled || baseUrl().isEmpty() || SunConfig.INSTANCE.capeServerKey == null
			|| SunConfig.INSTANCE.capeServerKey.isBlank()) return;
		long now = System.currentTimeMillis();
		Long previous = REQUESTED_AT.putIfAbsent(uuid, now);
		if (previous != null && (now - previous < 5 * 60_000L || !REQUESTED_AT.replace(uuid, previous, now))) return;
		try {
			HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl() + "/api/capes/" + uuid + ".png"))
				.timeout(Duration.ofSeconds(8)).header("X-SunClient-Key", SunConfig.INSTANCE.capeServerKey.trim()).GET().build();
			HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray()).thenAccept(res -> {
				if (res.statusCode() == 200 && res.body().length <= 128 * 1024) {
					try { validateCape(res.body()); PENDING_TEXTURES.put(uuid, res.body()); }
					catch (Exception ignored) {}
				} else if (res.statusCode() == 401) {
					status = "Cape-Server: Gruppenschlüssel stimmt nicht.";
				}
			}).exceptionally(ex -> null);
		} catch (Exception ignored) {}
	}

	private static String baseUrl() {
		String raw = SunConfig.INSTANCE.capeServerUrl == null ? "" : SunConfig.INSTANCE.capeServerUrl.trim();
		while (raw.endsWith("/")) raw = raw.substring(0, raw.length() - 1);
		if (!(raw.startsWith("https://") || raw.startsWith("http://localhost") || raw.startsWith("http://127.0.0.1"))) return "";
		return raw;
	}

	private static String safeMessage(Throwable e) {
		Throwable root = e;
		while (root.getCause() != null) root = root.getCause();
		String message = root.getMessage();
		return message == null || message.isBlank() ? root.getClass().getSimpleName() : message;
	}
}
