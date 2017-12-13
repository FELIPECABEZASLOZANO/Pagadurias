/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entidades;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Felipe
 */
@Entity
@Table(name = "tipopersona")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Tipopersona.findAll", query = "SELECT t FROM Tipopersona t"),
    @NamedQuery(name = "Tipopersona.findByIdTipo", query = "SELECT t FROM Tipopersona t WHERE t.idTipo = :idTipo"),
    @NamedQuery(name = "Tipopersona.findByNombreTipo", query = "SELECT t FROM Tipopersona t WHERE t.nombreTipo = :nombreTipo"),
    @NamedQuery(name = "Tipopersona.findByDescripcion", query = "SELECT t FROM Tipopersona t WHERE t.descripcion = :descripcion")})
public class Tipopersona implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "idTipo")
    private Integer idTipo;
    @Size(max = 50)
    @Column(name = "nombreTipo")
    private String nombreTipo;
    @Size(max = 500)
    @Column(name = "descripcion")
    private String descripcion;
    @OneToMany(mappedBy = "tipoPersona")
    private Collection<Cliente> clienteCollection;

    public Tipopersona() {
    }

    public Tipopersona(Integer idTipo) {
        this.idTipo = idTipo;
    }

    public Integer getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Integer idTipo) {
        this.idTipo = idTipo;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @XmlTransient
    public Collection<Cliente> getClienteCollection() {
        return clienteCollection;
    }

    public void setClienteCollection(Collection<Cliente> clienteCollection) {
        this.clienteCollection = clienteCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipo != null ? idTipo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Tipopersona)) {
            return false;
        }
        Tipopersona other = (Tipopersona) object;
        if ((this.idTipo == null && other.idTipo != null) || (this.idTipo != null && !this.idTipo.equals(other.idTipo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.Tipopersona[ idTipo=" + idTipo + " ]";
    }
    
}
