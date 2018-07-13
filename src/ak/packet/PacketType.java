package ak.packet;

/**
 * Created by Aleksander on 03:49, 08/07/2018.
 */
public enum PacketType {
    S_MESSAGE,
    SC_REQUEST,
    S_INIT,
    C_DENY_INIT, C_ACCEPT_INIT,
    S_ACCEPT_INIT, S_DENY_INIT,
    S_CLOSE,
    SC_COMMAND_PACKET, SC_COMMAND_POLL,
    C_WORK_PACKET, S_WORK_PACKET_COMPLETE,

    C_NEW_PROJECT_ACCEPT, S_NEW_PROJECT

}
