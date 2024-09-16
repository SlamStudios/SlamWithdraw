package org.slamstudios.currencyvouchers;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
//Developed By SlamStudios
public class VoucherManager implements Listener {

    private final CurrencyVouchers plugin;
    private final Set<UUID> usedUUIDs = new HashSet<>();
    private final File logFile;

    public VoucherManager(CurrencyVouchers plugin) {
        this.plugin = plugin;
        logFile = new File(plugin.getDataFolder(), "banknote_log.txt");
        createLogFile();
    }

    private void createLogFile() {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create log file: " + e.getMessage());
        }
    }

    public ItemStack createVoucher(String signer, double amount) {
        ItemStack item = new ItemStack(Material.valueOf(plugin.getConfig().getString("voucher-item.material", "PAPER")));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        NamespacedKey amountKey = plugin.getAmountKey();
        NamespacedKey signerKey = plugin.getSignerKey();
        NamespacedKey uuidKey = new NamespacedKey(plugin, "voucher_uuid");

        UUID voucherUUID = UUID.randomUUID();
        meta.getPersistentDataContainer().set(amountKey, PersistentDataType.DOUBLE, amount);
        meta.getPersistentDataContainer().set(signerKey, PersistentDataType.STRING, signer);
        meta.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, voucherUUID.toString());

        meta.setDisplayName(plugin.getConfig().getString("voucher-item.name").replace("&", "§"));
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("voucher-item.lore")) {
            lore.add(line.replace("&", "§").replace("%amount%", String.valueOf(amount)).replace("%signer%", signer));
        }
        meta.setLore(lore);

        item.setItemMeta(meta);
        item.setAmount(1);  // Ensure the item is unstackable

        // Log the creation of the voucher
        logMovement("Created voucher with UUID: " + voucherUUID + ", Amount: " + amount + ", Signer: " + signer);

        return item;
    }

    @EventHandler
    public void onPlayerUseVoucher(PlayerInteractEvent event) {
        handleVoucherInteraction(event.getItem(), event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        handleVoucherInteraction(event.getItem().getItemStack(), event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        handleVoucherInteraction(event.getItemDrop().getItemStack(), event.getPlayer(), false);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            handleVoucherInteraction(event.getCurrentItem(), (Player) event.getWhoClicked(), false);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        event.getNewItems().forEach((slot, item) -> {
            if (item != null) {
                handleVoucherInteraction(item, (Player) event.getWhoClicked(), false);
            }
        });
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getItem() != null) {
            handleVoucherInteraction(event.getItem(), null, false);  // No player involved, just log the movement
        }
    }

    private void handleVoucherInteraction(ItemStack item, Player player, boolean isRedemption) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey uuidKey = new NamespacedKey(plugin, "voucher_uuid");

        if (meta.getPersistentDataContainer().has(uuidKey, PersistentDataType.STRING)) {
            String uuidString = meta.getPersistentDataContainer().get(uuidKey, PersistentDataType.STRING);
            UUID voucherUUID;
            try {
                voucherUUID = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                logMovement("Failed to parse UUID for item: " + item.toString());
                return;
            }

            if (isRedemption) {
                // Check for duplication during redemption
                if (usedUUIDs.contains(voucherUUID)) {
                    logMovement("Duplicate banknote detected during redemption with UUID: " + voucherUUID);
                    if (player != null) {
                        player.sendMessage(plugin.getMessageManager().getMessage("redeem.duplicate_error"));
                    }
                    item.setAmount(0);  // Remove the item
                    return;
                }

                // Mark the UUID as used and redeem
                usedUUIDs.add(voucherUUID);
                double amount = meta.getPersistentDataContainer().get(plugin.getAmountKey(), PersistentDataType.DOUBLE);
                plugin.getEconomy().depositPlayer(player, amount);
                player.getInventory().remove(item);
                logMovement("Redeemed voucher with UUID: " + voucherUUID + ", Amount: " + amount + ", Player: " + player.getName());
                player.sendMessage(plugin.getMessageManager().getMessage("redeem.success").replace("%amount%", String.valueOf(amount)));
                item.setAmount(0);  // Ensure item is removed from inventory
            } else {
                // Just log non-redemption interactions
                logMovement("Item interacted with UUID: " + voucherUUID);
            }
        }
    }

    private void logMovement(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to log banknote movement: " + e.getMessage());
        }
    }
}