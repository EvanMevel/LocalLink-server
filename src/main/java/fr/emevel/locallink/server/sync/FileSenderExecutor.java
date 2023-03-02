package fr.emevel.locallink.server.sync;

import fr.emevel.locallink.network.PacketReceiver;
import fr.emevel.locallink.network.packets.PacketDeleteFiles;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileSenderExecutor {

    private final ExecutorService executor;
    private final int bufferSize;
    @Getter
    private final List<FileSender> currentlySending = new ArrayList<>();

    public FileSenderExecutor(int threadNumber, int bufferSize) {
        this.executor = Executors.newFixedThreadPool(threadNumber);
        this.bufferSize = bufferSize;
        System.out.println("File sender initialized with " + threadNumber + " threads and " + bufferSize + " buffer size");
    }

    public void stop() throws InterruptedException {
        executor.shutdown();
        System.out.println("Waiting for File Senders to stop");
        if(!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("File Senders didn't stop in time, forcing shutdown");
            executor.shutdownNow();
        }
    }

    public void execute(PacketReceiver client, UUID folder, List<FileAction> actions) {
        PacketDeleteFiles.Builder deleteBuilder = PacketDeleteFiles.builder();
        for (FileAction action : actions) {
            if (action.getAction() == FileAction.Action.REMOVE) {
                deleteBuilder.addFile(folder, action.getFile().getName());
            } else {
                executor.submit(new FileSender(client, folder, action.getFile(), bufferSize,
                        currentlySending::add, currentlySending::remove));
            }
        }
        if (deleteBuilder.size() > 0) {
            client.safeSendPacket(deleteBuilder.build());
        }
    }
}
