package pb.lofe.chatl.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TellCommand implements TabCompleter, CommandExecutor {

    MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length < 2)
            return true;
        CommandSender receiver = Bukkit.getPlayer(strings[0]);
        if(receiver == null && !strings[0].equalsIgnoreCase("CONSOLE")) {
            commandSender.sendMessage(mm.deserialize("<white>ЛС <dark_gray>| <gray>Игрок не существует!"));
            return true;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < strings.length; i++) {
            stringBuilder.append(strings[i] + ' ');
        }

        receiver.sendMessage(mm.deserialize("<white>ЛС <dark_gray>|<white> <bold>" + commandSender.getName() + "</bold> <gray>> <white>Я <dark_gray>» <white>" + stringBuilder));
        commandSender.sendMessage(mm.deserialize("<white>ЛС <dark_gray>| <white>Я <gray>> <white><bold>" + receiver.getName() + "</bold> <dark_gray>» <white>" + stringBuilder));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
