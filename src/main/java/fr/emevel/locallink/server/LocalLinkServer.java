package fr.emevel.locallink.server;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.server.NetworkServer;
import fr.emevel.locallink.server.sync.LocalSyncFolder;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class LocalLinkServer extends NetworkServer {

    private final LocalLinkServerData data;

    public LocalLinkServer(LocalLinkServerData data) throws IOException {
        super(data.getPort());
        this.data = data;
    }

    @Override
    protected LinkSocket createClient(Socket sock) throws IOException {
        return new LocalLinkClient(data, sock);
    }

    public void createLocalSyncFolder(File folder) {
        LocalSyncFolder syncFolder = new LocalSyncFolder(folder);
        data.getFolders().addFolder(syncFolder);
    }

}
