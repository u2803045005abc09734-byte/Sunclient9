package de.sunclient.hud;

import de.sunclient.SunConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;

/** Rüstungs-HUD mit getrennten Haltbarkeits-Prozentwerten unter jedem Item. */
public class ArmorModule extends HudModule {
	private static final class_1304[] SLOTS = {
		class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166, class_1304.field_6173
	};
	private static final int SLOT_WIDTH = 24;
	private static final int SLOT_HEIGHT = 29;
	private static final int ICON_SIZE = 16;

	public ArmorModule() {
		// x = sehr groß: wird automatisch an den rechten Rand gesetzt.
		super("armor", "Rüstung", 100000, 4);
	}

	private List<class_1799> stacks(class_310 mc) {
		List<class_1799> list = new ArrayList<>();
		if (mc.field_1724 != null) {
			for (class_1304 slot : SLOTS) {
				class_1799 stack = mc.field_1724.method_6118(slot);
				if (!stack.method_7960()) list.add(stack);
			}
		}
		if (list.isEmpty() && HudManager.editing) {
			list.add(new class_1799(class_1802.field_8805));
			list.add(new class_1799(class_1802.field_8058));
			list.add(new class_1799(class_1802.field_8348));
			list.add(new class_1799(class_1802.field_8285));
			list.add(new class_1799(class_1802.field_8802));
		}
		return list;
	}

	@Override
	public boolean hasContent(class_310 mc) {
		return !stacks(mc).isEmpty();
	}

	@Override
	public int width(class_310 mc) {
		int count = Math.max(1, stacks(mc).size());
		return SunConfig.INSTANCE.armorHorizontal ? count * SLOT_WIDTH : SLOT_WIDTH;
	}

	@Override
	public int height(class_310 mc) {
		int count = Math.max(1, stacks(mc).size());
		return SunConfig.INSTANCE.armorHorizontal ? SLOT_HEIGHT : count * SLOT_HEIGHT;
	}

	@Override
	public void draw(class_332 g, class_310 mc, int x, int y) {
		List<class_1799> contents = stacks(mc);
		for (int i = 0; i < contents.size(); i++) {
			class_1799 stack = contents.get(i);
			int ix = x + (SunConfig.INSTANCE.armorHorizontal ? i * SLOT_WIDTH : 0);
			int iy = y + (SunConfig.INSTANCE.armorHorizontal ? 0 : i * SLOT_HEIGHT);
			int itemX = ix + (SLOT_WIDTH - ICON_SIZE) / 2;
			g.method_51427(stack, itemX, iy);
			g.method_51431(mc.field_1772, stack, itemX, iy);

			if (stack.method_7963()) {
				int maxDamage = stack.method_7936();
				if (maxDamage <= 0) continue;
				int remaining = Math.max(0, maxDamage - stack.method_7919());
				int percent = Math.max(0, Math.min(100, Math.round(remaining * 100.0f / maxDamage)));
				String label = percent + "%";
				int textX = ix + (SLOT_WIDTH - mc.field_1772.method_1727(label)) / 2;
				int color = percent >= 60 ? 0xFF70E090 : percent >= 30 ? 0xFFFFD166 : 0xFFFF6666;
				g.method_51433(mc.field_1772, label, textX, iy + ICON_SIZE + 2, color, true);
			}
		}
	}
}
