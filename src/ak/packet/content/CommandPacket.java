package ak.packet.content;

import java.util.Arrays;

/**
 * Created by Aleksander on 18:25, 09/07/2018.
 */
public class CommandPacket {
    private String command;
    private String[] args;

    public CommandPacket(String command, String... args) {
        this.command = command;
        this.args = args;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommandPacket{");
        sb.append("args=").append(Arrays.toString(args));
        sb.append(", command='").append(command).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
