package ak.entity;

import ak.util.Logger;
import ak.packet.Packet;
import ak.packet.PacketType;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayDeque;

/**
 * Created by Aleksander on 14:11, 09/07/2018.
 */
public class Robot {

    private Socket s;
    private RobotState state;
    private int x, y, z;
    private String address;
    private BufferedReader input;
    private ArrayDeque<Packet> packets;

    public Robot(String address, Socket s, float x, float y, float z) throws IOException {
        try {
            s.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.address = address;
        this.s = s;
        this.state = RobotState.POST_INIT_STATE;
        //TODO SOMETHING WITH POST_INIT?
        this.state = RobotState.IDLE;
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
        input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        packets = new ArrayDeque<>();
    }

    public void startWork() {
        this.state = RobotState.WORKING;
    }

    public boolean isFree() {
        return state == RobotState.IDLE;
    }

    public void sendPacket(Packet p) {
        String message = new Gson().toJson(p);
        Logger.log("Sending " + p.type);
        send(message.getBytes());
    }

    public Packet receivePacket(long timeout) throws IOException {
        long t = System.currentTimeMillis();
        while (!input.ready()) {
            if(t + timeout < System.currentTimeMillis()) {
                return null;
            }
        }
        String data = input.readLine();
        Packet p = new Gson().fromJson(data, Packet.class);
        return p;
    }

    public void update(long timeout) throws IOException {
        Logger.log("Updating robot " + address);
        if (s.isClosed()) {
            this.state = RobotState.CLOSED;
            return;
        }

        Packet p = new Packet("", null, PacketType.SC_COMMAND_POLL);
        sendPacket(p);
        p = receivePacket(timeout);
        if (p == null) {
            this.state = RobotState.CLOSED;
            return;
        }
        if (p.type != PacketType.SC_COMMAND_POLL) {
            Logger.log("Got response to poll but with wrong type? " + p.type);
        }
    }


    public String getAddress() {
        return address;
    }

    public RobotState getState() {
        return state;
    }

    private void send(byte[] bytes) {
        try {
            s.getOutputStream().write(bytes);
        } catch (IOException e) {
            Logger.log(e.toString());
        }
    }

    public boolean hasPackets() {
        while (true) {
            try {
                Packet p = receivePacket(5);
                if (p == null)
                    return packets.size() > 0;
                packets.push(p);
            } catch (IOException e) {
                return packets.size() > 0;
            }
        }
    }

    public boolean queueEmpty() {
        return packets.isEmpty();
    }

    public Packet poll() {
        return packets.poll();
    }

    public void free() {
        state = RobotState.IDLE;
    }

}
