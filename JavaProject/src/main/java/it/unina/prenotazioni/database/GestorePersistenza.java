package it.unina.prenotazioni.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;

/**
 * <<database>> Accesso generico alla persistenza (CRUD + query JPQL): lavora per
 * Class&lt;T&gt; e non conosce le classi di dominio, quindi il layer database non
 * dipende da nessun altro layer. Ogni operazione apre un proprio EntityManager
 * (chiuso nel finally) e una propria transazione; sugli errori di scrittura
 * salva/salvaTutti/elimina fanno rollback e restituiscono false, aggiorna propaga.
 */
public class GestorePersistenza {

    /** Rende persistente una nuova entity (e le associate via cascade); false se la transazione fallisce. */
    public boolean salva(Object oggetto) {
        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(oggetto);

            em.getTransaction().commit();

            return true;

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

    /** Salva più entity in un'unica transazione: o tutte o nessuna. */
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

            e.printStackTrace();
            return false;

        } finally {
            em.close();
        }
    }

    /** Ricerca per chiave primaria; null se non esiste. */
    public <T> T trovaPerId(Class<T> classe, Long id) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            return em.find(classe, id);

        } finally {
            em.close();
        }
    }

    /** Entity della classe con campo = valore; il campo supporta percorsi annidati (es. "area.salaStudio.id"). */
    public <T> List<T> cercaPerCampo(Class<T> classe,
            String nomeCampo,
            Object valore) {

        return cercaPerCampi(
                classe,
                Map.of(nomeCampo, valore));
    }

    /**
     * Ricerca con più condizioni in AND. La JPQL è costruita dinamicamente ma i valori
     * sono sempre bindati come parametri (mai concatenati nella stringa).
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

                    // I punti dei percorsi annidati non sono ammessi nei nomi dei parametri JPQL.
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

    /** Esegue una JPQL arbitraria (JOIN e query non esprimibili con cercaPerCampi). */
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

    /** Primo risultato di cercaPerCampi; null se non ce ne sono. */
    public <T> T cercaPrimoPerCampi(Class<T> classe,
            Map<String, Object> campi) {

        List<T> risultati = cercaPerCampi(classe, campi);

        if (risultati.isEmpty()) {
            return null;
        }

        return risultati.get(0);
    }

    /** Aggiorna un'entity esistente (merge); in caso di errore fa rollback e propaga l'eccezione. */
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

    /** Elimina per chiave primaria; false se l'entity non esiste o la transazione fallisce. */
    public <T> boolean elimina(Class<T> classe, Long id) {

        EntityManager em = JpaUtil.getInstance().getEntityManager();

        try {
            em.getTransaction().begin();

            T oggetto = em.find(classe, id);

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
