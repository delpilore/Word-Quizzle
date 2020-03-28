package wordquizzle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Comunication {

	// write(Socket receiver, Object message)
	//
	// Questo metodo racchiude tutto il necessario per spedire un oggetto su una socket.
	// Viene usato sia da Client che da Server.
	public static void write(Socket receiver, Object message) {
		try {
			ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(receiver.getOutputStream()));
			writer.writeObject(message);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// read()
	//
	// Questo metodo racchiude tutto il necessario per leggere un oggetto da una socket.
	// Viene usato sia da Client che da Server.
	public static Object read(Socket sender) {
		try {
			ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(sender.getInputStream()));
			return reader.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
