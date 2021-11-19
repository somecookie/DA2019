package ch.epfl.da.message;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class LCBMessage {
    private final int origin;
    private final int[] data;

    public LCBMessage(int origin, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("The arguments of Message cannot be null");
        }

        this.origin = origin;
        IntBuffer intBuf =
                ByteBuffer.wrap(data)
                        .asIntBuffer();
        this.data = new int[data.length / 4];
        intBuf.get(this.data);
    }


    public int getOrigin() {
        return origin;
    }

    public int[] getData() {
        return data;
    }

    public int getValue(){
        return data[origin-1] + 1;
    }
}
