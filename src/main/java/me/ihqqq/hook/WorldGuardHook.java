package me.ihqqq.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Location;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorldGuardHook {
    private static final Logger LOG = Logger.getLogger("notTempBlock");

    private final StateFlag tempBlockFlag;
    private final StateFlag tempEntityFlag;

    public WorldGuardHook() {
        this.tempBlockFlag = new StateFlag("block-temp",  true);
        this.tempEntityFlag = new StateFlag("entity-temp",  true);
    }

    public void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registerFlag(registry, "block-temp", tempBlockFlag);
        registerFlag(registry, "entity-temp", tempEntityFlag);
    }

    public boolean canEraseBlock(Location location) {
        return testFlag(location, tempBlockFlag);
    }

    public boolean canEraseEntity(Location location) {
        return testFlag(location, tempEntityFlag);
    }

    private void registerFlag(FlagRegistry registry, String name, StateFlag flag) {
        try {
            registry.register(flag);
        } catch (FlagConflictException | IllegalStateException e) {
            LOG.fine("Flag WorldGuard '" + name + "' đã được đăng ký trước đó; sử dụng flag hiện có.");
        }
    }

    private boolean testFlag(Location location, StateFlag flag) {
        if (location.getWorld() == null) return true;
        try {
            var query = WorldGuard.getInstance()
                    .getPlatform()
                    .getRegionContainer()
                    .createQuery();
            var result = query.getApplicableRegions(BukkitAdapter.adapt(location))
                    .testState(null, flag);
            return result;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Lỗi khi kiểm tra flag WorldGuard; mặc định cho phép.", e);
            return true;
        }
    }
}