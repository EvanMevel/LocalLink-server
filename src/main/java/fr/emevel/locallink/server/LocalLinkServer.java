package fr.emevel.locallink.server;

import fr.emevel.locallink.network.InetAddressUtils;
import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.jmdns.server.JmDNSServerThread;
import fr.emevel.locallink.network.server.NetworkServer;
import fr.emevel.locallink.server.sync.LocalSyncFolder;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class LocalLinkServer {

    private final LocalLinkServerData data;
    @Getter
    private final NetworkServer networkServer;
    private final JmDNSServerThread jmDNSServer;
    private final Runnable dataSaver;

    public LocalLinkServer(LocalLinkServerData data, Runnable dataSaver) throws IOException {
        InetAddress address = InetAddressUtils.getLocalHostLANAddress();
        if (data.getAddress() != null) {
            address = data.getAddress();
        }
        this.networkServer = new NetworkServer(address, data.getPort()) {
            @Override
            protected LinkSocket createClient(Socket sock) throws IOException {
                return LocalLinkServer.this.createClient(sock);
            }

            @Override
            protected void clientDisconnected(LinkSocket client) {
                LocalLinkServer.this.clientDisconnected(client);
            }
        };
        this.dataSaver = dataSaver;
        this.jmDNSServer = new JmDNSServerThread(this.networkServer.getPort(), data.getUuid());
        this.data = data;
    }

    public void start() {
        jmDNSServer.start();
        networkServer.start();
    }

    public void stop() throws IOException {
        networkServer.stop();
        jmDNSServer.stop();
    }

    protected LinkSocket createClient(Socket sock) throws IOException {
        return new LocalLinkClient(data, sock, dataSaver);
    }

    protected void clientDisconnected(LinkSocket client) {
    }

    public void createLocalSyncFolder(File folder) {
        LocalSyncFolder syncFolder = new LocalSyncFolder(folder);
        data.getFolders().addFolder(syncFolder);
        dataSaver.run();
    }

}
