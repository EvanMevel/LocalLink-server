package fr.emevel.locallink.server.sync;

import fr.emevel.locallink.network.packets.PacketFileList;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public interface SyncFolder extends Serializable {

    List<FileAction> needUpdate(Iterable<PacketFileList> packets) throws IOException;

    String getName();

    UUID getUuid();

}
