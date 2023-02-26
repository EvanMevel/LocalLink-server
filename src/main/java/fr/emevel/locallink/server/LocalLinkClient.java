package fr.emevel.locallink.server;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.PacketConsumerList;
import fr.emevel.locallink.network.Signatures;
import fr.emevel.locallink.network.packets.*;
import fr.emevel.locallink.server.sync.FileAction;
import fr.emevel.locallink.server.sync.FileSenderExecutor;
import fr.emevel.locallink.server.sync.SyncFolder;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocalLinkClient extends LinkSocket {

    private final PacketConsumerList packetConsumerList = new PacketConsumerList();
    private final List<Pair<SyncFolder, List<PacketFileList>>> folders = new ArrayList<>();
    private final FileSenderExecutor fileSenderExecutor = new FileSenderExecutor(5, 1024);
    private final LocalLinkServerData data;
    private String name;
    private UUID uuid;

    public LocalLinkClient(LocalLinkServerData data, Socket socket) throws IOException {
        super(socket);
        this.data = data;
        packetConsumerList.addConsumer(PacketHandShake.class, this::handshake);
        packetConsumerList.addConsumer(PacketFolderList.class, this::receiveFolder);
        packetConsumerList.addConsumer(PacketFileList.class, this::receiveFiles);
    }

    @Override
    public void stop() throws IOException {
        try {
            fileSenderExecutor.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            super.stop();
        }
    }

    public void runSync() {
        for (Pair<SyncFolder, List<PacketFileList>> folder : folders) {
            safeSendPacket(new PacketAskFiles(folder.getKey().getUuid()));
        }
    }

    public void askFolders() {
        safeSendPacket(new PacketAskFolders());
    }

    public void createLink(UUID folderUuid, String folder) {
        safeSendPacket(new PacketCreateLink(folderUuid, folder));
        data.getUserFolders()
                .computeIfAbsent(this.uuid, k -> new ArrayList<>()).add(folderUuid);
    }

    private void handshake(PacketHandShake packet) {
        if (!Signatures.VERSION.equals(packet.getVersion())) {
            System.out.println("Kicked client " + getPrintableAddress() + " because of version mismatch (expected " + Signatures.VERSION + ", got " + packet.getVersion() + ")");
            try {
                stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        name = packet.getName();
        uuid = packet.getUuid();
        safeSendPacket(new PacketHandShake(data.getName(), data.getUuid(), Signatures.VERSION));

        List<UUID> dataFolders = data.getUserFolders().get(packet.getUuid());
        if (dataFolders == null) {
            return;
        }
        for (UUID folderId : dataFolders) {
            SyncFolder folder = data.getFolders().getFolder(folderId);
            if (folder != null) {
                folders.add(Pair.of(folder, new ArrayList<>()));
            } else {
                System.out.println(packet.getName() + " >> Folder " + folderId + " not found");
            }
        }
        runSync();
    }

    private void receiveFolder(PacketFolderList packet) {
        System.out.println(packet.toString());
    }

    private void executeSync(Pair<SyncFolder, List<PacketFileList>> sync) {
        try {
            List<FileAction> actions = sync.getKey().needUpdate(sync.getValue());
            fileSenderExecutor.execute(this, sync.getKey().getUuid(), actions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveFiles(PacketFileList packet) {
        for (Pair<SyncFolder, List<PacketFileList>> folder : folders) {
            if (folder.getKey().getUuid().equals(packet.getFolder())) {
                folder.getValue().add(packet);
                if (packet.isEnd()) {
                    executeSync(folder);
                    folder.getValue().clear();
                }
                return;
            }
        }
    }

    @Override
    protected void onPacketReceived(Packet packet) throws IOException {
        System.out.println("Received packet " + packet);
        packetConsumerList.consumePacket(packet);
    }

}
