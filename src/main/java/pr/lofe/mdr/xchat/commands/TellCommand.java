package pr.lofe.mdr.xchat.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pr.lofe.mdr.api.util.SortUtil;
import pr.lofe.mdr.xchat.messaging.MessageType;
import pr.lofe.mdr.xchat.notify.Notifies;
import pr.lofe.mdr.xchat.util.MessageProcessor;
import pr.lofe.mdr.xchat.xChat;
import pr.lofe.mdr.xsettings.api.chat.ChatSettings;

import java.util.ArrayList;
import java.util.List;

public class TellCommand implements TabCompleter, CommandExecutor {

    MiniMessage mm = MiniMessage.miniMessage();


    @Override public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player) || strings.length < 2)
            return true;

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < strings.length; i++)
            stringBuilder.append(strings[i]).append(' ');

        Player sender = (Player) commandSender;
        MessageProcessor.processMessage(sender, MessageType.PRIVATE, stringBuilder.toString(), strings[0]);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> output = new ArrayList<>();
        if(strings.length == 1) {
            for(Player player : Bukkit.getOnlinePlayers())
                output.add(player.getName());
        }
        else if (strings.length >= 2) {
            Player target = Bukkit.getPlayer(strings[0]);
            if(target != null && ChatSettings.notifiesEnabled().getValue(target))
                Notifies.trySend(target, xChat.i().getConfig().getString("notifies.texting_you").replaceAll("#PLAYER", commandSender.getName()));
        }
        return SortUtil.search(output, strings[strings.length - 1]);
    }
}
