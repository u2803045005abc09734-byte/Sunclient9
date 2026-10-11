package de.sunclient;

import org.lwjgl.glfw.GLFW;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import net.minecraft.class_310;
import net.minecraft.class_640;

/**
 * Messwerte: FPS, CPS, Ping.
 *
 * FPS werden pro echtem Frame gezählt (Hook in Minecraft.runTick). Mods wie Exordium zeichnen
 * das HUD nur selten neu - deshalb darf man FPS nicht im HUD-Callback zählen. Falls der Hook
 * nicht greift, zählt der HUD-Callback als Ersatz.
 */
public final class Stats {
	private Stats() {}

	// ---- FPS ----
	private static boolean mixinActive = false;
	private static long mixinFrames = 0;
	private static long hudFrames = 0;
	private static long lastSample = System.nanoTime();
	private static long mixinAtSample = 0;
	private static long hudAtSample = 0;
	private static int fps = 0;

	/** Wird in jedem echten Frame aufgerufen (Mixin). */
	public static void onFrame() {
		mixinActive = true;
		mixinFrames++;
		sample();
		pollClicks();
	}

	/** Wird im HUD-Callback aufgerufen (Ersatz, falls der Mixin nicht greift). */
	public static void onHudFrame() {
		hudFrames++;
		sample();
		if (!mixinActive) pollClicks();
	}

	private static void sample() {
		long now = System.nanoTime();
		long dt = now - lastSample;
		if (dt < 500_000_000L) return;
		long frames = mixinActive ? (mixinFrames - mixinAtSample) : (hudFrames - hudAtSample);
		fps = (int) Math.round(frames * 1_000_000_000.0 / dt);
		mixinAtSample = mixinFrames;
		hudAtSample = hudFrames;
		lastSample = now;
	}

	public static int fps() {
		return fps;
	}

	// ---- CPS ----
	private static final ArrayDeque<Long> leftClicks = new ArrayDeque<>();
	private static final ArrayDeque<Long> rightClicks = new ArrayDeque<>();
	private static boolean leftDown = false;
	private static boolean rightDown = false;

	private static void pollClicks() {
		long window = GLFW.glfwGetCurrentContext();
		if (window == 0L) return;
		long now = System.currentTimeMillis();

		boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean right = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		if (left && !leftDown) leftClicks.addLast(now);
		if (right && !rightDown) rightClicks.addLast(now);
		leftDown = left;
		rightDown = right;
		prune(now);
	}

	private static void prune(long now) {
		while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000) leftClicks.pollFirst();
		while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000) rightClicks.pollFirst();
	}

	public static int cpsLeft() {
		prune(System.currentTimeMillis());
		return leftClicks.size();
	}

	public static int cpsRight() {
		prune(System.currentTimeMillis());
		return rightClicks.size();
	}

	public static boolean leftDown() {
		return leftDown;
	}

	public static boolean rightDown() {
		return rightDown;
	}

	// ---- Ping ----
	private static volatile int tcpPing = -1;
	private static Thread tcpThread;

	/** Ping in ms, -1 = unbekannt. */
	public static int ping(class_310 mc) {
		if (SunConfig.INSTANCE.pingMeasure) {
			ensureTcpThread();
			return tcpPing;
		}
		var connection = mc.method_1562();
		if (connection == null || mc.field_1724 == null) return -1;
		class_640 info = connection.method_2871(mc.field_1724.method_5667());
		return info == null ? -1 : info.method_2959();
	}

	/** Misst alle 2 Sekunden die Dauer eines TCP-Verbindungsaufbaus zum aktuellen Server. */
	public static synchronized void ensureTcpThread() {
		if (tcpThread != null && tcpThread.isAlive()) return;
		tcpThread = new Thread(() -> {
			while (true) {
				try {
					var server = class_310.method_1551().method_1558();
					if (server == null || server.field_3761 == null || server.field_3761.isBlank()) {
						tcpPing = -1;
					} else {
						String host = server.field_3761;
						int port = 25565;
						int idx = host.lastIndexOf(':');
						if (idx > 0 && host.indexOf(':') == idx) {
							try {
								port = Integer.parseInt(host.substring(idx + 1));
								host = host.substring(0, idx);
							} catch (NumberFormatException ignored) {
							}
						}
						tcpPing = measure(host, port);
					}
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					return;
				} catch (Throwable t) {
					tcpPing = -1;
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, "SunClient-Ping");
		tcpThread.setDaemon(true);
		tcpThread.start();
	}

	private static int measure(String host, int port) {
		try (Socket socket = new Socket()) {
			InetSocketAddress address = new InetSocketAddress(host, port);
			long start = System.nanoTime();
			socket.connect(address, 2000);
			return (int) ((System.nanoTime() - start) / 1_000_000L);
		} catch (Exception e) {
			return -1;
		}
	}
}
