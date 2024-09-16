package org.slamstudios.currencyvouchers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slamstudios.currencyvouchers.CurrencyVouchers;
import org.slamstudios.currencyvouchers.MessageManager;
import org.slamstudios.currencyvouchers.VoucherManager;
//Developed By SlamStudios
@CommandAlias("withdraw")
@Description("Withdraw money into a voucher")
public class WithdrawCommand extends BaseCommand {

    private final VoucherManager voucherManager;
    private final MessageManager messageManager;

    public WithdrawCommand(VoucherManager voucherManager, MessageManager messageManager) {
        this.voucherManager = voucherManager;
        this.messageManager = messageManager;
    }

    @Default
    public void onWithdraw(Player player, double amount) {
        double minAmount = CurrencyVouchers.getInstance().getConfig().getDouble("minimum-amount", 1);
        double maxAmount = CurrencyVouchers.getInstance().getConfig().getDouble("maximum-amount", 10000);

        if (amount < minAmount || amount > maxAmount) {
            player.sendMessage(messageManager.getMessage("withdraw.out_of_bounds")
                    .replace("%min%", String.valueOf(minAmount))
                    .replace("%max%", String.valueOf(maxAmount)));
            return;
        }

        Economy economy = CurrencyVouchers.getInstance().getEconomy();
        if (economy.getBalance(player) < amount) {
            player.sendMessage(messageManager.getMessage("withdraw.not_enough_money"));
            return;
        }

        economy.withdrawPlayer(player, amount);
        ItemStack voucher = voucherManager.createVoucher(player.getName(), amount);
        player.getInventory().addItem(voucher);
        player.sendMessage(messageManager.getMessage("withdraw.success").replace("%amount%", String.valueOf(amount)));
    }
}
