package eu.adiih.buildffa.utils;

import eu.adiih.buildffa.FFAPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    private static final FFAPlugin plugin = FFAPlugin.getPlugin(FFAPlugin.class);

    public static void saveInventoryContents(final ItemStack[] contents) {
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) plugin.getStorageConfig().set("defaultKit." + i, "empty");
            else plugin.getStorageConfig().set("defaultKit." + i, item);
        }
        plugin.saveStorage();
    }

    public static ItemStack[] loadInventoryContents() {
        ConfigurationSection cs = plugin.getStorageConfig().getConfigurationSection("defaultKit");
        if (cs == null) return null;
        List<ItemStack> items = new ArrayList<>();
        for (String key : cs.getKeys(false)) {
            Object o = cs.get(key);
            if (o instanceof ItemStack) items.add((ItemStack)o);
            else items.add(null);
        }
        return items.toArray(new ItemStack[0]);
    }

}
