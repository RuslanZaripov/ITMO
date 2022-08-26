package info.kgeorgiy.ja.zaripov.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utils class for server {@link HelloUDPServer} and client {@link HelloUDPClient}.
 */
public final class HelloUDPUtils {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private HelloUDPUtils() {
    }

    /**
     * Creates {@link DatagramPacket} packet for receiving messages.
     *
     * @param socket            for getting receive buffer size
     * @return                  {@link DatagramPacket} created packet
     * @throws SocketException  if there is an error
     *                          in the underlying protocol
     */
    public static DatagramPacket createPacket(final DatagramSocket socket)
            throws SocketException {
        return new DatagramPacket(
                new byte[socket.getReceiveBufferSize()],
                socket.getReceiveBufferSize()
        );
    }

    /**
     * Sends a message via specified address.
     *
     * @param socket where message will be sent
     * @param request string message
     * @param address socket address
     * @throws IOException if an I/O error occurs
     */
    public static void send(
            final DatagramSocket socket,
            final String request,
            final SocketAddress address
    ) throws IOException {
        DatagramPacket outPacket = new DatagramPacket(new byte[0], 0);
        setString(outPacket, request);
        outPacket.setSocketAddress(address);
        socket.send(outPacket);
    }

    /**
     * Receives {@link DatagramPacket} packet from the specified socket.
     *
     * @param socket where the packet was sent from
     * @return {@link DatagramPacket} sent package
     * @throws IOException if an I/O error occurs while receiving
     */
    public static DatagramPacket receivePacket(final DatagramSocket socket)
            throws IOException {
        DatagramPacket inPacket = createPacket(socket);
        socket.receive(inPacket);
        return inPacket;
    }

    /**
     * Simply extracts message from the received packet.
     *
     * @param socket where the message was sent from
     * @return string message located in the received packet
     * @throws IOException if an I/O error occurs while receiving
     */
    public static String receive(final DatagramSocket socket)
            throws IOException {
        return getString(receivePacket(socket));
    }

    /**
     * Converts string to byte array.
     *
     * @param string to be converted
     * @return byte array
     */
    public static byte[] getBytes(final String string) {
        return string.getBytes(CHARSET);
    }

    /**
     * Gets string message from datagram packet.
     *
     * @param packet which contains message
     * @return string message
     */
    public static String getString(final DatagramPacket packet) {
        return getString(
                packet.getData(),
                packet.getOffset(),
                packet.getLength()
        );
    }

    /**
     * Extracts message from byte array via length and offset.
     *
     * @param data byte array
     * @param offset start of message
     * @param length message length
     * @return string message
     */
    public static String getString(
            final byte[] data,
            final int offset,
            final int length
    ) {
        return new String(data, offset, length, CHARSET);
    }

    /**
     * Sets a message in a packet.
     *
     * @param packet where message will be placed
     * @param string message to be placed
     */
    public static void setString(
            final DatagramPacket packet,
            final String string
    ) {
        packet.setData(getBytes(string));
        packet.setLength(packet.getData().length);
    }

    /**
     * Shuts down the {@link ExecutorService}.
     *
     * @param executorService to be terminated
     * @param timeout the maximum time to wait
     */
    public static void awaitTermination(
            final ExecutorService executorService,
            final int timeout
    ) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (final InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
