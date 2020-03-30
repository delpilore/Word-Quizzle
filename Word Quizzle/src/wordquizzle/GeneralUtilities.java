package wordquizzle;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* GENERALUTILITIES
* 
* Questa classe contiene le implementazioni di metodi considerati di generale utilità nell'ecosistema
* dell'applicazione Word Quizzle. (utili sia a client che a server)
* In questo caso abbiamo un solo metodo: "UDPToString", che viene utilizzato per convertire l'array di byte
* contenuto in un datagramma UDP, in una stringa.
*/

public class GeneralUtilities {

	// UDPToString(DatagramPacket pack)
	//
	// Questo metodo trasforma l'array di byte contenuto nel datagramma UDP passato 
	// in una stringa di formato UTF8 (utile per non perdere accenti o simboli speciali)
	public static String UDPToString(DatagramPacket pack) 
    { 
        return new String(pack.getData(), 
        				  pack.getOffset(), 
        				  pack.getLength(), 
        				  StandardCharsets.UTF_8);
    } 
	
}