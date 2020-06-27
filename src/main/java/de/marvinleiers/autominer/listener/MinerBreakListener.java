package de.marvinleiers.autominer.listener;

import de.marvinleiers.autominer.AutoMiner;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;

public class MinerBreakListener implements Listener
{
    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        Block dispenser = event.getBlock();
        Player player = event.getPlayer();

        if (dispenser.getType() != Material.DISPENSER)
            return;

        if (!isMiner(dispenser))
            return;

        for (Map.Entry<String, Object> entry : AutoMiner.config.getSection("").getValues(false).entrySet())
        {
            if (entry.getKey().equalsIgnoreCase("blacklist"))
                continue;

            if (dispenser.getLocation().distance(AutoMiner.config.getLocation(entry.getKey())) <= 1)
            {
                String type = AutoMiner.config.getString(entry.getKey() + ".type");

                if (type.contains("dia"))
                    player.getInventory().addItem(AutoMiner.diamondMiner.clone());
                else if (type.contains("gold"))
                    player.getInventory().addItem(AutoMiner.goldMiner.clone());
                else if (type.contains("iron"))
                    player.getInventory().addItem(AutoMiner.ironMiner.clone());

                return;
            }
        }
    }

    private boolean isMiner(Block block)
    {
        if (block == null || block.getType() != Material.DISPENSER)
            return false;

        for (Location loc : AutoMiner.miners)
        {
            if (block.getLocation().distance(loc) < 1)
                return true;
        }

        return false;
    }
}
