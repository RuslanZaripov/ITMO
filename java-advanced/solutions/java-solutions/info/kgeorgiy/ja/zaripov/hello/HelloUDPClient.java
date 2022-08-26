package info.kgeorgiy.ja.zaripov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.zaripov.hello.HelloUDPUtils.*;

/**
 * Client class that implements and interface {@link HelloClient}.
 */
public class HelloUDPClient implements HelloClient {
    private static final int SINGLE_REQUEST_TIMEOUT = 5;
    private static final int SOCKET_TIMEOUT = 100;

    private InetSocketAddress socketAddress;

    /**
     * Runs Hello client.
     * This method should return when all requests completed.
     *
     * @param hostName server host
     * @param port server port
     * @param prefix request prefix
     * @param threadCount number of request threads
     * @param requestCount number of requests per thread.
     */
    @Override
    public void run(final String hostName,
                    final int port,
                    final String prefix,
                    final int threadCount,
                    final int requestCount
    ) {
        socketAddress = new InetSocketAddress(hostName, port);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        IntStream.range(0, threadCount)
                .<Runnable>mapToObj(threadNumber ->
                        () -> makeRequest(prefix, threadNumber, requestCount))
                .forEach(executorService::submit);

        awaitTermination(executorService, SINGLE_REQUEST_TIMEOUT * threadCount * requestCount);
    }

    private void makeRequest(
            final String prefix,
            final int threadNumber,
            final int requestCount
    ) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(SOCKET_TIMEOUT);
            IntStream.range(0, requestCount)
                    .mapToObj(requestNumber -> getMessage(prefix, threadNumber, requestNumber))
                    .forEach(request -> process(datagramSocket, request));
        } catch (final SocketException e) {
            System.err.printf(
                    "Socket could not be opened. "
                            + "Socket host: %s, port: %d%n",
                    socketAddress.getHostName(),
                    socketAddress.getPort()
            );
        }
    }

    public String getMessage(final String prefix, final int threadNumber, final int requestNumber) {
        return String.format("%s%d_%d", prefix, threadNumber, requestNumber);
    }

    private void process(
            final DatagramSocket datagramSocket,
            final String request
    ) {
        while (true) {
            try {
                send(datagramSocket, request, socketAddress);
                final String responseMessage = receive(datagramSocket);
                if (responseMessage.contains(request)) {
                    System.out.println(responseMessage);
                    break;
                }
            } catch (final IOException e) {
                System.err.printf(
                        "An I/O error occurred while sending datagram packet. "
                                + "Socket: address: %s, port: %d%n",
                        socketAddress.getAddress(),
                        socketAddress.getPort()
                );
            }
        }
    }

    private static final String USAGE =
            "HelloUDPClient <host-name> <port> <prefix> <threads> <requests>";

    /**
     * Main method of class {@link HelloUDPClient}.
     *
     * @param args command line arguments
     */
    public void main(final String[] args) {
        if (!check(args)) {
            System.err.println(USAGE);
            return;
        }
        try {
            final String hostName = args[0];
            final int port = Integer.parseInt(args[1]);
            final String prefix = args[2];
            final int threadCount = Integer.parseInt(args[3]);
            final int requestCount = Integer.parseInt(args[4]);
            run(hostName, port, prefix, threadCount, requestCount);
        } catch (final NumberFormatException e) {
            System.err.println("Cannot parse command line arguments");
        }
    }

    private static boolean check(final String[] args) {
        return Objects.nonNull(args)
                && Arrays.stream(args).noneMatch(Objects::isNull)
                && args.length == 5;
    }
}
