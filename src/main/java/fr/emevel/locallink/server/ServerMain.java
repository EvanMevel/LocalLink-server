package fr.emevel.locallink.server;

import java.io.IOException;
import java.util.Scanner;

public class ServerMain {

    public static void main(String[] args) throws IOException {
        LocalLinkServer server = new LocalLinkServer(4242);

        server.start();

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        server.stop();
    }
}