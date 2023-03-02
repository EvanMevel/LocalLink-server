package fr.emevel.locallink.server;

import java.io.*;
import java.util.Scanner;

public class ServerMain {

    public static LocalLinkServer server;

    public static void saveDataToFile(LocalLinkServerData data, File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
            oos.writeObject(data);
        }
    }

    public static LocalLinkServerData loadDataFromFile(File file) throws IOException {
        if (!file.exists()) {
            LocalLinkServerData data = new LocalLinkServerData();
            saveDataToFile(data, file);
            return data;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            return (LocalLinkServerData) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        LocalLinkServerData data = loadDataFromFile(new File("server.dat"));

        Runnable dataSaver = () -> {
            try {
                saveDataToFile(data, new File("server.dat"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        server = new LocalLinkServer(data, dataSaver);

        server.start();

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        server.stop();
    }
}