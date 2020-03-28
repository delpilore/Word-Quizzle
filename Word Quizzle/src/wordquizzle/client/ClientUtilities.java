package wordquizzle.client;

import java.net.BindException;
import java.net.DatagramSocket;

public class ClientUtilities {
 
	public static int portScanner() {
		for (int i=1024; i<49151; i++){
			try (DatagramSocket s =new DatagramSocket(i)){ 
				return i;
			}
			catch (BindException e) {
				;
			}
			catch (Exception e) {
				System.out.println (e);
			}
		} 
		return 0;
	}
}
