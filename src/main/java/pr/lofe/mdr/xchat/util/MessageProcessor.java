package pr.lofe.mdr.xchat.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import pr.lofe.mdr.xchat.messaging.DataType;
import pr.lofe.mdr.xchat.messaging.MessageConverter;
import pr.lofe.mdr.xchat.messaging.MessageType;
import pr.lofe.mdr.xchat.messaging.MultiServerService;
import pr.lofe.mdr.xchat.notify.Notifies;
import pr.lofe.mdr.xchat.xChat;
import pr.lofe.mdr.xsettings.api.chat.ChatSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageProcessor {

    private static final String URL_WITH_TEXT_FORMAT = "#PREFIX<click:open_url:'#URL'><hover:show_text:'#URL'><#7C98FB><underlined>#TEXT</underlined></#7C98FB></hover></click>#SUFFIX";
    private static final String URL_FORMAT = "#PREFIX<click:open_url:'#URL'><hover:show_text:'#URL'><#7C98FB><underlined>#URL</underlined></#7C98FB></hover></click>#SUFFIX";

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static void processMessage(@NotNull Player sender, MessageType T, String message, @Nullable String receiver) {
        switch (T) {
            case DEFAULT -> {
                List<Player> receivers = new ArrayList<>();

                boolean isGlobal;
                if(message.toCharArray()[0] == '!') {
                    for(Player player : Bukkit.getOnlinePlayers())
                        if(ChatSettings.globalEnabled().getValue(player) && !ChatSettings.isIgnored(player, sender.getName()))
                            receivers.add(player);
                    isGlobal = true;
                }
                else {
                    isGlobal = false;
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(player.getWorld().equals(sender.getWorld()) && !ChatSettings.isIgnored(player, sender.getName())) {
                            double distance = Math.sqrt(Math.pow(player.getLocation().getX() - sender.getLocation().getX(), 2) + Math.pow(player.getLocation().getZ() - sender.getLocation().getZ(), 2));
                            int radius = xChat.i().getConfig().getInt("local_radius", 100);
                            if(radius <= 0) {
                                receivers = List.copyOf(Bukkit.getOnlinePlayers());
                                isGlobal = radius == 0;
                                break;
                            }
                            else if (distance < xChat.i().getConfig().getInt("local_radius", 100))
                                receivers.add(player);
                        }
                    }
                }


                if(isGlobal) {
                    if(!ChatSettings.globalEnabled().getValue(sender)) {
                        sender.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(sender, xChat.i().getConfig().getString("messages.global_disabled"))));
                        sender.playSound(sender, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                        return;
                    }

                    StringBuilder parseMessage = new StringBuilder(mm.escapeTags(message));
                    if(parseMessage.charAt(0) == '!')
                        parseMessage.deleteCharAt(0);
                    while (parseMessage.charAt(0) == ' ')
                        parseMessage.deleteCharAt(0);

                    if (Bukkit.getPluginManager().getPlugin("DiscordSRV") != null) {
                        // TODO
                    }
                }

                String structure;
                if (isGlobal) structure = xChat.i().getConfig().getString("global_structure", "<#PLAYER> #MESSAGE");
                else structure = xChat.i().getConfig().getString("local_structure", "<#PLAYER> #MESSAGE");
                List<String> replacements = xChat.i().getConfig().getStringList("replacements.list");
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) structure = PlaceholderAPI.setPlaceholders(sender, structure);
                StringBuilder parseMessage = new StringBuilder(message);

                if (isGlobal && parseMessage.toString().equalsIgnoreCase("!")) return;
                if (isGlobal && parseMessage.charAt(0) == '!')
                    parseMessage.deleteCharAt(0);
                while (parseMessage.charAt(0) == ' ')
                    parseMessage.deleteCharAt(0);

                structure = structure.replaceAll("#MESSAGE", mm.escapeTags(parseMessage.toString()));
                structure = structure.replaceAll("&loc", xChat.i().getConfig().getString("messages.location." + sender.getWorld().getName())
                        .replaceAll("#X", String.valueOf((int) sender.getLocation().getX()))
                        .replaceAll("#Y", String.valueOf((int) sender.getLocation().getY()))
                        .replaceAll("#Z", String.valueOf((int) sender.getLocation().getZ())));
                structure = sender.hasPermission("xchat.format") ? structure
                        .replaceAll("&l", "<bold>")
                        .replaceAll("&n", "<underlined>")
                        .replaceAll("&m", "<strikethrough>")
                        .replaceAll("&r", "<reset>") :
                        structure
                                .replaceAll("&l", "")
                                .replaceAll("&n", "")
                                .replaceAll("&m", "")
                                .replaceAll("&r", "");
                Pattern formattedUrl = Pattern.compile("(?<prefix>.*?)\\[(?<text>.*)\\]\\((?<url>.*)\\)(?<suffix>.*)", Pattern.CASE_INSENSITIVE);
                Matcher m1 = formattedUrl.matcher(structure);
                if (m1.find()) {
                    String prefix = m1.group("prefix"), url = m1.group("url"), text = m1.group("text"), suffix = m1.group("suffix");
                    structure = URL_WITH_TEXT_FORMAT
                            .replaceAll("#PREFIX", prefix)
                            .replaceAll("#URL", url)
                            .replaceAll("#TEXT", text)
                            .replaceAll("#SUFFIX", suffix);
                }
                else {
                    Pattern rawUrl = Pattern.compile("(?<prefix>.*?)(?<url>https?://\\S+)(?<suffix>.*)", Pattern.CASE_INSENSITIVE);
                    Matcher m2 = rawUrl.matcher(structure);
                    if (m2.find()) {
                        String prefix = m2.group("prefix"), url = m2.group("url"), suffix = m2.group("suffix");
                        structure = URL_FORMAT
                                .replaceAll("#PREFIX", prefix)
                                .replaceAll("#URL", url)
                                .replaceAll("#SUFFIX", suffix);
                    }
                }
                if (xChat.i().getConfig().getBoolean("replacements.enabled")) {
                    for (String replacement : replacements) {
                        String[] args = replacement.split(";");
                        if (args.length == 2)
                            structure = structure.replaceAll(args[0], args[1]);
                    }
                }

                if(isGlobal)
                    MultiServerService.wroteMessage(DataType.Player, MessageConverter.toJson(
                        DataType.Player,
                        structure,
                        sender.getName(),
                        sender.hasPermission("xchat.ping"),
                        MessageType.DEFAULT,
                        null,
                            null
                    ), sender);

                for(Player player : receivers) {
                    if (xChat.i().getConfig().getBoolean("mentions.enabled") && isGlobal) {
                        if (message.contains("@everyone") && sender.hasPermission("xchat.ping") && xChat.i().getConfig().getBoolean("mentions.enabled_everyone")  && ChatSettings.everyoneMentionEnabled().getValue(player)) {
                            structure = structure.replaceAll("@everyone", xChat.i().getConfig().getString("mentions.everyone_style", "<gradient:#FBF100:#FD8900><bold>@everyone</bold></gradient>"));
                            player.playSound(player.getLocation(), xChat.i().getConfig().getString("mentions.sound", "block.amethyst_block.hit"), SoundCategory.MASTER, 100, 1);
                            if(ChatSettings.notifiesEnabled().getValue(player))
                                Notifies.send(player, structure.replace("|", ""));
                        }

                        if (message.contains("@" + player.getName()) && ChatSettings.mentionEnabled().getValue(player)) {
                            structure = structure.replaceAll("@" + player.getName(), xChat.i().getConfig().getString("mentions.player_style", "<gradient:green:blue><bold>@%PLAYER%</bold></gradient>").replaceAll("%PLAYER%", player.getName()));
                            player.playSound(player.getLocation(), xChat.i().getConfig().getString("mentions.sound", "block.amethyst_block.hit"), SoundCategory.MASTER, 100, 1);
                            if(ChatSettings.notifiesEnabled().getValue(player))
                                Notifies.send(player, structure.replace("|", ""));
                        }
                    }

                    player.sendMessage(mm.deserialize(structure));
                }
            }
            case PRIVATE -> {
                Player receiverP = null;
                if(receiver != null)
                    receiverP = Bukkit.getPlayer(receiver);
                if(!ChatSettings.privateEnabled().getValue(sender)) {
                    sender.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(sender, xChat.i().getConfig().getString("pm.disabled", ""))));
                    sender.playSound(sender, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                    return;
                }

                if(receiver == null || receiverP == null) {
                    MultiServerService.wroteMessage(DataType.Player, MessageConverter.toJson(
                            DataType.Player,
                            message,
                            sender.getName(),
                            null,
                            MessageType.PRIVATE,
                            receiver,
                            null
                    ), sender);
                    return;
                }

                StringBuilder stringBuilder = new StringBuilder(message);
                xChat.i().lastMessage.put(sender.getName(), receiver);
                xChat.i().lastMessage.put(receiver, sender.getName());

                String senderFormat = xChat.i().getConfig().getString("pm.sender"),
                        receiverFormat = xChat.i().getConfig().getString("pm.receiver");
                senderFormat = senderFormat.replaceAll("#MESSAGE", stringBuilder.toString());
                receiverFormat = receiverFormat.replaceAll("#MESSAGE", stringBuilder.toString());


                if (!ChatSettings.privateEnabled().getValue(receiverP)){
                    sender.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(sender, xChat.i().getConfig().getString("pm.attempt_disabled", ""))));
                    sender.playSound(sender, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                }
                else if (ChatSettings.isIgnored(sender, receiver)) {
                    sender.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(sender, xChat.i().getConfig().getString("pm.ignoring", ""))));
                    sender.playSound(sender, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                }
                else if (ChatSettings.isIgnored(receiverP, sender.getName())) {
                    sender.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(sender, xChat.i().getConfig().getString("pm.ignores_you", ""))));
                    sender.playSound(sender, "custom.sfx.error", SoundCategory.MASTER, 100, 1);
                }
                else {
                    receiverP.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(receiverP, receiverFormat.replaceAll("#SENDER", sender.getName()))));

                    if(ChatSettings.notifiesEnabled().getValue(sender))
                        Notifies.send(receiverP, receiverFormat.replaceAll("#SENDER", sender.getName()));

                    sender.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(sender, senderFormat.replaceAll("#RECEIVER", receiver))));
                    receiverP.playSound(receiverP, Sound.BLOCK_AMETHYST_BLOCK_HIT, 100, 1);
                }

            }
            default -> throw new IllegalArgumentException();
        }
    }

    public static void receiveMessage(@NotNull JSONObject obj) {
        MessageType T = MessageType.fromByte(Integer.parseInt((String) obj.get("messageT")));
        if(T == null) throw new IllegalArgumentException();
        String sender = (String) obj.get("sender");
        String message = (String) obj.get("message");
        switch (T) {
            case DEFAULT -> {
                List<Player> receivers = new ArrayList<>();

                for(Player player : Bukkit.getOnlinePlayers())
                    if(ChatSettings.globalEnabled().getValue(player) && !ChatSettings.isIgnored(player, sender))
                        receivers.add(player);

                String structure = message;

                for(Player player : receivers) {
                    if (xChat.i().getConfig().getBoolean("mentions.enabled")) {
                        if (message.contains("@everyone") && (Boolean) obj.get("perm") && xChat.i().getConfig().getBoolean("mentions.enabled_everyone")  && ChatSettings.everyoneMentionEnabled().getValue(player)) {
                            structure = structure.replaceAll("@everyone", xChat.i().getConfig().getString("mentions.everyone_style", "<gradient:#FBF100:#FD8900><bold>@everyone</bold></gradient>"));
                            player.playSound(player.getLocation(), xChat.i().getConfig().getString("mentions.sound", "block.amethyst_block.hit"), SoundCategory.MASTER, 100, 1);
                            if(ChatSettings.notifiesEnabled().getValue(player))
                                Notifies.send(player, structure.replace("|", ""));
                        }

                        if (message.contains("@" + player.getName()) && ChatSettings.mentionEnabled().getValue(player)) {
                            structure = structure.replaceAll("@" + player.getName(), xChat.i().getConfig().getString("mentions.player_style", "<gradient:green:blue><bold>@%PLAYER%</bold></gradient>").replaceAll("%PLAYER%", player.getName()));
                            player.playSound(player.getLocation(), xChat.i().getConfig().getString("mentions.sound", "block.amethyst_block.hit"), SoundCategory.MASTER, 100, 1);
                            if(ChatSettings.notifiesEnabled().getValue(player))
                                Notifies.send(player, structure.replace("|", ""));
                        }
                    }

                    player.sendMessage(mm.deserialize(structure));
                }
            }
            case PRIVATE -> {
                String receiver = (String) obj.get("receiver");
                Player receiverP = Bukkit.getPlayer(receiver);

                if(receiverP == null) {
                    MultiServerService.wroteMessage(DataType.PluginResponse, MessageConverter.toJson(
                            DataType.PluginResponse,
                            xChat.i().getConfig().getString("pm.not_found", ""),
                            null,
                            null,
                            MessageType.PRIVATE,
                            sender,
                            "custom.sfx.error"
                    ), new ArrayList<>(Bukkit.getOnlinePlayers()).get(0));
                    return;
                }

                if (!ChatSettings.privateEnabled().getValue(receiverP)){
                    MultiServerService.wroteMessage(DataType.PluginResponse, MessageConverter.toJson(
                            DataType.PluginResponse,
                            xChat.i().getConfig().getString("pm.attempt_disabled", ""),
                            null,
                            null,
                            MessageType.PRIVATE,
                            sender,
                            "custom.sfx.error"
                    ), receiverP);
                }
                else if (ChatSettings.isIgnored(receiverP, sender)) {
                    MultiServerService.wroteMessage(DataType.PluginResponse, MessageConverter.toJson(
                            DataType.PluginResponse,
                            xChat.i().getConfig().getString("pm.ignores_you", ""),
                            null,
                            null,
                            MessageType.PRIVATE,
                            sender,
                            "custom.sfx.error"
                    ), receiverP);
                }
                else {
                    String senderFormat = xChat.i().getConfig().getString("pm.sender"),
                            receiverFormat = xChat.i().getConfig().getString("pm.receiver");
                    senderFormat = senderFormat.replaceAll("#MESSAGE", message);
                    receiverFormat = receiverFormat.replaceAll("#MESSAGE", message);

                    receiverP.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(receiverP, receiverFormat.replaceAll("#SENDER", sender))));

                    if(ChatSettings.notifiesEnabled().getValue(receiverP))
                        Notifies.send(receiverP, receiverFormat.replaceAll("#SENDER", sender));

                    MultiServerService.wroteMessage(DataType.PluginResponse, MessageConverter.toJson(
                            DataType.PluginResponse,
                            senderFormat.replaceAll("#RECEIVER", receiver),
                            null,
                            null,
                            MessageType.PRIVATE,
                            sender,
                            "block.amethyst_block.hit"
                    ), receiverP);

                    xChat.i().lastMessage.put(receiver, sender);
                    xChat.i().lastMessage.put(sender, receiver);
                }
            }
            default -> throw new IllegalArgumentException();
        }
    }

}
