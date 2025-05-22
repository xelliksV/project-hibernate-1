package com.game.repository;

import com.game.entity.Player;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    Logger logger = LogManager.getLogger();
    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "password");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }
    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        List<Player> result = null;
        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> nativeQuery = session.createNativeQuery("SELECT * FROM player LIMIT :siz OFFSET :off", Player.class);
            int offset = 0;
            for (int i = 0; i < pageNumber; i++) {
                offset += pageSize;
            }
            nativeQuery.setParameter("off", offset);
            nativeQuery.setParameter("siz", pageSize);
            result = nativeQuery.list();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "lol");
        return result;
    }

    @Override
    public int getAllCount() {
        int result = 0;
        try (Session session = sessionFactory.openSession()) {
            Query<Player> getCount = session.createNativeQuery("SELECT * FROM player", Player.class);
            result = getCount.list().size();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Player save(Player player) {
        Player saved = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return saved;
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.update(player);
            transaction.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        Optional<Player> result = Optional.empty();
        try (Session session = sessionFactory.openSession()) {
            Query<Player> query = session.createQuery("from Player where id = :num", Player.class);
            query.setParameter("num", id);
            Stream<Player> resultStream = query.getResultStream();
            result = resultStream.findFirst();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.delete(player);
            transaction.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}