/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dao.exceptions.RollbackFailureException;
import entidades.Detalle;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidades.Impuesto;
import entidades.Factura;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Felipe
 */
public class DetalleJpaController implements Serializable {

    public DetalleJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Detalle detalle) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Impuesto idImpuesto = detalle.getIdImpuesto();
            if (idImpuesto != null) {
                idImpuesto = em.getReference(idImpuesto.getClass(), idImpuesto.getIdImpuesto());
                detalle.setIdImpuesto(idImpuesto);
            }
            Factura idFactura = detalle.getIdFactura();
            if (idFactura != null) {
                idFactura = em.getReference(idFactura.getClass(), idFactura.getIdFactura());
                detalle.setIdFactura(idFactura);
            }
            em.persist(detalle);
            if (idImpuesto != null) {
                idImpuesto.getDetalleCollection().add(detalle);
                idImpuesto = em.merge(idImpuesto);
            }
            if (idFactura != null) {
                idFactura.getDetalleCollection().add(detalle);
                idFactura = em.merge(idFactura);
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

    public void edit(Detalle detalle) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Detalle persistentDetalle = em.find(Detalle.class, detalle.getIdDetalle());
            Impuesto idImpuestoOld = persistentDetalle.getIdImpuesto();
            Impuesto idImpuestoNew = detalle.getIdImpuesto();
            Factura idFacturaOld = persistentDetalle.getIdFactura();
            Factura idFacturaNew = detalle.getIdFactura();
            if (idImpuestoNew != null) {
                idImpuestoNew = em.getReference(idImpuestoNew.getClass(), idImpuestoNew.getIdImpuesto());
                detalle.setIdImpuesto(idImpuestoNew);
            }
            if (idFacturaNew != null) {
                idFacturaNew = em.getReference(idFacturaNew.getClass(), idFacturaNew.getIdFactura());
                detalle.setIdFactura(idFacturaNew);
            }
            detalle = em.merge(detalle);
            if (idImpuestoOld != null && !idImpuestoOld.equals(idImpuestoNew)) {
                idImpuestoOld.getDetalleCollection().remove(detalle);
                idImpuestoOld = em.merge(idImpuestoOld);
            }
            if (idImpuestoNew != null && !idImpuestoNew.equals(idImpuestoOld)) {
                idImpuestoNew.getDetalleCollection().add(detalle);
                idImpuestoNew = em.merge(idImpuestoNew);
            }
            if (idFacturaOld != null && !idFacturaOld.equals(idFacturaNew)) {
                idFacturaOld.getDetalleCollection().remove(detalle);
                idFacturaOld = em.merge(idFacturaOld);
            }
            if (idFacturaNew != null && !idFacturaNew.equals(idFacturaOld)) {
                idFacturaNew.getDetalleCollection().add(detalle);
                idFacturaNew = em.merge(idFacturaNew);
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
                Integer id = detalle.getIdDetalle();
                if (findDetalle(id) == null) {
                    throw new NonexistentEntityException("The detalle with id " + id + " no longer exists.");
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
            Detalle detalle;
            try {
                detalle = em.getReference(Detalle.class, id);
                detalle.getIdDetalle();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The detalle with id " + id + " no longer exists.", enfe);
            }
            Impuesto idImpuesto = detalle.getIdImpuesto();
            if (idImpuesto != null) {
                idImpuesto.getDetalleCollection().remove(detalle);
                idImpuesto = em.merge(idImpuesto);
            }
            Factura idFactura = detalle.getIdFactura();
            if (idFactura != null) {
                idFactura.getDetalleCollection().remove(detalle);
                idFactura = em.merge(idFactura);
            }
            em.remove(detalle);
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

    public List<Detalle> findDetalleEntities() {
        return findDetalleEntities(true, -1, -1);
    }

    public List<Detalle> findDetalleEntities(int maxResults, int firstResult) {
        return findDetalleEntities(false, maxResults, firstResult);
    }

    private List<Detalle> findDetalleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Detalle.class));
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

    public Detalle findDetalle(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Detalle.class, id);
        } finally {
            em.close();
        }
    }

    public int getDetalleCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Detalle> rt = cq.from(Detalle.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
