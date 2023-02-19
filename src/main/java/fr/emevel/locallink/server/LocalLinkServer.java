package fr.emevel.locallink.server;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.server.NetworkServer;

import java.io.IOException;
import java.net.Socket;

public class LocalLinkServer extends NetworkServer {

    public LocalLinkServer(int port) throws IOException {
        super(port);
    }

    @Override
    protected LinkSocket createClient(Socket sock) throws IOException {
        return new LocalLinkClient(sock);
    }

}
