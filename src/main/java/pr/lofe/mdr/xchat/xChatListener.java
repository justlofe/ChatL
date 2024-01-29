package pr.lofe.mdr.xchat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import me.clip.placeholderapi.*;
import pr.lofe.mdr.engine.util.ObjectUtils;
import pr.lofe.mdr.xchat.messaging.MessageType;
import pr.lofe.mdr.xchat.util.MessageProcessor;

import java.util.*;

public class xChatListener implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler public void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String result = player.hasPlayedBefore() ? xChat.i().getConfig().getString("messages.player.join") : xChat.i().getConfig().getString("messages.player.first-join");
        if(result == null)
            event.setJoinMessage(null);
        else {
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
                result = PlaceholderAPI.setPlaceholders(player, result);

            if(!player.hasPlayedBefore())
                Bukkit.getOnlinePlayers().forEach(player1 -> player1.playSound(player1, Sound.BLOCK_NOTE_BLOCK_BELL, 100, 2));
            event.joinMessage(mm.deserialize(result.replaceAll("#PLAYER", player.getName())));
        }
    }

    private final HashMap<UUID, Date> lastHubCommand = new HashMap<>();

    @EventHandler public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
        if(!event.isCancelled()) {
            String first = event.getMessage();
            if(first.equalsIgnoreCase("/hub") || first.equalsIgnoreCase("/lobby"))
                lastHubCommand.put(event.getPlayer().getUniqueId(), new Date());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) public void handlePlayerDeath(PlayerDeathEvent event) {
        if(!event.isCancelled() && xChat.i().getConfig().getBoolean("death_messages.local", false)) {
            Player sender = event.getPlayer();
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(player.getWorld().equals(sender.getWorld())) {
                    double distance = Math.sqrt(Math.pow(player.getLocation().getX() - sender.getLocation().getX(), 2) + Math.pow(player.getLocation().getZ() - sender.getLocation().getZ(), 2));
                    int radius = xChat.i().getConfig().getInt("death_messages.radius", 100);
                    if (distance < radius)
                        player.sendMessage((Component) ObjectUtils.notNull(event.deathMessage(), Component.text("")));
                }
            }
            event.setDeathMessage(null);
        }
    }

    @EventHandler public void handlePlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String result = xChat.i().getConfig().getString("messages.player.quit.def");

        if(event.getReason() != PlayerQuitEvent.QuitReason.DISCONNECTED)
            result = xChat.i().getConfig().getString("messages.player.quit.error");
        else {
            Date date = lastHubCommand.remove(player.getUniqueId());
            if(date != null && new Date().getTime() - date.getTime() < 5000)
                result = xChat.i().getConfig().getString("messages.player.quit.hub");
        }

        if (result == null)
            event.setQuitMessage(null);
        else {
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
                result = PlaceholderAPI.setPlaceholders(player, result);
            event.quitMessage(mm.deserialize(result.replaceAll("#PLAYER", player.getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) public void handleAsyncChat(AsyncChatEvent event) {
        if(event.isCancelled()) return;
        event.setCancelled(true);

        Player sender = event.getPlayer();
        MessageProcessor.processMessage(sender, MessageType.DEFAULT, mm.serialize(event.message()), null);
        Bukkit.getConsoleSender().sendMessage(Component.text("<" + sender.getName() + "> ").append(event.message()));
    }

}