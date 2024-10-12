package org.slamstudios.slamwithdraw;

import co.aikar.commands.BukkitCommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.slamstudios.slamwithdraw.commands.AdminWithdrawCommand;
import org.slamstudios.slamwithdraw.commands.WithdrawCommand;
//Developed By SlamStudios
public class SlamWithdraw extends JavaPlugin {

    private static SlamWithdraw instance;
    private VoucherManager voucherManager;
    private MessageManager messageManager;
    private Economy economy;
    private NamespacedKey amountKey;
    private NamespacedKey signerKey;

    public static SlamWithdraw getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        messageManager = new MessageManager(this);
        voucherManager = new VoucherManager(this);
        amountKey = new NamespacedKey(this, "voucher_amount");
        signerKey = new NamespacedKey(this, "voucher_signer");

        if (!setupEconomy()) {
            getLogger().severe(messageManager.getMessage("general.vault_not_found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new WithdrawCommand(voucherManager, messageManager));
        commandManager.registerCommand(new AdminWithdrawCommand(voucherManager, messageManager));

        getServer().getPluginManager().registerEvents(voucherManager, this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public VoucherManager getVoucherManager() {
        return voucherManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public NamespacedKey getAmountKey() {
        return amountKey;
    }

    public NamespacedKey getSignerKey() {
        return signerKey;
    }
}