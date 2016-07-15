/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.service.DatabaseModifiesState

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 * Address Role Privileges Table
 */
@Entity
@Table(name = "GORADRL")
@NamedQueries(value = [
@NamedQuery(name = "AddressRolePrivileges.fetchPrivilegedByRole",
        query = """FROM AddressRolePrivileges a
                             WHERE  role = :role
                             AND  (a.privilegeIndicator = 'D' or a.privilegeIndicator = 'U')
                    """),
@NamedQuery(name = "AddressRolePrivileges.fetchUpdatePrivsByRoleList",
                query = """FROM AddressRolePrivileges a
                             WHERE  role IN :roles
                             AND  a.privilegeIndicator = 'U'
                    """),
@NamedQuery(name = "AddressRolePrivileges.fetchUpdatePrivByCodeAndRoles",
                query = """FROM AddressRolePrivileges a
                             WHERE  role IN :roles
                             AND  a.privilegeIndicator = 'U'
                             and  a.addressType.code = :code
                    """)
])
@DatabaseModifiesState
class AddressRolePrivileges  implements Serializable{

    /**
     * Surrogate ID for GORADRL
     */
    @Id
    @Column(name = "GORADRL_SURROGATE_ID")
    @SequenceGenerator(name = "GORADRL_SEQ_GEN", allocationSize = 1, sequenceName = "GORADRL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORADRL_SEQ_GEN")

    Long id

    /**
     * Optimistic lock token for GOREMAL
     */
    @Version
    @Column(name = "GORADRL_VERSION")
    Long version

    /**
     * The privileged role
     */
    @Column(name = "GORADRL_ROLE")
    String role

    /**
     * The privilege indicator. Possible values are (U)pdate, (D)isplay, or (N)one.
     */
    @Column(name = "GORADRL_PRIV_IND")
    String privilegeIndicator

    /**
     * The date on which the row was added or modified.
     */
    @Column(name = "GORADRL_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * The user id when the row was added or modified.
     */
    @Column(name = "GORADRL_USER_ID", length=30)
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row
     */
    @Column(name = "GORADRL_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FK1_GORADRL_INV_STVATYP_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GORADRL_ATYP_CODE", referencedColumnName = "STVATYP_CODE")
    ])
    AddressType addressType


    public String toString() {
        """AddressRolePrivileges[
            id=${id},
            version=${version},
            role=${role},
            privilegeIndicator=${privilegeIndicator},
            lastModified=${lastModified},
            lastModifiedBy=${lastModifiedBy},
            dataOrigin=${dataOrigin},
            addressType=${addressType}
        ]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof AddressRolePrivileges)) return false
        AddressRolePrivileges that = (AddressRolePrivileges)o
        if (id != that.id) return false
        if (version != that.version) return false
        if (role != that.role) return false
        if (privilegeIndicator != that.privilegeIndicator) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (addressType != that.addressType) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (role != null ? role.hashCode() : 0)
        result = 31 * result + (privilegeIndicator != null ? privilegeIndicator.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (addressType != null ? addressType.hashCode() : 0)
        return result
    }


    static constraints = {
        version(nullable: true, precision: 19)
        role(nullable: false, maxSize: 30)
        privilegeIndicator(nullable: false, maxSize: 1)
        lastModified(nullable: false)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        addressType(nullable: false)
    }


    static def fetchPrivilegedByRole(String role) {
        AddressRolePrivileges.withSession { session ->
            return session.getNamedQuery('AddressRolePrivileges.fetchPrivilegedByRole').setString('role', role).list()

        }
    }

    static def fetchUpdatePrivsByRoleList(List roles) {
        def result
        result = AddressRolePrivileges.withSession { session ->
            session.getNamedQuery('AddressRolePrivileges.fetchUpdatePrivsByRoleList')
                    .setParameterList('roles', roles).list()
        }
        return result
    }

    static def fetchUpdatePrivByCodeAndRoles(List roles, String code) {
        def result
        result = AddressRolePrivileges.withSession { session ->
            session.getNamedQuery('AddressRolePrivileges.fetchUpdatePrivByCodeAndRoles')
                    .setParameterList('roles', roles)
                    .setString('code', code).list()[0]
        }
        return result
    }

}
