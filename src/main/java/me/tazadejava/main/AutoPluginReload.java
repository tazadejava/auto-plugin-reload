package me.tazadejava.main;

import org.bukkit.plugin.java.JavaPlugin;

public class AutoPluginReload extends JavaPlugin {

    private PluginSizeListener runnable;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        boolean broadcastOnReload = getConfig().getBoolean("broadcast-on-reload", false);

        if(broadcastOnReload) {
            getLogger().info("[AutoPluginReload] Plugin updates will be broadcasted to all players.");
        }

        runnable = new PluginSizeListener(this, broadcastOnReload);
        runnable.runTaskTimer(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        if(!runnable.isCancelled()) {
            runnable.cancel();
        }
    }
}
