package dk.lockfuglsang.minecraft.animation;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

/**
 * Sends (bogus) block-info to the player
 */
public class BlockAnimation implements Animation {
    private final Player player;
    private final List<Location> points;
    private final BlockData material;
    private volatile boolean shown;

    @Deprecated
    public BlockAnimation(Player player, List<Location> points, Material material, byte data) {
        this(player, points, material.createBlockData());
    }

    public BlockAnimation(Player player, List<Location> points, BlockData material) {
        this.player = player;
        this.points = points;
        this.material = material;
        shown = false;
    }

    @Override
    public boolean show() {
        if (shown) {
            return true;
        }
        if (!player.isOnline()) {
            return false;
        }
        for (Location loc : points) {
            if (!PlayerHandler.sendBlockChange(player, loc, material)) {
                return false;
            }
        }
        shown = true;
        return true;
    }

    @Override
    public boolean hide() {
        try {
            if (shown) {
                for (Location loc : points) {
                    if (!PlayerHandler.sendBlockChange(player, loc, loc.getBlock().getBlockData())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } finally {
            shown = false;
        }
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
