package wordquizzle;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class GeneralUtilities {

	public static String UDPToString(DatagramPacket pack) 
    { 
        return new String(pack.getData(), pack.getOffset(), pack.getLength(), StandardCharsets.UTF_8);
    } 
}
