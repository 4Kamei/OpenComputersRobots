package ak.packet;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by Aleksander on 01:10, 08/07/2018.
 */
public class Packet {

    @SerializedName("type")
    public final PacketType type;
    @SerializedName("header")
    public final String[] header;
    @SerializedName("content-size")
    public final int contentSize;
    @SerializedName("content")
    public final String content;

    public Packet(Object content, String[] header, PacketType type) {
        if (content instanceof String) {
            this.content = (String) content;
        } else {
            this.content = new Gson().toJson(content);
        }

        this.contentSize = this.content.length();
        this.header = header;
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Packet{");
        sb.append("type=").append(type);
        sb.append(", header=").append(Arrays.toString(header));
        sb.append(", contentSize=").append(contentSize);
        sb.append(", content=").append(content);
        sb.append('}');
        return sb.toString();
    }

}
