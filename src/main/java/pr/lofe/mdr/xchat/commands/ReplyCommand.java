package pr.lofe.mdr.xchat.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pr.lofe.mdr.xchat.messaging.MessageType;
import pr.lofe.mdr.xchat.notify.Notifies;
import pr.lofe.mdr.xchat.util.MessageProcessor;
import pr.lofe.mdr.xchat.xChat;
import pr.lofe.mdr.xsettings.api.chat.ChatSettings;

import java.util.ArrayList;
import java.util.List;

public class ReplyCommand implements TabCompleter, CommandExecutor {

    MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(!(commandSender instanceof Player) || strings.length < 1)
            return true;

        Player player = (Player) commandSender;
        String receiver = xChat.i().lastMessage.get(player.getName());
        if(receiver == null) {
            player.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(player, xChat.i().getConfig().getString("pm.not_found", ""))));
            player.playSound(player, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
            return true;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) stringBuilder.append(string).append(' ');

        Player sender = (Player) commandSender;
        MessageProcessor.processMessage(sender, MessageType.PRIVATE, stringBuilder.toString(), receiver);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> output = new ArrayList<>();
        String string = xChat.i().lastMessage.get(commandSender);
        if(string == null)
            return output;
        Player target = Bukkit.getPlayer(string);
        if(target != null && ChatSettings.notifiesEnabled().getValue(target))
            Notifies.trySend(target, xChat.i().getConfig().getString("notifies.texting_you").replaceAll("#PLAYER", commandSender.getName()));
        return output;
    }
}
