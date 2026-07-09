package it.unina.prenotazioni.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject del pattern Observer: mantiene la lista degli osservatori da avvisare
 * ai cambi di stato. La lista non è persistita: gli observer vanno riagganciati
 * (attach) dopo ogni caricamento dal database.
 */
public abstract class Subject {

    protected List<Observer> observers = new ArrayList<>();

    /** Registra un osservatore, evitando i duplicati. */
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** Rimuove un osservatore. Non usato nei flussi attuali: mantenuto per completezza del pattern. */
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    public abstract void notifyObservers();
}