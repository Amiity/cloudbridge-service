package com.amiity.cloudbridge.utility;

import java.io.IOException;
import java.net.ServerSocket;

public final class DeploymentUtility {

    private DeploymentUtility() {
        throw new UnsupportedOperationException("Utility class - Cannot be instantiated");
    }

    /**
     * Finds the next available port starting from the given base port.
     */
    public static int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {  // 0 means pick any available port
            return socket.getLocalPort();
        }
    }

    /**
     * Checks if a given port is already in use.
     */
    private static boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.close();
            return false; // Port is available
        } catch (IOException e) {
            return true; // Port is in use
        }
    }
}
