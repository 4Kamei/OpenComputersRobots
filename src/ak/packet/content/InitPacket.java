package ak.packet.content;

/**
 * Created by Aleksander on 23:45, 08/07/2018.
 */
public class InitPacket {

    private final float x;
    private final float y;
    private final float z;
    private final String address;
    private final String type;

    public InitPacket(String address, float z, float y, float x, String type) {
        this.address = address;
        this.z = z;
        this.y = y;
        this.x = x;
        this.type = type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InitPacket{");
        sb.append("x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", z=").append(z);
        sb.append(", address='").append(address).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getType() {
        return type;
    }
}
