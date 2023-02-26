package fr.emevel.locallink.server;

import fr.emevel.locallink.server.sync.SyncFolderList;
import lombok.Data;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class LocalLinkServerData implements Serializable {

    private UUID uuid = UUID.randomUUID();
    private String name = InetAddress.getLocalHost().getHostName();
    private SyncFolderList folders = new SyncFolderList();
    private int port = 0;
    private Map<UUID, List<UUID>> userFolders = new HashMap<>();

    public LocalLinkServerData() throws UnknownHostException {
    }
}
