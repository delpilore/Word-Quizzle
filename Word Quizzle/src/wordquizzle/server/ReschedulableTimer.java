package wordquizzle.server;

import java.util.Timer;
import java.util.TimerTask;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* RESCHEDULABLETIMER
* 
* Questa classe, che estende la classe già esistente Timer, realizza un Timer rischedulabile.
* Questo tipo di timer entra in gioco al momento dell'invio del risultato di un match che, di base, 
* viene spedito agli sfidanti dopo 60 secondi dall'inizio della partita.
* Nel caso però, entrambi gli sfidanti finiscano le parole prima dei 60 secondi, è necessario rischedulare 
* questa routine in modo che avvenga subito.
* Ecco il perché di questa classe.
*/

public class ReschedulableTimer extends Timer {
	
    private Runnable  task;
    private TimerTask timerTask;
    
    // schedule(Runnable runnable, long delay)
    //
    // Il seguente metodo prende come argomento un oggetto Runnable (il nostro task da eseguire)
    // e un delay, che rappresenta il tempo che deve passare prima che questo task venga eseguito.
    public void schedule(Runnable runnable, long delay)
    {
        task = runnable;
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                task.run();
            }
        };
        this.schedule(timerTask, delay);
    }

    // reschedule(long delay)
    //
    // Il seguente metodo cancella il precedente task e ne crea uno nuovo con un nuovo delay (passato per argomento)
    public void reschedule(long delay)
    {
        timerTask.cancel();
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                task.run();
            }
        };
        this.schedule(timerTask, delay);
    }
}