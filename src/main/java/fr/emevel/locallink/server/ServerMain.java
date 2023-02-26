package fr.emevel.locallink.server;

import java.io.*;
import java.util.Scanner;

public class ServerMain {

    public static LocalLinkServer server;

    private static void save(LocalLinkServerData data) throws IOException {
        File file = new File("server.dat");
        if (!file.exists()) {
            file.createNewFile();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
            oos.writeObject(data);
        }
    }

    private static LocalLinkServerData loadData() throws IOException {
        File file = new File("server.dat");
        if (!file.exists()) {
            LocalLinkServerData data = new LocalLinkServerData();
            save(data);
            return data;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            return (LocalLinkServerData) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        LocalLinkServerData data = loadData();

        server = new LocalLinkServer(data);

        server.start();

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        server.stop();
    }
}