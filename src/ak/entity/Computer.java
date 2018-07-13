package ak.entity;

import ak.packet.Packet;
import ak.packet.PacketType;
import ak.util.Logger;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayDeque;

/**
 * Created by Aleksander on 15:57, 11/07/2018.
 */
public class Computer {

    private Socket s;
    private RobotState state;
    private String address;
    private BufferedReader input;
    private ArrayDeque<Packet> packets;
    private String name;


    public Computer(String address, Socket s) throws IOException {
        this.address = address;
        this.s = s;
        this.state = RobotState.POST_INIT_STATE;
        //TODO SOMETHING WITH POST_INIT?
        this.state = RobotState.IDLE;

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
        Logger.log("Updating server " + address);
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

    public boolean hasPackets() throws IOException {
        while (true) {
            Packet p = receivePacket(100);
            if (p == null) return packets.size() > 0;
            packets.add(p);
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
