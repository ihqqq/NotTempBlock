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

    private final StateFlag blockEraseFlag;
    private final StateFlag entityEraseFlag;

    public WorldGuardHook() {
        this.blockEraseFlag = new StateFlag("block-erase",  true);
        this.entityEraseFlag = new StateFlag("entity-erase",  true);
    }

    public void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registerFlag(registry, "block-erase", blockEraseFlag);
        registerFlag(registry, "entity-erase", entityEraseFlag);
    }

    public boolean canEraseBlock(Location location) {
        return testFlag(location, blockEraseFlag);
    }

    public boolean canEraseEntity(Location location) {
        return testFlag(location, entityEraseFlag);
    }

    private void registerFlag(FlagRegistry registry, String name, StateFlag flag) {
        try {
            registry.register(flag);
        } catch (FlagConflictException | IllegalStateException e) {
            LOG.fine("WorldGuard flag '" + name + "' already registered; using existing.");
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
            LOG.log(Level.WARNING, "Error while checking WorldGuard flag; defaulting to allow.", e);
            return true;
        }
    }
}
