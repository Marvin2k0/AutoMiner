package de.marvinleiers.autominer.listener;

import de.marvinleiers.autominer.AutoMiner;
import de.marvinleiers.autominer.utils.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.UUID;

public class BuildPlaceListener implements Listener
{
    @EventHandler
    public void onBuild(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        ItemStack minerItem = player.getInventory().getItemInMainHand();
        Block block = event.getBlock();

        if (block.getType() != Material.DISPENSER)
            return;

        if (!minerItem.hasItemMeta() || !minerItem.getItemMeta().getDisplayName().contains(Messages.get("item-name", false)))
            return;

        Dispenser dispenser = (Dispenser) block.getState();
        String facing = dispenser.getBlockData().getAsString().split("=")[1].split(",")[0].toUpperCase();
        BlockFace face = BlockFace.valueOf(facing);
        BlockFace chestFace = BlockFace.UP;
        String type = "iron";

        if (minerItem.getItemMeta().getDisplayName().contains("Dia"))
            type = "diamond";
        else if (minerItem.getItemMeta().getDisplayName().contains("Gold"))
            type = "gold";

        String uuid = UUID.randomUUID().toString();
        AutoMiner.config.setLocation(uuid, dispenser.getLocation());
        AutoMiner.config.set(uuid + ".type", type);


        if (block.getRelative(chestFace) == null || block.getRelative(chestFace).getType() != Material.CHEST)
        {
            player.sendMessage(Messages.get("place-chest"));
        }

        Block chestBlock = block.getRelative(chestFace);
        AutoMiner.miners.add(block.getLocation());

        new BukkitRunnable()
        {
            int i = 0;

            @Override
            public void run()
            {
                if (block.getType() != Material.DISPENSER)
                {
                    this.cancel();
                    AutoMiner.config.set(uuid, null);
                    AutoMiner.miners.remove(block.getLocation());
                    return;
                }

                if (chestBlock.getType() == Material.CHEST)
                {
                    Chest chest = (Chest) chestBlock.getState();
                    Block miningBlock = block.getRelative(face);

                    if (miningBlock.getType() != Material.AIR)
                    {
                        if (dispenser.getInventory().contains(Material.COAL) || dispenser.getInventory().contains(Material.COAL_BLOCK))
                        {

                            ItemStack mining = new ItemStack(Material.IRON_PICKAXE);
                            AutoMiner.config.set(uuid + ".type", "iron");

                            if (minerItem.getItemMeta().getDisplayName().contains("Diamond"))
                            {
                                mining = new ItemStack(Material.DIAMOND_PICKAXE);
                                AutoMiner.config.set(uuid + ".type", "diamond");
                            }
                            else if (minerItem.getItemMeta().getDisplayName().contains("Gold"))
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
                                }
                            }

                            Collection<ItemStack> drops = miningBlock.getDrops(mining);
                            AutoMiner.blocks.add(miningBlock.getLocation());

                            boolean mine = true;

                            for (Material check : AutoMiner.blacklist)
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
                                chest.getInventory().addItem(item);
                        }
                    }
                }
            }
        }.runTaskTimer(AutoMiner.plugin, 0, 20 * Long.parseLong(Messages.get("mining-interval-in-seconds", false)));
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event)
    {
        Location loc = event.getLocation();

        if (event.getEntity().getItemStack().getType() == Material.DISPENSER)
        {
            return;
        }

        for (Location locations : AutoMiner.blocks)
        {
            if (locations.distance(loc) <= 2)
            {
                event.setCancelled(true);
            }
        }
    }
}
