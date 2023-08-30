package pb.lofe.chatl;

import com.google.common.collect.Lists;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import me.clip.placeholderapi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler public void handlePlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();


        if ((ChatL.i().getConfig().getBoolean("mentions.enabled_everyone") && ChatL.i().getConfig().getBoolean("mentions.enabled")) || ChatL.i().getConfig().getBoolean("replacements.enabled")){
            List<String> completions = Lists.newArrayList();
            completions.add("@everyone");
            List<String> replacements = ChatL.i().getConfig().getStringList("replacements.list");

            for (String replacement : replacements) {
                String[] args = replacement.split(";");
                if (args.length == 2)
                    completions.add(args[0]);
            }
            player.addCustomChatCompletions(completions);
        }


        String result = ChatL.i().getConfig().getString("messages.player.join");
        if(result == null)
            event.setJoinMessage(null);
        else {
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
                result = PlaceholderAPI.setPlaceholders(player, result);

            event.joinMessage(mm.deserialize(result.replaceAll("@PLAYER", player.getName())));
        }
    }

    @EventHandler public void handlePlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        String result = ChatL.i().getConfig().getString("messages.player.quit");
        if(result == null)
            event.setQuitMessage(null);
        else {
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
                result = PlaceholderAPI.setPlaceholders(player, result);

            event.quitMessage(mm.deserialize(result.replaceAll("@PLAYER", player.getName())));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR) public void handleAsyncChat(AsyncChatEvent event) {

        if(event.isCancelled())
            return;

        event.setCancelled(true);

        Player sender = event.getPlayer();

        List<Player> receivers = new ArrayList<>();

        boolean isGlobal;
        if(mm.serialize(event.message()).toCharArray()[0] == '!') {
            receivers = List.copyOf(Bukkit.getOnlinePlayers());
            isGlobal = true;
        }
        else {
            isGlobal = false;
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.getWorld().equals(sender.getWorld())) {
                    double distance = Math.sqrt(Math.pow(player.getLocation().getX() - sender.getLocation().getX(), 2) + Math.pow(player.getLocation().getZ() - sender.getLocation().getZ(), 2));
                    int radius = ChatL.i().getConfig().getInt("local_radius", 100);
                    if(radius == 0) {
                        receivers = List.copyOf(Bukkit.getOnlinePlayers());
                        isGlobal = true;
                        isLastGlobal = true;
                        break;
                    }
                    else if (distance < ChatL.i().getConfig().getInt("local_radius", 100))
                        receivers.add(player);
                }
            }
        }



        for(Player player : receivers) {

            String structure;

            if (isGlobal)
                structure = ChatL.i().getConfig().getString("global_structure", "<#PLAYER> #MESSAGE");
            else
                structure = ChatL.i().getConfig().getString("local_structure", "<#PLAYER> #MESSAGE");

            List<String> replacements = ChatL.i().getConfig().getStringList("replacements.list");

            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
                structure = PlaceholderAPI.setPlaceholders(sender, structure);
            StringBuilder parseMessage = new StringBuilder(mm.escapeTags(mm.serialize(event.message())));

            if (isGlobal && parseMessage.toString().equalsIgnoreCase("!")) {
                return;
            }



            while (parseMessage.charAt(0) == ' ') {
                parseMessage.deleteCharAt(0);
            }

            while (parseMessage.charAt(parseMessage.length() - 1) == ' ') {
                parseMessage.deleteCharAt(parseMessage.length() - 1);
            }

            if(isGlobal && parseMessage.charAt(0) == '!')
                parseMessage.deleteCharAt(0);


            structure = structure.replaceAll("#MESSAGE", mm.escapeTags(parseMessage.toString()));

            Pattern pattern = Pattern.compile("(?<prefix>.*?)(?<url>https?://\\S+)(?<suffix>.*)", Pattern.CASE_INSENSITIVE);
            Matcher m = pattern.matcher(structure);

            if(m.find())
                structure = m.group("prefix") + "<click:open_url:'" + m.group("url") + "'><#7C98FB><hover:show_text:'<blue>Открыть'><underlined>" + m.group("url") + "<reset>" + m.group("suffix");
            else
                structure = structure.replaceAll("#MESSAGE", mm.serialize(event.message()));


            if (ChatL.i().getConfig().getBoolean("replacements.enabled")) {
                for (String replacement : replacements) {
                    String[] args = replacement.split(";");
                    if (args.length == 2)
                        structure = structure.replaceAll(args[0], args[1]);
                }
            }

            if(ChatL.i().getConfig().getBoolean("mentions.enabled") && isGlobal) {
                if (mm.serialize(event.message()).contains("@everyone") && sender.hasPermission("simplechatl.ping") && ChatL.i().getConfig().getBoolean("mentions.enabled_everyone")) {
                     structure = structure.replaceAll("@everyone", ChatL.i().getConfig().getString("mentions.everyone_style", "<gradient:#FBF100:#FD8900><bold>@everyone</bold></gradient>"));
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_FALL, 100, 1);
                }

                if (mm.serialize(event.message()).contains("@" + player.getName())) {
                    structure = structure.replaceAll("@" + player.getName(), ChatL.i().getConfig().getString("mentions.player_style", "<gradient:green:blue><bold>@%PLAYER%</bold></gradient>").replaceAll("%PLAYER%", player.getName()));
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 100, 1);
                }
            }

            structure = structure.replaceAll("#PLAYER", "<click:suggest_command:'@" + sender.getName() + ", '><hover:show_text:'" + ChatL.i().getConfig().getString("hover_text." + sender.getName(), "") +"'>" + sender.getName() + "</hover></click>");

            player.sendMessage(mm.deserialize(structure));
        }
        Bukkit.getConsoleSender().sendMessage(Component.text("<" + sender.getName() + "> ").append(event.message()));
        if(receivers.size() == 1 && !isGlobal)
            sender.sendActionBar(mm.deserialize(ChatL.i().getConfig().getString("nobody_heard_you", "<dark_grey>[Nobody heard you]")));
    }

    boolean isLastGlobal = false;

}
