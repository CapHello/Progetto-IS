package it.unina.prenotazioni.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    /*
     * Istanza unica di JpaUtil.
     *
     * Questa variabile realizza il cuore del pattern Singleton:
     * la classe mantiene internamente l'unica istanza disponibile.
     */
    private static JpaUtil instance;

    /*
     * EntityManagerFactory condivisa.
     *
     * La factory viene creata una sola volta, perché è un oggetto
     * costoso da inizializzare: legge la persistence unit dal file
     * persistence.xml e prepara Hibernate per comunicare con il database.
     */
    private EntityManagerFactory emf;

    /*
     * Costruttore privato.
     *
     * Questo impedisce al resto dell'applicazione di creare oggetti
     * JpaUtil usando new JpaUtil().
     *
     * L'unico modo per ottenere JpaUtil sarà passare dal metodo
     * statico getInstance().
     */
    private JpaUtil() {
        /*
         * Creiamo la EntityManagerFactory usando la persistence unit
         * definita nel file persistence.xml.
         */
        emf = Persistence.createEntityManagerFactory("prenotazioniPU");
    }

    /*
     * Punto di accesso globale all'unica istanza di JpaUtil.
     *
     * Se l'istanza non esiste ancora, viene creata.
     * Se è già stata creata, viene semplicemente restituita.
     *
     * Questo metodo completa l'applicazione del pattern Singleton.
     */
    public static JpaUtil getInstance() {
        if (instance == null) {
            instance = new JpaUtil();
        }

        return instance;
    }

    /*
     * Crea un nuovo EntityManager.
     *
     * Attenzione: l'EntityManager non è Singleton.
     * Ogni operazione di persistenza deve usare un proprio EntityManager,
     * perché l'EntityManager mantiene lo stato della singola sessione
     * di lavoro con il database.
     */
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /*
     * Chiude la EntityManagerFactory.
     *
     * Questo metodo va chiamato alla fine dell'applicazione,
     * quando non sono più necessarie operazioni di persistenza.
     */
    public void chiudi() {
        emf.close();
    }
}
