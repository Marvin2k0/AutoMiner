package de.marvinleiers.autominer.listener;

import de.marvinleiers.autominer.AutoMiner;
import de.marvinleiers.autominer.utils.Messages;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BuildPlaceListener implements Listener
{
    @EventHandler
    public void onBuild(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() != Material.DISPENSER)
            return;

        Dispenser dispenser = (Dispenser) block.getState();
        String facing = dispenser.getBlockData().getAsString().split("=")[1].split(",")[0].toUpperCase();
        BlockFace face = BlockFace.valueOf(facing);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (block.getType() != Material.DISPENSER)
                {
                    System.out.println("blokc wurde zerstört");
                    this.cancel();
                    return;
                }

                System.out.println("mining...");
            }
        }.runTaskTimer(AutoMiner.plugin, 0, 20 * Long.parseLong(Messages.get("mining-interval-in-seconds", false)));
    }
}
