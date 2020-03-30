package wordquizzle.client;

import java.net.BindException;
import java.net.DatagramSocket;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* CLIENTUTILITIES
* 
* Questa classe contiene le implementazioni di metodi considerati d'utilità per il client.
* In questo caso abbiamo un solo metodo: "portScanner", che viene utilizzato per rintracciare la
* prima porta disponibile su cui piazzare una socket UDP.
*/

public class ClientUtilities {
 
	// portScanner()
	//
	// Questo metodo rintraccia e ritorna la prima porta disponibile
	// su cui aprire una socket UDP.
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