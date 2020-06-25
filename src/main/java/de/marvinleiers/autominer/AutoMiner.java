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
    }

    private void addRecipies()
    {
        ItemStack autominer = new ItemStack(Material.DISPENSER);
        ItemMeta meta = autominer.getItemMeta();
        meta.setDisplayName("§6Gold " + Messages.get("item-name", false));
        autominer.setItemMeta(meta);

        ShapedRecipe goldenPickaxe = new ShapedRecipe(new NamespacedKey(this, "Marvin2k0_AutoMiner_gold"), autominer);
        goldenPickaxe.shape("D", "H", "G");
        goldenPickaxe.setIngredient('D', new ItemStack(Material.DISPENSER).getData());
        goldenPickaxe.setIngredient('H', new ItemStack(Material.HOPPER).getData());
        goldenPickaxe.setIngredient('G', new ItemStack(Material.GOLDEN_PICKAXE).getData());

        Bukkit.addRecipe(goldenPickaxe);

        meta.setDisplayName("§bDiamond " + Messages.get("item-name", false));
        autominer.setItemMeta(meta);

        ShapedRecipe diamnondPickaxe = new ShapedRecipe(new NamespacedKey(this, "Marvin2k0_AutoMiner_dia"), autominer);
        diamnondPickaxe.shape("D", "H", "G");
        diamnondPickaxe.setIngredient('D', new ItemStack(Material.DISPENSER).getData());
        diamnondPickaxe.setIngredient('H', new ItemStack(Material.HOPPER).getData());
        diamnondPickaxe.setIngredient('G', new ItemStack(Material.DIAMOND_PICKAXE).getData());

        Bukkit.addRecipe(diamnondPickaxe);

        meta.setDisplayName("§fIron " + Messages.get("item-name", false));
        autominer.setItemMeta(meta);

        ShapedRecipe ironPickaxe = new ShapedRecipe(new NamespacedKey(this, "Marvin2k0_AutoMiner_iron"), autominer);
        ironPickaxe.shape("D", "H", "G");
        ironPickaxe.setIngredient('D', new ItemStack(Material.DISPENSER).getData());
        ironPickaxe.setIngredient('H', new ItemStack(Material.HOPPER).getData());
        ironPickaxe.setIngredient('G', new ItemStack(Material.IRON_PICKAXE).getData());

        Bukkit.addRecipe(ironPickaxe);
    }
}
