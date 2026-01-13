package com.spygstudios.chestshop.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.spygstudios.chestshop.ChestShop;

import lombok.Getter;

@Getter
public final class WorldGuardHook {

    private static final String FLAG_NAME_ALLOW_SHOP = "allow-shop";

    private static volatile boolean available = false;
    private static volatile StateFlag allowShopFlag = null;

    private WorldGuardHook() {
    }

    public static void onLoad(ChestShop plugin) {
        Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (!(wg instanceof WorldGuardPlugin)) {
            return;
        }

        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            StateFlag flag = new StateFlag(FLAG_NAME_ALLOW_SHOP, false);
            registry.register(flag);
            allowShopFlag = flag;
            plugin.getLogger().info("Registered WorldGuard flag: " + FLAG_NAME_ALLOW_SHOP);
        } catch (FlagConflictException conflict) {
            Flag<?> existing = WorldGuard.getInstance().getFlagRegistry().get(FLAG_NAME_ALLOW_SHOP);
            if (existing instanceof StateFlag stateFlag) {
                allowShopFlag = stateFlag;
                plugin.getLogger().info("Reusing existing WorldGuard flag: " + FLAG_NAME_ALLOW_SHOP);
            } else {
                plugin.getLogger().warning("WorldGuard flag name conflict for '" + FLAG_NAME_ALLOW_SHOP
                        + "' (existing type: " + (existing == null ? "null" : existing.getClass().getName()) + "). "
                        + "ChestShop WorldGuard integration will be disabled.");
                allowShopFlag = null;
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to register WorldGuard flag '" + FLAG_NAME_ALLOW_SHOP
                    + "'. ChestShop WorldGuard integration will be disabled. Error: "
                    + t.getClass().getSimpleName() + ": " + t.getMessage());
            allowShopFlag = null;
        }
    }

    public static void onEnable(JavaPlugin plugin) {
        available = Bukkit.getPluginManager().isPluginEnabled("WorldGuard") && allowShopFlag != null;
        if (available) {
            plugin.getLogger().info("WorldGuard integration enabled (flag: " + FLAG_NAME_ALLOW_SHOP + ").");
        } else if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            plugin.getLogger().warning("WorldGuard detected, but ChestShop WorldGuard integration is not active (flag unavailable).");
        }
    }

    public static boolean isShopCreationAllowed(Player player, Location location) {
        if (!available || player.hasPermission("spygchestshop.bypass.worldguard")) {
            return true;
        }

        if (location == null) {
            throw new IllegalArgumentException("location cannot be null");
        }

        ApplicableRegionSet set = getApplicableRegions(location);
        boolean onlyInRegions = ChestShop.getInstance().getConf().getBoolean("only-in-regions");
        if ((set == null || set.getRegions().isEmpty())) {
            if (onlyInRegions) {
                return false;
            }
            return true;
        }

        for (ProtectedRegion region : set) {
            State state = region.getFlag(allowShopFlag);
            if (state == State.ALLOW) {
                return true;
            } else if (state == State.DENY) {
                return false;
            }
        }
        return true;
    }

    private static ApplicableRegionSet getApplicableRegions(Location location) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            return query.getApplicableRegions(BukkitAdapter.adapt(location));
        } catch (Throwable t) {
            return null;
        }
    }
}
