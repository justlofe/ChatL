package pr.lofe.mdr.xchat.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pr.lofe.mdr.xchat.util.MessageProcessor;
import pr.lofe.mdr.xchat.xChat;

import java.io.*;

public class MultiServerService implements PluginMessageListener {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    @Override public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if(!channel.equals("BungeeCord")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));


        if (subChannel.equals("ChatPlayer")) {
            try {
                String data = msgin.readUTF();
                MessageProcessor.receiveMessage((JSONObject) new JSONParser().parse(data));
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        else if (subChannel.equals("ChatPluginResponse")) {
            JSONObject obj;
            try {
                String data = msgin.readUTF();
                obj = (JSONObject) new JSONParser().parse(data);
            } catch (ParseException | IOException e) {
                throw new RuntimeException(e);
            }

            if(obj != null && MessageType.fromByte(Integer.parseInt((String) obj.get("messageT"))) == MessageType.PRIVATE) {
                String receiver = (String) obj.get("receiver");
                Player receiverP = Bukkit.getPlayer(receiver);
                if(receiverP != null) {
                    String msg = (String) obj.get("message");
                    String sound = (String) obj.get("sound");
                    if(msg != null)
                        receiverP.sendMessage(mm.deserialize(PlaceholderAPI.setPlaceholders(receiverP, msg)));
                    if(sound != null)
                        receiverP.playSound(receiverP, sound, 1F, 1);
                }
            }
        }
    }

    public static void wroteMessage(DataType type, String data, @Nullable Player sender) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("Chat" + type.name());

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF(data);
        } catch (IOException exception){
            exception.printStackTrace();
        }

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        if(sender != null)
            sender.sendPluginMessage(xChat.i(), "BungeeCord", out.toByteArray());
    }

}
