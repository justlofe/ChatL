package pr.lofe.mdr.xchat.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

public class MessageConverter {

    public static String toJson(@NotNull DataType T, @NotNull String message, @Nullable String sender, @Nullable Boolean senderPermissioned, @Nullable MessageType messageT, @Nullable String receiver, @Nullable String sound) {
        JSONObject json = new JSONObject();
        json.put("dataT", T.toByte);
        json.put("message", message);
        if(sender != null)
            json.put("sender", sender);
        if(receiver != null)
            json.put("receiver", receiver);
        if(messageT != null)
            json.put("messageT", String.valueOf(messageT.toByte));
        if(senderPermissioned != null)
            json.put("perm", senderPermissioned);
        if(sound != null)
            json.put("sound", sound);
        return json.toJSONString();
    }

}
