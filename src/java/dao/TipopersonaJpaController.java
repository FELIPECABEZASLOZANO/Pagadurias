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
import entidades.Cliente;
import entidades.Tipopersona;
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
public class TipopersonaJpaController implements Serializable {

    public TipopersonaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Tipopersona tipopersona) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (tipopersona.getClienteCollection() == null) {
            tipopersona.setClienteCollection(new ArrayList<Cliente>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Cliente> attachedClienteCollection = new ArrayList<Cliente>();
            for (Cliente clienteCollectionClienteToAttach : tipopersona.getClienteCollection()) {
                clienteCollectionClienteToAttach = em.getReference(clienteCollectionClienteToAttach.getClass(), clienteCollectionClienteToAttach.getIdCliente());
                attachedClienteCollection.add(clienteCollectionClienteToAttach);
            }
            tipopersona.setClienteCollection(attachedClienteCollection);
            em.persist(tipopersona);
            for (Cliente clienteCollectionCliente : tipopersona.getClienteCollection()) {
                Tipopersona oldTipoPersonaOfClienteCollectionCliente = clienteCollectionCliente.getTipoPersona();
                clienteCollectionCliente.setTipoPersona(tipopersona);
                clienteCollectionCliente = em.merge(clienteCollectionCliente);
                if (oldTipoPersonaOfClienteCollectionCliente != null) {
                    oldTipoPersonaOfClienteCollectionCliente.getClienteCollection().remove(clienteCollectionCliente);
                    oldTipoPersonaOfClienteCollectionCliente = em.merge(oldTipoPersonaOfClienteCollectionCliente);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findTipopersona(tipopersona.getIdTipo()) != null) {
                throw new PreexistingEntityException("Tipopersona " + tipopersona + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Tipopersona tipopersona) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Tipopersona persistentTipopersona = em.find(Tipopersona.class, tipopersona.getIdTipo());
            Collection<Cliente> clienteCollectionOld = persistentTipopersona.getClienteCollection();
            Collection<Cliente> clienteCollectionNew = tipopersona.getClienteCollection();
            Collection<Cliente> attachedClienteCollectionNew = new ArrayList<Cliente>();
            for (Cliente clienteCollectionNewClienteToAttach : clienteCollectionNew) {
                clienteCollectionNewClienteToAttach = em.getReference(clienteCollectionNewClienteToAttach.getClass(), clienteCollectionNewClienteToAttach.getIdCliente());
                attachedClienteCollectionNew.add(clienteCollectionNewClienteToAttach);
            }
            clienteCollectionNew = attachedClienteCollectionNew;
            tipopersona.setClienteCollection(clienteCollectionNew);
            tipopersona = em.merge(tipopersona);
            for (Cliente clienteCollectionOldCliente : clienteCollectionOld) {
                if (!clienteCollectionNew.contains(clienteCollectionOldCliente)) {
                    clienteCollectionOldCliente.setTipoPersona(null);
                    clienteCollectionOldCliente = em.merge(clienteCollectionOldCliente);
                }
            }
            for (Cliente clienteCollectionNewCliente : clienteCollectionNew) {
                if (!clienteCollectionOld.contains(clienteCollectionNewCliente)) {
                    Tipopersona oldTipoPersonaOfClienteCollectionNewCliente = clienteCollectionNewCliente.getTipoPersona();
                    clienteCollectionNewCliente.setTipoPersona(tipopersona);
                    clienteCollectionNewCliente = em.merge(clienteCollectionNewCliente);
                    if (oldTipoPersonaOfClienteCollectionNewCliente != null && !oldTipoPersonaOfClienteCollectionNewCliente.equals(tipopersona)) {
                        oldTipoPersonaOfClienteCollectionNewCliente.getClienteCollection().remove(clienteCollectionNewCliente);
                        oldTipoPersonaOfClienteCollectionNewCliente = em.merge(oldTipoPersonaOfClienteCollectionNewCliente);
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
                Integer id = tipopersona.getIdTipo();
                if (findTipopersona(id) == null) {
                    throw new NonexistentEntityException("The tipopersona with id " + id + " no longer exists.");
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
            Tipopersona tipopersona;
            try {
                tipopersona = em.getReference(Tipopersona.class, id);
                tipopersona.getIdTipo();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipopersona with id " + id + " no longer exists.", enfe);
            }
            Collection<Cliente> clienteCollection = tipopersona.getClienteCollection();
            for (Cliente clienteCollectionCliente : clienteCollection) {
                clienteCollectionCliente.setTipoPersona(null);
                clienteCollectionCliente = em.merge(clienteCollectionCliente);
            }
            em.remove(tipopersona);
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

    public List<Tipopersona> findTipopersonaEntities() {
        return findTipopersonaEntities(true, -1, -1);
    }

    public List<Tipopersona> findTipopersonaEntities(int maxResults, int firstResult) {
        return findTipopersonaEntities(false, maxResults, firstResult);
    }

    private List<Tipopersona> findTipopersonaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Tipopersona.class));
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

    public Tipopersona findTipopersona(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Tipopersona.class, id);
        } finally {
            em.close();
        }
    }

    public int getTipopersonaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Tipopersona> rt = cq.from(Tipopersona.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
