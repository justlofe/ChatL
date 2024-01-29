package pr.lofe.mdr.xchat.messaging;

public enum DataType {

    PluginResponse(0),
    Player(1);

    public final int toByte;

    DataType(int i) {
        toByte = i;
    }

    public static DataType fromByte(int byteI) {
        for(DataType type : DataType.values())
            if(type.toByte == byteI)
                return type;
        return null;
    }
}
