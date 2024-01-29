package pr.lofe.mdr.xchat.commands;

import com.google.common.collect.Lists;
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
import pr.lofe.mdr.api.util.SortUtil;
import pr.lofe.mdr.xchat.xChat;
import pr.lofe.mdr.xsettings.api.chat.ChatSettings;

import java.util.List;

public class IgnoreCommand implements CommandExecutor, TabCompleter {

    MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (strings.length >= 1) {
                switch (s.toLowerCase()) {
                    case "ignore": {
                        Player target = Bukkit.getPlayer(strings[0]);
                        if(target == null) {
                            player.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(player, xChat.i().getConfig().getString("pm.not_found", ""))));
                            player.playSound(player, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                            return true;
                        }
                        else if (!ChatSettings.isIgnored(player, strings[0])) {
                            ChatSettings.setIgnored(player, strings[0], true);
                            player.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(player, xChat.i().getConfig().getString("messages.now_ignoring").replaceAll("#PLAYER", target.getName()))));
                            player.playSound(player, "custom.sfx.sucess", SoundCategory.MASTER, 100, 1);
                            return true;
                        }
                        else {
                            player.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(player, xChat.i().getConfig().getString("messages.already_ignoring", ""))));
                            player.playSound(player, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                            return true;
                        }
                    }
                    case "unignore": {
                        if (ChatSettings.isIgnored(player, strings[0])) {
                            ChatSettings.setIgnored(player, strings[0], false);
                            player.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(player, xChat.i().getConfig().getString("messages.no_longer_ignoring").replaceAll("#PLAYER", strings[0]))));
                            player.playSound(player, "custom.sfx.sucess", SoundCategory.MASTER, 100, 1);
                            return true;
                        }
                        else {
                            player.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(player, xChat.i().getConfig().getString("messages.not_ignoring", ""))));
                            player.playSound(player, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                            return true;
                        }

                    }
                    default:
                        return true;
                }
            }

        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (s.equalsIgnoreCase("ignore")) {
                List<String> names = Lists.newArrayList();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (ChatSettings.isIgnored(player, p.getName()))
                        names.add(p.getName());
                }
                return SortUtil.search(names, strings[0]);
            }
            else if (s.equalsIgnoreCase("unignore")) {
                return SortUtil.search(ChatSettings.ignoringList(player), strings[0]);
            }
        }
        return Lists.newArrayList("");
    }
}
