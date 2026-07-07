package it.unina.prenotazioni.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;

public class GestorePersistenza {

    /*
     * Salva nel database un oggetto persistente.
     *
     * Il parametro è di tipo Object perché il gestore della persistenza
     * deve rimanere generico: non deve conoscere direttamente le classi
     * specifiche del dominio, come Proprietario o Imbarcazione.
     *
     * L'oggetto passato deve però essere una Entity, cioè una classe
     * annotata con @Entity.
     */
    // public void salva(Object oggetto) {
    public boolean salva(Object oggetto) {
        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            /*
             * Ogni operazione che modifica il database deve essere eseguita
             * all'interno di una transazione.
             */
            em.getTransaction().begin();

            /*
             * persist rende l'oggetto gestito da Hibernate.
             * Al commit della transazione, Hibernate tradurrà l'oggetto
             * in una riga della tabella corrispondente.
             */
            em.persist(oggetto);

            /*
             * Conferma la transazione.
             * Da questo momento le modifiche diventano effettive nel database.
             */
            em.getTransaction().commit();

            return true;

        } catch (RuntimeException e) {

            /*
             * Se qualcosa va storto durante l'operazione, annulliamo
             * la transazione per evitare modifiche parziali al database.
             */
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            // throw e;
            e.printStackTrace();
            return false;

        } finally {
            /*
             * L'EntityManager deve essere chiuso dopo l'operazione.
             * La EntityManagerFactory resta invece aperta e viene chiusa
             * solo alla fine dell'applicazione.
             */
            em.close();
        }
    }

    /*
     * Salva più oggetti nella stessa transazione.
     *
     * Questo metodo è utile quando vogliamo rendere persistenti oggetti
     * collegati tra loro, ad esempio un Proprietario e una o più Imbarcazione.
     *
     * Usare una sola transazione è importante: o vengono salvati tutti
     * gli oggetti, oppure, in caso di errore, non viene salvato nessuno.
     */
    // public void salvaTutti(Object... oggetti) {
    public boolean salvaTutti(Object... oggetti) {
        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            for (Object oggetto : oggetti) {
                em.persist(oggetto);
            }

            em.getTransaction().commit();
            return true;

        } catch (RuntimeException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            // throw e;
            e.printStackTrace();
            return false;

        } finally {
            em.close();
        }
    }

    /*
     * Cerca un oggetto persistente a partire dalla sua classe e dal suo id.
     *
     * Il metodo è generico: può essere usato con qualunque Entity.
     *
     * Esempio:
     * Proprietario p = cercaSalaPerId(Proprietario.class, 1L);
     */
    public <T> T trovaPerId(Class<T> classe, Long id) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            /*
             * find cerca nel database una riga della tabella associata
             * alla classe indicata, usando l'id come chiave primaria.
             */
            return em.find(classe, id);

        } finally {
            em.close();
        }
    }

    /*
     * Cerca tutti gli oggetti persistenti di una certa classe
     * per cui un campo ha un determinato valore.
     */
    public <T> List<T> cercaPerCampo(Class<T> classe,
            String nomeCampo,
            Object valore) {

        return cercaPerCampi(
                classe,
                Map.of(nomeCampo, valore));
    }

    /*
     * Cerca tutti gli oggetti persistenti che soddisfano un insieme di condizioni.
     *
     * La query JPQL viene costruita nel livello database.
     */
    public <T> List<T> cercaPerCampi(Class<T> classe,
            Map<String, Object> campi) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            StringBuilder jpql = new StringBuilder();

            jpql.append("SELECT e FROM ")
                    .append(classe.getSimpleName())
                    .append(" e");

            if (!campi.isEmpty()) {
                jpql.append(" WHERE ");

                int contatore = 0;

                for (String nomeCampo : campi.keySet()) {
                    if (contatore > 0) {
                        jpql.append(" AND ");
                    }

                    String nomeParametro = nomeCampo.replace(".", "_");

                    jpql.append("e.")
                            .append(nomeCampo)
                            .append(" = :")
                            .append(nomeParametro);

                    contatore++;
                }
            }

            TypedQuery<T> query = em.createQuery(
                    jpql.toString(),
                    classe);

            for (String nomeCampo : campi.keySet()) {
                String nomeParametro = nomeCampo.replace(".", "_");
                query.setParameter(nomeParametro, campi.get(nomeCampo));
            }

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    /*
     * Esegue una query JPQL personalizzata (utile per JOIN o query complesse)
     * e restituisce una lista di risultati tipizzati.
     */
    public <T> List<T> eseguiQueryCustom(String jpql, Class<T> classeRisultato, Map<String, Object> parametri) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            TypedQuery<T> query = em.createQuery(jpql, classeRisultato);

            if (parametri != null && !parametri.isEmpty()) {
                for (Map.Entry<String, Object> parametro : parametri.entrySet()) {
                    query.setParameter(parametro.getKey(), parametro.getValue());
                }
            }

            return query.getResultList();

        } finally {
            em.close();
        }
    }

    /*
     * Cerca il primo oggetto persistente che soddisfa un insieme di condizioni.
     *
     * Se non trova nessun risultato, restituisce null.
     */
    public <T> T cercaPrimoPerCampi(Class<T> classe,
            Map<String, Object> campi) {

        List<T> risultati = cercaPerCampi(classe, campi);

        if (risultati.isEmpty()) {
            return null;
        }

        return risultati.get(0);
    }

    public <T> T aggiorna(T oggetto) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            T oggettoAggiornato = em.merge(oggetto);

            em.getTransaction().commit();

            return oggettoAggiornato;

        } catch (RuntimeException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw e;

        } finally {
            em.close();
        }
    }

    public <T> boolean elimina(Class<T> classe, Long id) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            /*
             * Cerchiamo nel database l'oggetto da eliminare,
             * usando la sua classe e il suo id.
             */

            T oggetto = em.find(classe, id);

            // se l'oggetto esiste, lo eliminiamo
            if (oggetto != null) {
                em.remove(oggetto);
                em.getTransaction().commit();
                return true;
            }

            em.getTransaction().commit();
            return false;

        } catch (RuntimeException e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            e.printStackTrace();
            return false;

        } finally {
            em.close();
        }
    }

}