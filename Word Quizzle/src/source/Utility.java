package source;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Utility {
	public static void write(Socket receiver, Object request) {
		try {
			ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(receiver.getOutputStream()));
			writer.writeObject(request);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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