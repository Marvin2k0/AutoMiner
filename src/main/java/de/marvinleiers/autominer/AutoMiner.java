package de.marvinleiers.autominer;

import de.marvinleiers.autominer.listener.BuildPlaceListener;
import de.marvinleiers.autominer.listener.MinerBreakListener;
import de.marvinleiers.autominer.utils.CustomConfig;
import de.marvinleiers.autominer.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class AutoMiner extends JavaPlugin
{
    public static AutoMiner plugin;
    public static CustomConfig config;

    public static ArrayList<Location> miners = new ArrayList<>();
    public static ArrayList<Location> blocks = new ArrayList<>();
    public static ArrayList<Material> blacklist = new ArrayList<>();

    public static ItemStack diamondMiner;
    public static ItemStack goldMiner;
    public static ItemStack ironMiner;

    @Override
    public void onEnable()
    {
        plugin = this;

        CustomConfig.setUp(this);
        Messages.setUp(this);

        config = new CustomConfig(this.getDataFolder().getPath() + "/data/miners.yml");

        ArrayList<String> blackList = new ArrayList<>();
        blackList.add("BEDROCK");
        config.addDefault("blacklist", blackList);

        this.getServer().getPluginManager().registerEvents(new BuildPlaceListener(), this);
        this.getServer().getPluginManager().registerEvents(new MinerBreakListener(), this);

        this.getCommand("miner").setExecutor(this);

        this.addRecipies();
        this.addBlackistedBlocks();
        this.loadMiners();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Messages.get("no-player"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0)
        {
            player.sendMessage("§cUsage: /miner <dia|gold|iron>");
            return true;
        }

        String type = args[0];

        if (!type.equalsIgnoreCase("dia") && !type.equalsIgnoreCase("gold") && !type.equalsIgnoreCase("iron"))
        {
            player.sendMessage("§4" + type + " §cis not valid!");
            return true;
        }

        if (player.getInventory().firstEmpty() == -1)
        {
            player.sendMessage("§cYour inventory is full!");
            return true;
        }

        if (type.equalsIgnoreCase("dia"))
            player.getInventory().addItem(diamondMiner.clone());
        else if (type.equalsIgnoreCase("gold"))
            player.getInventory().addItem(goldMiner.clone());
        else if (type.equalsIgnoreCase("iron"))
            player.getInventory().addItem(ironMiner.clone());

        player.sendMessage("§7You received your item");
        return true;
    }

    private void addBlackistedBlocks()
    {
        if (!config.isSet("blacklist"))
            return;

        for (String material : config.getConfig().getStringList("blacklist"))
        {
            blacklist.add(Material.getMaterial(material.toUpperCase()));
            System.out.println("loaded blacklisted material " + material.toUpperCase());
        }
    }

    private void loadMiners()
    {
        for (Map.Entry<String, Object> entry : config.getSection("").getValues(false).entrySet())
        {
            if (entry.getKey().equalsIgnoreCase("blacklist"))
                continue;

            String uuid = entry.getKey();
            Block block = config.getLocation(entry.getKey()).getBlock();
            Block chestBlock = block.getRelative(BlockFace.UP);
            Dispenser dispenser = (Dispenser) block.getState();
            String facing = dispenser.getBlockData().getAsString().split("=")[1].split(",")[0].toUpperCase();
            BlockFace face = BlockFace.valueOf(facing);
            String type = config.getString(entry.getKey() + ".type");

            System.out.println("loading " + type + " miner at " + block.getLocation());
            miners.add(block.getLocation());

            try
            {
                new BukkitRunnable()
                {
                    int i = 0;

                    @Override
                    public void run()
                    {
                        if (config.getLocation(entry.getKey()).getBlock().getType() != Material.DISPENSER)
                        {
                            this.cancel();
                            AutoMiner.config.set(uuid, null);
                            miners.remove(block.getLocation());
                            return;
                        }

                        if (chestBlock.getType() == Material.CHEST)
                        {
                            Chest chest = (Chest) chestBlock.getState();
                            Block miningBlock = block.getRelative(face);

                            if (miningBlock.getType() != Material.AIR)
                            {
                                if (dispenser.getInventory().contains(Material.COAL))
                                {

                                    ItemStack mining = new ItemStack(Material.IRON_PICKAXE);
                                    AutoMiner.config.set(uuid + ".type", "iron");

                                    if (type.equalsIgnoreCase("Diamond"))
                                    {
                                        mining = new ItemStack(Material.DIAMOND_PICKAXE);
                                        AutoMiner.config.set(uuid + ".type", "diamond");
                                    }
                                    else if (type.equalsIgnoreCase("Gold"))
                                    {
                                        mining = new ItemStack(Material.GOLDEN_PICKAXE);
                                        AutoMiner.config.set(uuid + ".type", "gold");
                                    }

                                    for (ItemStack coal : dispenser.getInventory().getContents())
                                    {
                                        if (coal == null)
                                            continue;

                                        if (coal.getType() == Material.ENCHANTED_BOOK)
                                        {
                                            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) coal.getItemMeta();
                                            mining.addUnsafeEnchantments(meta.getStoredEnchants());
                                        }
                                    }

                                    if (i % Integer.parseInt(Messages.get("blocks-per-coal", false)) == 0)
                                    {
                                        for (ItemStack coal : dispenser.getInventory().getContents())
                                        {
                                            if (coal == null)
                                                continue;

                                            if (coal.getType() == Material.COAL)
                                            {
                                                coal.setAmount(coal.getAmount() - 1);
                                            }
                                            else if (coal.getType() == Material.ENCHANTED_BOOK)
                                            {
                                                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) coal.getItemMeta();
                                                mining.addUnsafeEnchantments(meta.getStoredEnchants());
                                            }
                                        }
                                    }

                                    Collection<ItemStack> drops = miningBlock.getDrops(mining);
                                    AutoMiner.blocks.add(miningBlock.getLocation());

                                    boolean mine = true;

                                    for (Material check : blacklist)
                                    {
                                        if (check == miningBlock.getType())
                                        {
                                            mine = false;
                                            break;
                                        }
                                    }

                                    if (mine)
                                        miningBlock.breakNaturally(mining);

                                    i++;

                                    for (ItemStack item : drops)
                                    {
                                        chest.getInventory().addItem(item);
                                    }
                                }
                            }
                        }
                    }
                }.runTaskTimer(this, 0, 20 * Long.parseLong(Messages.get("mining-interval-in-seconds", false)));
            }
            catch (Exception ignored)
            {
            }
        }
    }

    private void addRecipies()
    {
        ItemStack autominer = new ItemStack(Material.DISPENSER);
        ItemMeta meta = autominer.getItemMeta();
        meta.setDisplayName("§6Gold " + Messages.get("item-name", false));
        autominer.setItemMeta(meta);
        goldMiner = autominer.clone();

        ShapedRecipe goldenPickaxe = new ShapedRecipe(new NamespacedKey(this, "Marvin2k0_AutoMiner_gold"), autominer);
        goldenPickaxe.shape("BDB", "RHR", "BPB");
        goldenPickaxe.setIngredient('D', new ItemStack(Material.DISPENSER).getData());
        goldenPickaxe.setIngredient('R', new ItemStack(Material.REDSTONE).getData());
        goldenPickaxe.setIngredient('P', new ItemStack(Material.GOLDEN_PICKAXE).getData());
        goldenPickaxe.setIngredient('H', new ItemStack(Material.HOPPER).getData());
        goldenPickaxe.setIngredient('B', new ItemStack(Material.GOLD_BLOCK).getData());

        Bukkit.addRecipe(goldenPickaxe);

        meta.setDisplayName("§bDiamond " + Messages.get("item-name", false));
        autominer.setItemMeta(meta);
        diamondMiner = autominer.clone();

        ShapedRecipe diamnondPickaxe = new ShapedRecipe(new NamespacedKey(this, "Marvin2k0_AutoMiner_dia"), autominer);
        diamnondPickaxe.shape("BDB", "RHR", "BPB");
        diamnondPickaxe.setIngredient('D', new ItemStack(Material.DISPENSER).getData());
        diamnondPickaxe.setIngredient('H', new ItemStack(Material.HOPPER).getData());
        diamnondPickaxe.setIngredient('R', new ItemStack(Material.REDSTONE).getData());
        diamnondPickaxe.setIngredient('B', new ItemStack(Material.DIAMOND_BLOCK).getData());
        diamnondPickaxe.setIngredient('P', new ItemStack(Material.DIAMOND_PICKAXE).getData());

        Bukkit.addRecipe(diamnondPickaxe);

        meta.setDisplayName("§fIron " + Messages.get("item-name", false));
        autominer.setItemMeta(meta);
        ironMiner = autominer.clone();

        ShapedRecipe ironPickaxe = new ShapedRecipe(new NamespacedKey(this, "Marvin2k0_AutoMiner_iron"), autominer);
        ironPickaxe.shape("BDB", "RHR", "BPB");
        ironPickaxe.setIngredient('D', new ItemStack(Material.DISPENSER).getData());
        ironPickaxe.setIngredient('H', new ItemStack(Material.HOPPER).getData());
        ironPickaxe.setIngredient('R', new ItemStack(Material.REDSTONE).getData());
        ironPickaxe.setIngredient('B', new ItemStack(Material.IRON_BLOCK).getData());
        ironPickaxe.setIngredient('P', new ItemStack(Material.IRON_PICKAXE).getData());

        Bukkit.addRecipe(ironPickaxe);
    }
}
