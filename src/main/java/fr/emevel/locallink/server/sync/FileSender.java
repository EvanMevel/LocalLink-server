package fr.emevel.locallink.server.sync;

import fr.emevel.locallink.network.PacketReceiver;
import fr.emevel.locallink.network.packets.PacketSendFile;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileSender implements Runnable {

    public static final List<FileSender> currentlySending = new ArrayList<>();

    private final PacketReceiver client;
    private final String folder;
    private final File file;
    private final int bufferSize;
    private ServerSocket socket;
    private Socket clientSocket;
    @Getter
    private final long length;
    @Getter
    private long current = 0;

    public FileSender(PacketReceiver client, String folder, File file, int bufferSize) {
        this.client = client;
        this.folder = folder;
        this.file = file;
        this.bufferSize = bufferSize;
        this.length = file.length();
    }

    private void close() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while closing file sender");
            e.printStackTrace();
        }
    }

    private void sendFile() throws IOException {
        System.out.println("Sending file " + file.getName() + " to " + socket.getLocalPort());
        try (FileInputStream fin = new FileInputStream(file)) {
            byte[] buffer = new byte[bufferSize];
            int read;
            while ((read = fin.read(buffer)) != -1) {
                current += read;
                clientSocket.getOutputStream().write(buffer, 0, read);
                synchronized (this) {
                    try {
                        wait(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void waitClient() throws IOException {
        socket = new ServerSocket(0);
        client.safeSendPacket(new PacketSendFile(folder, file.getName(), socket.getLocalPort(), length));
        System.out.println("Waiting for client at " + socket.getLocalPort());
        // TODO non blocking accept with a timeout
        clientSocket = socket.accept();
    }

    @Override
    public void run() {
        currentlySending.add(this);
        try {
            waitClient();
            sendFile();
            close();
        } catch (IOException e) {
            System.err.println("Error while sending file");
            e.printStackTrace();
        } finally {
            currentlySending.remove(this);
        }
    }

}