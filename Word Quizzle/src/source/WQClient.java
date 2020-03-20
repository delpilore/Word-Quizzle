package source;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import source.RegisterInterface.UserAlreadyRegisteredException;

public class WQClient {
	
	public static void main(String[] args) {
		
		Remote remoteObject;
		RegisterInterface serverObject;
		
		Scanner input = new Scanner(System.in);
		String command;
		Boolean on = true;
		
		System.out.print("Benvenuto/a in World Quizzle!\n");
        
        while(on) {
        	
	   		 System.out.print("\nAiuto: --help\n"
					 + "usage : COMMAND [ ARGS ... ]\n"
	                 + "\tR: Registrati come nuovo utente\n"
	                 + "\tL: Effettua il login\n"
	                 + "\tLo: Effettua il logout\n"
	                 + "\tA: Aggiungi un amico\n"
	                 + "\tLa: Vedi la tua lista amici\n"
	                 + "\tS: Sfida un amico\n"
	                 + "\tMp: Vedi il tuo punteggio\n"
	                 + "\tMc: Vedi la classifica con i tuoi amici\n"
	         		 + "\tX: Chiudi\n\t");
	        command = input.next();
	        
	        switch (command) {
		        case "R":
		        case "r":
		            String usr, pass;
		            System.out.print("Nome utente: ");
		            usr = input.next();
		            System.out.print("Password: ");
		            pass = input.next();
		            
		    		try {
		    			Registry r = LocateRegistry.getRegistry(15000);
		    			remoteObject = r.lookup("REGISTER-SERVER");
		    			serverObject = (RegisterInterface) remoteObject;
		    			
		    			if (serverObject.registra_utente(usr, pass))
		    				System.out.print("Registrato!\n");
		    			
		    		}
		    		catch(UserAlreadyRegisteredException e) {
		    			System.out.print("Questo username è stato già preso!\n");
		    		}
		    		catch(NullPointerException e) {
		    			System.out.print("Non hai inserito uno o entrambi gli argomenti!\n");
		    		}
		    		catch(Exception e) {
		    			System.out.print("Qualcosa è andato storto.. riprova!\n");
		    		}
		            
				break;
				
		        case "X":
		        case "x":
		        	System.out.print("Chiudo\n");
		        	on = false;
		        break;
		
	        }
        }
        
        input.close();    
	}
}