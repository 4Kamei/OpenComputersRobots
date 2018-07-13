package ak;

import ak.packet.Packet;
import ak.packet.PacketType;
import ak.packet.content.CommandPacket;
import ak.packet.content.InitPacket;
import ak.packet.content.WorkPacketComplete;
import ak.project.MineProject;
import ak.project.PartialProjectData;
import ak.project.Project;
import ak.entity.Computer;
import ak.entity.Robot;
import ak.entity.RobotState;
import ak.util.Logger;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ak.packet.PacketType.*;

/**
 * Created by Aleksander on 14:11, 09/07/2018.
 */
public class OperationManager implements Runnable {

    private Gson g;
    private ConcurrentHashMap<Integer, Robot> robots;
    private ConcurrentHashMap<String, Integer> addresses;
    private List<Socket> pre_init;

    private ConcurrentHashMap<Integer, Project> projects;
    private ConcurrentHashMap<Integer, PartialProjectData> partialProjects;

    private int nextAddress = 0;
    private int nextProjectID = 0;
    private Computer server;

    private boolean initRunning = false;

    public OperationManager() {
        g = new Gson();
        robots = new ConcurrentHashMap<>();
        addresses = new ConcurrentHashMap<>();
        pre_init = Collections.synchronizedList(new ArrayList<>());
        projects = new ConcurrentHashMap<>();
        partialProjects = new ConcurrentHashMap<>();
    }

    public void addPreInit(Socket s) {
        Logger.log("Added pre-init socket " + s);
        pre_init.add(s);
        Logger.log(this.toString());
    }

    private boolean needsInit() {
        return pre_init.size() > 0;
    }

    private void addRobot(Robot r) {
        if (robots.containsKey(r.getAddress())) {
            Logger.log("This must be an error? Already contains key " + r.getAddress());
        }
        addresses.put(r.getAddress(), nextAddress);
        robots.put(nextAddress, r);
        nextAddress++;
    }

    private void initDevice(Socket s) throws IOException {
        Logger.log("Initializing socket " + s);
        BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
        while (!r.ready());
        String data = r.readLine();
        Packet p = g.fromJson(data, Packet.class);
        if (p.type != S_INIT) {

            Packet reject = new Packet("", null, C_DENY_INIT);
            String cont = g.toJson(reject) + "\n";
            s.getOutputStream().write(cont.getBytes());
            Logger.log("Got malformed packet");

        } else {

            InitPacket cont = g.fromJson(p.content, InitPacket.class);
            Logger.log("Got init packet from address " + cont.getAddress());
            if (cont.getType().equals("worker")) {
                Robot robot = new Robot(cont.getAddress(), s, cont.getX(), cont.getY(), cont.getZ());


                Packet accept = new Packet("", null, C_ACCEPT_INIT);
                String message = g.toJson(accept) + "\n";
                Logger.log("Sending " + message);
                robot.sendPacket(accept);
                Packet reply = robot.receivePacket(1000);
                switch (reply.type) {
                    case S_ACCEPT_INIT : {
                        Logger.log("Accepted robot");
                        addRobot(robot);
                        break;
                    }
                    case S_DENY_INIT: {
                        Logger.log("Denied robot");
                        break;
                    }
                    default: {
                        Logger.log("Robot send packet " + reply.type);
                        break;
                    }
                }
            } else if (cont.getType().equals("server")) {
                if (server == null) {
                    server = new Computer(cont.getAddress(), s);


                    Packet accept = new Packet("", null, C_ACCEPT_INIT);
                    server.sendPacket(accept);
                    Packet reply = server.receivePacket(1000);

                    if (reply.type != PacketType.S_ACCEPT_INIT) {
                        Logger.log("Server send packet " + reply.type);
                        return;
                    }
                } else {
                    Logger.log("Could not init");
                    Packet deny = new Packet("", null, C_DENY_INIT);
                    new Computer(cont.getAddress(), s).sendPacket(deny);
                    pre_init.remove(s);
                    return;
                }
            }
        }

        pre_init.remove(s);
    }

    public void initializeAll() {
        Logger.log("InitAll Called");
        for (int i = pre_init.size() - 1; i >= 0; i--) {
            try {
                initDevice(pre_init.get(i));
            } catch (IOException e) {
                e.printStackTrace();
                initRunning = false;
            }
        }
        initRunning = false;
    }

    private void sendPacket(Integer id, Packet content) {
        Robot r = robots.get(id);
        r.sendPacket(content);
    }

    //server loop
    @Override
    public void run() {
        Logger.log("Started robot manager");
        Logger.log(this.toString());
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        long startT = 0;
        long target = 50;
        while (true) {
            startT = System.currentTimeMillis();
            handleCommands(input);
            updateProjects();
            updateRobots();
            updateServer();


            try {
                long t = System.currentTimeMillis() - startT;
                if (t < target) {
                    Thread.sleep(target - t);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateServer() {
        if (server == null)
            return;
        try {
            if (!server.hasPackets())
                return;
        } catch (IOException e) {
            e.printStackTrace();
            server = null;
            Logger.log("Removed server");
            return;
        }
        while (!server.queueEmpty()) {
            Packet p = server.poll();
            if (p.type == null) {
                Logger.log("Unknown packet type " + p.toString());
                return;
            }
            switch (p.type) {
                case S_CLOSE: {
                    server = null;
                    Logger.log("Removing server");
                    return;
                }
                case S_MESSAGE: {
                    System.out.println(p);
                    Logger.log(server.getAddress().substring(0, 8) + " -> " + p.content);
                    break;
                }
                case S_NEW_PROJECT: {
                    Logger.log(p.toString());
                    PartialProjectData data = g.fromJson(p.content, PartialProjectData.class);
                    Project project = data.getProject(nextProjectID++);
                    projects.put(project.getID(), project);
                    break;
                }
                default: {
                    Logger.log("Unknown type " + p.type);
                }
            }
        }
    }

    private int getNextProjectID() {
        return nextProjectID++;
    }

    private void updateRobots() {
        if (needsInit()) {
            initializeAll();
        }

        robots.forEach((k, v) -> {
            if (v.hasPackets()) {
                while (!v.queueEmpty()) {
                    Packet p = v.poll();
                    handlePacket(k, p);
                }
            }
        });
    }

    private void handlePacket(Integer k, Packet p) {
        Logger.log("Received packet from robot " + k + " with type " + p.type);

        if (p.type == PacketType.S_MESSAGE) {
            Logger.log(String.format("[%d] -> %s", k, p.content));
        } else if (p.type == PacketType.S_WORK_PACKET_COMPLETE) {
            WorkPacketComplete complete = g.fromJson(p.content, WorkPacketComplete.class);
            System.out.println(complete);
            MineProject proj = (MineProject) projects.get(complete.getProjID());
            proj.workFinished(complete.getWorkID());
        }
    }

    private void handleCommand(String command) {
        Logger.log("Parsing command " + command);
        if (command.startsWith("init"))
            commandInit();
        if (command.startsWith("update"))
            commandUpdate();
        if (command.startsWith("project")) {
            String[] args = command.split(" ");
            if (args.length == 1) {
                Logger.log("No project Specified");
                return;
            }
            if (args[1].equals("mine")) {
                if (args.length < 8) {
                    Logger.log("Not enough parameters for mine project");
                    Logger.log("Usage: project mine <x0> <y0> <z0> <x1> <y1> <z1>");
                    return;
                }
                int x0, y0, z0, x1, y1, z1;
                try {
                    x0 = Integer.parseInt(args[2]);
                    y0 = Integer.parseInt(args[3]);
                    z0 = Integer.parseInt(args[4]);
                    x1 = Integer.parseInt(args[5]);
                    y1 = Integer.parseInt(args[6]);
                    z1 = Integer.parseInt(args[7]);
                } catch (NumberFormatException e) {
                    Logger.log("coordinates must be integers");
                    return;
                }


                MineProject project = new MineProject(getNextProjectID(), x0, y0, z0, x1, y1, z1);
                projects.put(project.getID(), project);

                Logger.log("Project created with ID " + project.getID());

            } else if (args[1].equals("assign")) {
                if (args.length < 4) {
                    Logger.log("Usage: project assign <project id> <robot 1>|all [<robot 2> <robot 3> ...]");
                    return;
                }
                int projID;
                try {
                    projID = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    Logger.log("IDs must be integers");
                    return;
                }
                if (!projects.containsKey(new Integer(projID))) {
                    Logger.log("Could not find project with ID " + projID);
                    return;
                }

                Project p = projects.get(projID);
                ArrayList<Integer> robotKeys = new ArrayList<>();
                if (args[3].equals("all")) {
                    robots.forEach((k, v) -> {
                        if (v.isFree())
                            robotKeys.add(k);
                    });
                } else {
                    try {
                        for (int i = 3; i < args.length; i++) {
                            robotKeys.add(Integer.parseInt(args[i]));
                        }
                    } catch (NumberFormatException e) {
                        Logger.log("Robot IDs must be integers");
                    }
                }
                Logger.log("Adding keys");
                for (Integer robot : robotKeys) {
                    if (!this.robots.containsKey(robot)) {
                        Logger.log("Unknown key " + robot);
                        return;
                    }
                    Robot r = this.robots.get(robot);
                    Logger.log("Added robot " + robot + " to project " + projID);
                    p.addWorker(r);
                }

            } else if (args[1].equals("start")) {
                if (args.length < 2) {
                    Logger.log("Usage: project start <project id>");
                    return;
                }
                int projID;
                try {
                    projID = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    Logger.log("IDs must be integers");
                    return;
                }
                if (!projects.containsKey(projID)) {
                    Logger.log("Could not find project with ID " + projID);
                    return;
                }
                Project p = projects.get(projID);
                p.start();
            } else if (args[1].equals("list")) {
                for (Integer integer : projects.keySet()) {
                    Logger.log(integer + " -> " + projects.get(integer));
                }
            } else if (args[1].equals("stop")) {
                if (args.length < 2) {
                    Logger.log("Usage: project stop <project id>");
                    return;
                }
                int projID;
                try {
                    projID = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    Logger.log("IDs must be integers");
                    return;
                }
                if (!projects.containsKey(projID)) {
                    Logger.log("Could not find project with ID " + projID);
                    return;
                }
                Project p = projects.get(projID);
                Logger.log("Stopping project");
                p.stop();
            }
        }
        if (command.startsWith("send")) {
            try {
                String[] args = command.split(" ");
                int id = Integer.parseInt(args[1]);

                if (addresses.containsValue(new Integer(id))) {
                    Logger.log("Sending packet to robot " + id);

                    if (args.length < 4) {
                        Logger.log("No action");
                    } else {
                        String action = args[2];
                        String data = args[3];
                        if (action.equals("command")) {
                            CommandPacket cont = new CommandPacket(data, Arrays.copyOfRange(args, 4, args.length));
                            Packet p = new Packet(g.toJson(cont), null, SC_COMMAND_PACKET);
                            sendPacket(id, p);
                        }
                    }

                } else {
                    Logger.log("Unknown robot id " + id);
                }

            } catch (Exception e) {
                Logger.log(e.toString());
            }
        }
        if (command.startsWith("list")) {
            if (addresses.isEmpty()) {
                Logger.log("No robots");
            } else {
                Logger.log(addresses.size() + " active robots");
            }
            if (pre_init.isEmpty()) {
                Logger.log("No pre-init robots");
            } else {
                Logger.log(pre_init.size() + " require initialization");
            }
            if (server != null) {
                Logger.log("Server is " + server.getAddress());
            } else {
                Logger.log("No server");
            }
            addresses.forEach((k, v) -> Logger.log(v + " -> " + k + " STATE = " + robots.get(v).getState()));
        }
    }

    private void handleCommands(BufferedReader input) {
        try {
            while (input.ready()) {
                handleCommand(input.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProjects() {
        projects.values().forEach(Project::update);
        ArrayList<Integer> toRemove = new ArrayList<>();
        projects.forEach((k, p) -> {
            if (p.isEnded())
                toRemove.add(k);
        });

        for (Integer key : toRemove) {
            Project p = projects.get(key);
            Logger.log("Removing project id " + key + " type " + p.getClass().getSimpleName());
            projects.remove(key);
        }
    }

    private void commandUpdate() {
        ArrayList<Integer> toRemove = new ArrayList<>();
        robots.forEach((id, robot) -> {
            try {
                Logger.log("Updating robot " + id);
                robot.update(200);
                if (robot.getState() == RobotState.CLOSED) {
                    Logger.log("Robot with id " + id + " has been removed");
                    toRemove.add(id);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        for (Integer key : toRemove) {
            robots.remove(key);
            addresses.values().remove(key);
        }
    }


    private void commandInit() {
        Logger.log("Initrunning? " + initRunning);
        if (!initRunning && pre_init.size() > 0) {
            initRunning = true;
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    initializeAll();
                }
            });
            th.start();
        }
    }


}
