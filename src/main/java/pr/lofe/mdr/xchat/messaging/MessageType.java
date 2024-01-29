package pr.lofe.mdr.xchat.messaging;

public enum MessageType {
    DEFAULT(0),
    PRIVATE(1);

    public final int toByte;

    MessageType(int i) {
        toByte = i;
    }

    public static MessageType fromByte(int byteI) {
        for(MessageType type : MessageType.values())
            if(type.toByte == byteI)
                return type;
        return null;
    }
}
