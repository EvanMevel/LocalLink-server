package fr.emevel.locallink.server.sync;

import fr.emevel.locallink.network.packets.PacketFileList;

import java.io.IOException;
import java.util.List;

public interface SyncFolder {

    List<FileAction> needUpdate(Iterable<PacketFileList> packets) throws IOException;

    String getName();

}
