/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dao.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidades.Detalle;
import entidades.Impuesto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Felipe
 */
public class ImpuestoJpaController implements Serializable {

    public ImpuestoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Impuesto impuesto) throws RollbackFailureException, Exception {
        if (impuesto.getDetalleCollection() == null) {
            impuesto.setDetalleCollection(new ArrayList<Detalle>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Detalle> attachedDetalleCollection = new ArrayList<Detalle>();
            for (Detalle detalleCollectionDetalleToAttach : impuesto.getDetalleCollection()) {
                detalleCollectionDetalleToAttach = em.getReference(detalleCollectionDetalleToAttach.getClass(), detalleCollectionDetalleToAttach.getIdDetalle());
                attachedDetalleCollection.add(detalleCollectionDetalleToAttach);
            }
            impuesto.setDetalleCollection(attachedDetalleCollection);
            em.persist(impuesto);
            for (Detalle detalleCollectionDetalle : impuesto.getDetalleCollection()) {
                Impuesto oldIdImpuestoOfDetalleCollectionDetalle = detalleCollectionDetalle.getIdImpuesto();
                detalleCollectionDetalle.setIdImpuesto(impuesto);
                detalleCollectionDetalle = em.merge(detalleCollectionDetalle);
                if (oldIdImpuestoOfDetalleCollectionDetalle != null) {
                    oldIdImpuestoOfDetalleCollectionDetalle.getDetalleCollection().remove(detalleCollectionDetalle);
                    oldIdImpuestoOfDetalleCollectionDetalle = em.merge(oldIdImpuestoOfDetalleCollectionDetalle);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Impuesto impuesto) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Impuesto persistentImpuesto = em.find(Impuesto.class, impuesto.getIdImpuesto());
            Collection<Detalle> detalleCollectionOld = persistentImpuesto.getDetalleCollection();
            Collection<Detalle> detalleCollectionNew = impuesto.getDetalleCollection();
            Collection<Detalle> attachedDetalleCollectionNew = new ArrayList<Detalle>();
            for (Detalle detalleCollectionNewDetalleToAttach : detalleCollectionNew) {
                detalleCollectionNewDetalleToAttach = em.getReference(detalleCollectionNewDetalleToAttach.getClass(), detalleCollectionNewDetalleToAttach.getIdDetalle());
                attachedDetalleCollectionNew.add(detalleCollectionNewDetalleToAttach);
            }
            detalleCollectionNew = attachedDetalleCollectionNew;
            impuesto.setDetalleCollection(detalleCollectionNew);
            impuesto = em.merge(impuesto);
            for (Detalle detalleCollectionOldDetalle : detalleCollectionOld) {
                if (!detalleCollectionNew.contains(detalleCollectionOldDetalle)) {
                    detalleCollectionOldDetalle.setIdImpuesto(null);
                    detalleCollectionOldDetalle = em.merge(detalleCollectionOldDetalle);
                }
            }
            for (Detalle detalleCollectionNewDetalle : detalleCollectionNew) {
                if (!detalleCollectionOld.contains(detalleCollectionNewDetalle)) {
                    Impuesto oldIdImpuestoOfDetalleCollectionNewDetalle = detalleCollectionNewDetalle.getIdImpuesto();
                    detalleCollectionNewDetalle.setIdImpuesto(impuesto);
                    detalleCollectionNewDetalle = em.merge(detalleCollectionNewDetalle);
                    if (oldIdImpuestoOfDetalleCollectionNewDetalle != null && !oldIdImpuestoOfDetalleCollectionNewDetalle.equals(impuesto)) {
                        oldIdImpuestoOfDetalleCollectionNewDetalle.getDetalleCollection().remove(detalleCollectionNewDetalle);
                        oldIdImpuestoOfDetalleCollectionNewDetalle = em.merge(oldIdImpuestoOfDetalleCollectionNewDetalle);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = impuesto.getIdImpuesto();
                if (findImpuesto(id) == null) {
                    throw new NonexistentEntityException("The impuesto with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Impuesto impuesto;
            try {
                impuesto = em.getReference(Impuesto.class, id);
                impuesto.getIdImpuesto();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The impuesto with id " + id + " no longer exists.", enfe);
            }
            Collection<Detalle> detalleCollection = impuesto.getDetalleCollection();
            for (Detalle detalleCollectionDetalle : detalleCollection) {
                detalleCollectionDetalle.setIdImpuesto(null);
                detalleCollectionDetalle = em.merge(detalleCollectionDetalle);
            }
            em.remove(impuesto);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Impuesto> findImpuestoEntities() {
        return findImpuestoEntities(true, -1, -1);
    }

    public List<Impuesto> findImpuestoEntities(int maxResults, int firstResult) {
        return findImpuestoEntities(false, maxResults, firstResult);
    }

    private List<Impuesto> findImpuestoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Impuesto.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Impuesto findImpuesto(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Impuesto.class, id);
        } finally {
            em.close();
        }
    }

    public int getImpuestoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Impuesto> rt = cq.from(Impuesto.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
