package de.marvinleiers.autominer.listener;

import de.marvinleiers.autominer.AutoMiner;
import de.marvinleiers.autominer.utils.Messages;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

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

        if (!minerItem.hasItemMeta() || !minerItem.getItemMeta().getDisplayName().equalsIgnoreCase(Messages.get("item-name", false)))
            return;

        Dispenser dispenser = (Dispenser) block.getState();
        String facing = dispenser.getBlockData().getAsString().split("=")[1].split(",")[0].toUpperCase();
        BlockFace face = BlockFace.valueOf(facing);
        BlockFace chestFace = BlockFace.UP;

        if (block.getRelative(chestFace) == null || block.getRelative(chestFace).getType() != Material.CHEST)
        {
            player.sendMessage(Messages.get("place-chest"));
        }

        Block chestBlock = block.getRelative(chestFace);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (block.getType() != Material.DISPENSER)
                {
                    this.cancel();
                    return;
                }

                if (chestBlock.getType() == Material.CHEST)
                {
                    Chest chest = (Chest) chestBlock.getState();
                    Block miningBlock = block.getRelative(face);

                    if (miningBlock.getType() != Material.AIR)
                    {


                        Collection<ItemStack> drops = miningBlock.getDrops(new ItemStack(Material.DIAMOND_PICKAXE));
                        miningBlock.setType(Material.AIR);

                        for (ItemStack item : drops)
                            chest.getInventory().addItem(item);
                    }
                }


            }
        }.runTaskTimer(AutoMiner.plugin, 0, 20 * Long.parseLong(Messages.get("mining-interval-in-seconds", false)));
    }
}
