package fr.emevel.locallink.server;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.Packet;
import fr.emevel.locallink.network.PacketConsumerList;
import fr.emevel.locallink.network.Signatures;
import fr.emevel.locallink.network.packets.PacketHandShake;

import java.io.IOException;
import java.net.Socket;

public class LocalLinkClient extends LinkSocket {

    private final PacketConsumerList packetConsumerList = new PacketConsumerList();

    public LocalLinkClient(Socket socket) throws IOException {
        super(socket);
        packetConsumerList.addConsumer(PacketHandShake.class, this::handshake);
    }

    @Override
    protected void onPacketReceived(Packet packet) throws IOException {
        System.out.println("Received packet " + packet);
        packetConsumerList.consumePacket(packet);
    }

    public void handshake(PacketHandShake packet) {
        if (!Signatures.VERSION.equals(packet.getVersion())) {
            System.out.println("Kicked client " + getPrintableAddress() + " because of version mismatch (expected " + Signatures.VERSION + ", got " + packet.getVersion() + ")");
            try {
                stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        safeSendPacket(new PacketHandShake("LocalLink Server", Signatures.VERSION));
    }

}
