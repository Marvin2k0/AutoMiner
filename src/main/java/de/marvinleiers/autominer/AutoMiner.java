package de.marvinleiers.autominer;

import de.marvinleiers.autominer.listener.BuildPlaceListener;
import de.marvinleiers.autominer.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoMiner extends JavaPlugin
{
    public static AutoMiner plugin;

    @Override
    public void onEnable()
    {
        plugin = this;

        Messages.setUp(this);

        this.getServer().getPluginManager().registerEvents(new BuildPlaceListener(), this);

        this.addRecipies();
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }

    private void addRecipies()
    {
        ItemStack autominer = new ItemStack(Material.DISPENSER);
        ItemMeta meta = autominer.getItemMeta();
        meta.setDisplayName(Messages.get("item-name", false));
        autominer.setItemMeta(meta);

        ShapedRecipe emeraldRecipe = new ShapedRecipe(new NamespacedKey(this, "Marvin2k0_AutoMiner"), autominer);
        emeraldRecipe.shape("D", "H", "G");
        emeraldRecipe.setIngredient('D', new ItemStack(Material.DISPENSER).getData());
        emeraldRecipe.setIngredient('H', new ItemStack(Material.HOPPER).getData());
        emeraldRecipe.setIngredient('G', new ItemStack(Material.GOLDEN_PICKAXE).getData());

        Bukkit.addRecipe(emeraldRecipe);
    }
}
