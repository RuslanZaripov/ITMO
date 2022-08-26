package info.kgeorgiy.ja.zaripov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static info.kgeorgiy.ja.zaripov.hello.HelloUDPUtils.awaitTermination;
import static info.kgeorgiy.ja.zaripov.hello.HelloUDPUtils.getString;
import static info.kgeorgiy.ja.zaripov.hello.HelloUDPUtils.receivePacket;
import static info.kgeorgiy.ja.zaripov.hello.HelloUDPUtils.setString;

/**
 * Server class that implements and interface {@link HelloServer}.
 */
public class HelloUDPServer implements HelloServer {
    private static final int EXECUTOR_SERVICE_TERMINATION_TIMEOUT = 1;
    private ExecutorService executorService;
    private DatagramSocket socket;
    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(final int port, final int threads) {
        if (socket != null) {
            return;
        }

        executorService = Executors.newFixedThreadPool(threads);
        socket = getSocket(port);

        new Thread(() -> {
            try {
                while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    final DatagramPacket responsePacket = receivePacket(socket);
                    executorService.submit(() -> send(responsePacket));
                }
            } catch (IOException e) {
                System.err.println(
                        "An I/O error occurred while receiving datagram packet. "
                                + "Socket: port: " + socket.getPort()
                );
            }
        }).start();
    }

    private DatagramSocket getSocket(final int port) {
        try {
            return new DatagramSocket(port);
        } catch (final SocketException e) {
            System.err.println("Socket could not be opened, port: " + port);
        }
        return null;
    }

    private void send(final DatagramPacket responsePacket) {
        try {
            setString(responsePacket, getResponseMessage(getString(responsePacket)));
            socket.send(responsePacket);
        } catch (IOException e) {
            System.err.println("Cannot send packet, port: " + socket.getPort());
        }
    }

    private String getResponseMessage(String message) {
        return String.format("Hello, %s", message);
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        socket.close();
        awaitTermination(
                executorService,
                EXECUTOR_SERVICE_TERMINATION_TIMEOUT
        );
    }

    private static final String USAGE = "HelloUDPServer <port> <threads>";

    /**
     * Main method of class {@link HelloUDPServer}.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        if (!check(args)) {
            System.err.println(USAGE);
            return;
        }
        try (HelloUDPServer server = new HelloUDPServer()) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Cannot parse command line arguments");
        }
    }

    private static boolean check(final String[] args) {
        return Objects.nonNull(args)
                && Arrays.stream(args).noneMatch(Objects::isNull)
                && args.length == 2;
    }
}
