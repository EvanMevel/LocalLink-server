package fr.emevel.locallink.server;

import fr.emevel.locallink.network.LinkSocket;
import fr.emevel.locallink.network.PacketConsumerList;
import fr.emevel.locallink.network.Signatures;
import fr.emevel.locallink.network.packets.*;
import fr.emevel.locallink.server.sync.FileAction;
import fr.emevel.locallink.server.sync.FileSenderExecutor;
import fr.emevel.locallink.server.sync.LocalSyncFolder;
import fr.emevel.locallink.server.sync.SyncFolder;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LocalLinkClient extends LinkSocket {

    private final PacketConsumerList packetConsumerList = new PacketConsumerList();
    private final List<Pair<SyncFolder, List<PacketFileList>>> folders = new ArrayList<>();
    private final FileSenderExecutor fileSenderExecutor = new FileSenderExecutor(5, 1024);

    public LocalLinkClient(Socket socket) throws IOException {
        super(socket);
        packetConsumerList.addConsumer(PacketHandShake.class, this::handshake);
        packetConsumerList.addConsumer(PacketFolderList.class, this::receiveFolder);
        packetConsumerList.addConsumer(PacketFileList.class, this::receiveFiles);

        folders.add(Pair.of(new LocalSyncFolder(new File("server/folder")), new ArrayList<>()));
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
            safeSendPacket(new PacketAskFiles(folder.getKey().getName()));
        }
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
        safeSendPacket(new PacketHandShake("LocalLink Server", Signatures.VERSION));

        runSync();
    }

    private void receiveFolder(PacketFolderList packet) {
        PacketAskFiles packetAskFiles = new PacketAskFiles(packet.getFolders().get(0).getName());

        System.out.println(packet.toString());
        safeSendPacket(packetAskFiles);
    }

    private void executeSync(Pair<SyncFolder, List<PacketFileList>> sync) {
        try {
            List<FileAction> actions = sync.getKey().needUpdate(sync.getValue());
            fileSenderExecutor.execute(this, sync.getKey().getName(), actions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveFiles(PacketFileList packet) {
        for (Pair<SyncFolder, List<PacketFileList>> folder : folders) {
            if (folder.getKey().getName().equals(packet.getFolder())) {
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
