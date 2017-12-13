/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans_sessiones;

import entidades.Tipopersona;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Felipe
 */
@Stateless
public class TipopersonaFacade extends AbstractFacade<Tipopersona> {

    @PersistenceContext(unitName = "PagaduriasPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public TipopersonaFacade() {
        super(Tipopersona.class);
    }
    
}
