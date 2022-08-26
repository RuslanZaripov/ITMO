package info.kgeorgiy.ja.zaripov.hello;

import java.nio.ByteBuffer;

class ChannelData {
    private int requestId;
    private final int channelId;
    private final ByteBuffer byteBuffer;

    ChannelData(int channelId, int bufferSize) {
        this.requestId = 0;
        this.channelId = channelId;
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
    }

    public int getChannelId() {
        return channelId;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public int getRequestId() {
        return requestId;
    }

    public void incrementRequestId() {
        requestId += 1;
    }
}