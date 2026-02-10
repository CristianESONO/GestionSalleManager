package com.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
// ... (autres imports javax.persistence)

public class HibernateUtil {
    private static final String PERSISTENCE_UNIT_NAME = "gestionSallePU";
    private static EntityManagerFactory emFactory;

    public static EntityManager getEntityManager() {
        if (emFactory == null) {
            emFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return emFactory.createEntityManager();
    }

    public static void closeEntityManagerFactory() {
        if (emFactory != null) {
            emFactory.close();
        }
    }
}