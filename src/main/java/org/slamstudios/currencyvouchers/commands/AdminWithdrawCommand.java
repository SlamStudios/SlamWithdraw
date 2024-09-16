package org.slamstudios.currencyvouchers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slamstudios.currencyvouchers.MessageManager;
import org.slamstudios.currencyvouchers.VoucherManager;
//Developed By SlamStudios
@CommandAlias("adminwithdraw")
@Description("Create a voucher with a specified signer")
@CommandPermission("voucher.adminwithdraw")
public class AdminWithdrawCommand extends BaseCommand {

    private final VoucherManager voucherManager;
    private final MessageManager messageManager;

    public AdminWithdrawCommand(VoucherManager voucherManager, MessageManager messageManager) {
        this.voucherManager = voucherManager;
        this.messageManager = messageManager;
    }

    @Default
    public void onAdminWithdraw(CommandSender sender, double amount, @Optional String signer) {
        if (signer == null) {
            signer = sender.getName();
        }

        ItemStack voucher = voucherManager.createVoucher(signer, amount);
        if (sender instanceof Player) {
            ((Player) sender).getInventory().addItem(voucher);
        } else {
            sender.sendMessage(messageManager.getMessage("adminwithdraw.success")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%signer%", signer));
        }
    }
}
