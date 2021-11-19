package ch.epfl.da.perfectLink;

import ch.epfl.da.message.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerfectLinkMessageTest {
    @Test
    public void testSerializeDeserialize(){
        Message m = new Message(1,2, new byte[]{0,(byte)255});
        PerfectLinkMessage plm = new PerfectLinkMessage(MessageType.MESSAGE, 3, m);

        byte[] serialized = PerfectLinkMessage.serialize(plm);
        PerfectLinkMessage newPlm = PerfectLinkMessage.deserialize(serialized);

        assertEquals(plm, newPlm);
    }
}
