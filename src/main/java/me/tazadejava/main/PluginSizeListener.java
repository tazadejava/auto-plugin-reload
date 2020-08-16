package me.tazadejava.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PluginSizeListener extends BukkitRunnable {

    private JavaPlugin plugin;

    private HashMap<String, Long> pluginLastModifiedTimes;

    private boolean broadcastOnReload;

    private File pluginsFolder;
    private FilenameFilter jarFilter;

    public PluginSizeListener(JavaPlugin plugin, boolean broadcastOnReload) {
        this.plugin = plugin;
        this.broadcastOnReload = broadcastOnReload;

        pluginLastModifiedTimes = new HashMap<>();
        pluginsFolder = plugin.getDataFolder().getParentFile();

        jarFilter = (dir, name) -> name.endsWith(".jar");

        getUpdatedPlugins();
    }

    @Override
    public void run() {
        List<File> changedFiles = getUpdatedPlugins();

        for(File file : changedFiles) {
            Plugin plugin = getExistingPlugin(file);

            if(plugin == null || !Bukkit.getPluginManager().isPluginEnabled(plugin)) {
                if(isValidPlugin(file)) {
                    log("Detected a new plugin named " + file.getName() + ". Loading the plugin now!");
                    PluginUtil.load(file);
                }
            } else {
                log("Detected changes in plugin " + plugin.getName() + ". Reloading the plugin now!");
                PluginUtil.reload(plugin, file);
            }
        }
    }

    private void log(String message) {
        message = "[AutoPluginReload] " + message;
        if(broadcastOnReload) {
            Bukkit.broadcastMessage(message);
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(message);
        }
    }

    private Plugin getExistingPlugin(File file) {
        for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if(plugin.getName().equalsIgnoreCase(file.getName().replace(".jar", ""))) {
                return plugin;
            }
        }

        return null;
    }

    private boolean isValidPlugin(File file) {
        if(!file.getName().endsWith(".jar")) {
            return false;
        }

        try {
            plugin.getPluginLoader().getPluginDescription(file);
        } catch (InvalidDescriptionException e) {
            return false;
        }

        return true;
    }

    //returns all files that have been updated
    private List<File> getUpdatedPlugins() {
        List<File> fileChanges = new ArrayList<>();

        for(File file : pluginsFolder.listFiles(jarFilter)) {
            String name = file.getName();

            if(!pluginLastModifiedTimes.containsKey(name)) {
                //new plugin
                pluginLastModifiedTimes.put(name, file.lastModified());
                fileChanges.add(file);
            } else {
                //plugin was replaced
                if(file.lastModified() != pluginLastModifiedTimes.get(name)) {
                    pluginLastModifiedTimes.put(name, file.lastModified());
                    fileChanges.add(file);
                }
            }
        }

        return fileChanges;
    }
}
