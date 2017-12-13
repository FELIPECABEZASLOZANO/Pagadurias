/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import dao.exceptions.NonexistentEntityException;
import dao.exceptions.PreexistingEntityException;
import dao.exceptions.RollbackFailureException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import entidades.Empleado;
import entidades.Cliente;
import entidades.Detalle;
import entidades.Factura;
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
public class FacturaJpaController implements Serializable {

    public FacturaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Factura factura) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (factura.getDetalleCollection() == null) {
            factura.setDetalleCollection(new ArrayList<Detalle>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Empleado idEmpleado = factura.getIdEmpleado();
            if (idEmpleado != null) {
                idEmpleado = em.getReference(idEmpleado.getClass(), idEmpleado.getCodigoEmpleado());
                factura.setIdEmpleado(idEmpleado);
            }
            Cliente idCliente = factura.getIdCliente();
            if (idCliente != null) {
                idCliente = em.getReference(idCliente.getClass(), idCliente.getIdCliente());
                factura.setIdCliente(idCliente);
            }
            Collection<Detalle> attachedDetalleCollection = new ArrayList<Detalle>();
            for (Detalle detalleCollectionDetalleToAttach : factura.getDetalleCollection()) {
                detalleCollectionDetalleToAttach = em.getReference(detalleCollectionDetalleToAttach.getClass(), detalleCollectionDetalleToAttach.getIdDetalle());
                attachedDetalleCollection.add(detalleCollectionDetalleToAttach);
            }
            factura.setDetalleCollection(attachedDetalleCollection);
            em.persist(factura);
            if (idEmpleado != null) {
                idEmpleado.getFacturaCollection().add(factura);
                idEmpleado = em.merge(idEmpleado);
            }
            if (idCliente != null) {
                idCliente.getFacturaCollection().add(factura);
                idCliente = em.merge(idCliente);
            }
            for (Detalle detalleCollectionDetalle : factura.getDetalleCollection()) {
                Factura oldIdFacturaOfDetalleCollectionDetalle = detalleCollectionDetalle.getIdFactura();
                detalleCollectionDetalle.setIdFactura(factura);
                detalleCollectionDetalle = em.merge(detalleCollectionDetalle);
                if (oldIdFacturaOfDetalleCollectionDetalle != null) {
                    oldIdFacturaOfDetalleCollectionDetalle.getDetalleCollection().remove(detalleCollectionDetalle);
                    oldIdFacturaOfDetalleCollectionDetalle = em.merge(oldIdFacturaOfDetalleCollectionDetalle);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findFactura(factura.getIdFactura()) != null) {
                throw new PreexistingEntityException("Factura " + factura + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Factura factura) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Factura persistentFactura = em.find(Factura.class, factura.getIdFactura());
            Empleado idEmpleadoOld = persistentFactura.getIdEmpleado();
            Empleado idEmpleadoNew = factura.getIdEmpleado();
            Cliente idClienteOld = persistentFactura.getIdCliente();
            Cliente idClienteNew = factura.getIdCliente();
            Collection<Detalle> detalleCollectionOld = persistentFactura.getDetalleCollection();
            Collection<Detalle> detalleCollectionNew = factura.getDetalleCollection();
            if (idEmpleadoNew != null) {
                idEmpleadoNew = em.getReference(idEmpleadoNew.getClass(), idEmpleadoNew.getCodigoEmpleado());
                factura.setIdEmpleado(idEmpleadoNew);
            }
            if (idClienteNew != null) {
                idClienteNew = em.getReference(idClienteNew.getClass(), idClienteNew.getIdCliente());
                factura.setIdCliente(idClienteNew);
            }
            Collection<Detalle> attachedDetalleCollectionNew = new ArrayList<Detalle>();
            for (Detalle detalleCollectionNewDetalleToAttach : detalleCollectionNew) {
                detalleCollectionNewDetalleToAttach = em.getReference(detalleCollectionNewDetalleToAttach.getClass(), detalleCollectionNewDetalleToAttach.getIdDetalle());
                attachedDetalleCollectionNew.add(detalleCollectionNewDetalleToAttach);
            }
            detalleCollectionNew = attachedDetalleCollectionNew;
            factura.setDetalleCollection(detalleCollectionNew);
            factura = em.merge(factura);
            if (idEmpleadoOld != null && !idEmpleadoOld.equals(idEmpleadoNew)) {
                idEmpleadoOld.getFacturaCollection().remove(factura);
                idEmpleadoOld = em.merge(idEmpleadoOld);
            }
            if (idEmpleadoNew != null && !idEmpleadoNew.equals(idEmpleadoOld)) {
                idEmpleadoNew.getFacturaCollection().add(factura);
                idEmpleadoNew = em.merge(idEmpleadoNew);
            }
            if (idClienteOld != null && !idClienteOld.equals(idClienteNew)) {
                idClienteOld.getFacturaCollection().remove(factura);
                idClienteOld = em.merge(idClienteOld);
            }
            if (idClienteNew != null && !idClienteNew.equals(idClienteOld)) {
                idClienteNew.getFacturaCollection().add(factura);
                idClienteNew = em.merge(idClienteNew);
            }
            for (Detalle detalleCollectionOldDetalle : detalleCollectionOld) {
                if (!detalleCollectionNew.contains(detalleCollectionOldDetalle)) {
                    detalleCollectionOldDetalle.setIdFactura(null);
                    detalleCollectionOldDetalle = em.merge(detalleCollectionOldDetalle);
                }
            }
            for (Detalle detalleCollectionNewDetalle : detalleCollectionNew) {
                if (!detalleCollectionOld.contains(detalleCollectionNewDetalle)) {
                    Factura oldIdFacturaOfDetalleCollectionNewDetalle = detalleCollectionNewDetalle.getIdFactura();
                    detalleCollectionNewDetalle.setIdFactura(factura);
                    detalleCollectionNewDetalle = em.merge(detalleCollectionNewDetalle);
                    if (oldIdFacturaOfDetalleCollectionNewDetalle != null && !oldIdFacturaOfDetalleCollectionNewDetalle.equals(factura)) {
                        oldIdFacturaOfDetalleCollectionNewDetalle.getDetalleCollection().remove(detalleCollectionNewDetalle);
                        oldIdFacturaOfDetalleCollectionNewDetalle = em.merge(oldIdFacturaOfDetalleCollectionNewDetalle);
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
                Integer id = factura.getIdFactura();
                if (findFactura(id) == null) {
                    throw new NonexistentEntityException("The factura with id " + id + " no longer exists.");
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
            Factura factura;
            try {
                factura = em.getReference(Factura.class, id);
                factura.getIdFactura();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The factura with id " + id + " no longer exists.", enfe);
            }
            Empleado idEmpleado = factura.getIdEmpleado();
            if (idEmpleado != null) {
                idEmpleado.getFacturaCollection().remove(factura);
                idEmpleado = em.merge(idEmpleado);
            }
            Cliente idCliente = factura.getIdCliente();
            if (idCliente != null) {
                idCliente.getFacturaCollection().remove(factura);
                idCliente = em.merge(idCliente);
            }
            Collection<Detalle> detalleCollection = factura.getDetalleCollection();
            for (Detalle detalleCollectionDetalle : detalleCollection) {
                detalleCollectionDetalle.setIdFactura(null);
                detalleCollectionDetalle = em.merge(detalleCollectionDetalle);
            }
            em.remove(factura);
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

    public List<Factura> findFacturaEntities() {
        return findFacturaEntities(true, -1, -1);
    }

    public List<Factura> findFacturaEntities(int maxResults, int firstResult) {
        return findFacturaEntities(false, maxResults, firstResult);
    }

    private List<Factura> findFacturaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Factura.class));
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

    public Factura findFactura(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Factura.class, id);
        } finally {
            em.close();
        }
    }

    public int getFacturaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Factura> rt = cq.from(Factura.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
