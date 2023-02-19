package fr.emevel.locallink;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.Packet;
import fr.emevel.locallink.network.packets.PacketHandShake;
import fr.emevel.locallink.network.serial.PacketParsingException;
import fr.emevel.locallink.network.server.NetworkServer;

import java.io.IOException;
import java.net.Socket;

public class LocalLinkServer extends NetworkServer {

    public LocalLinkServer(int port) throws IOException {
        super(port);
    }

    @Override
    protected LinkSocket createClient(Socket sock) throws IOException {
        return new LinkSocket(sock) {
            @Override
            protected void onPacketReceived(Packet packet) throws IOException {
                System.out.println("Received packet " + packet);
                if (packet instanceof PacketHandShake) {
                    try {
                        sendPacket(new PacketHandShake("LocalLink Server", "1.0"));
                    } catch (PacketParsingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }
}
