package org.slamstudios.slamwithdraw;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
//Developed By SlamStudios
public class MessageManager {

    private final SlamWithdraw plugin;
    private FileConfiguration messagesConfig;

    public MessageManager(SlamWithdraw plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        return messagesConfig.getString(key, "Message not found: " + key).replace("&", "§");
    }
}
