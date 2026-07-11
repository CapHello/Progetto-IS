package it.unina.prenotazioni.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * <database> <Singleton> Incapsula l'EntityManagerFactory, oggetto costoso creato
 * una sola volta dalla persistence unit "prenotazioniPU" (persistence.xml).
 * Gli EntityManager, al contrario, sono usa-e-getta: uno nuovo per ogni operazione.
 */
public class JpaUtil {

    private static JpaUtil instance;

    private EntityManagerFactory emf;

    private JpaUtil() {
        emf = Persistence.createEntityManagerFactory("prenotazioniPU");
        // Shutdown hook: chiusura pulita della factory alla terminazione della JVM,
        // registrato qui perché scatti solo se la factory è stata davvero creata.
        Runtime.getRuntime().addShutdownHook(new Thread(this::chiudi));
    }

    public static JpaUtil getInstance() {
        if (instance == null) {
            instance = new JpaUtil();
        }

        return instance;
    }

    /** Nuovo EntityManager: rappresenta la singola sessione di lavoro col database, va chiuso da chi lo usa. */
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /** Chiude la factory; invocato automaticamente dallo shutdown hook. */
    public void chiudi() {
        emf.close();
    }
}
