/*******************************************************************************
 Copyright 2013-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall

import javax.persistence.*

/**
 * This table stores a mapping  between PIDM and UDC ID.
 */

@Entity
@Table(name = "GV_GOBUMAP")
@NamedQueries(value = [
@NamedQuery(name = "PidmAndUDCIdMapping.fetchByUdcId",
        query = """FROM  PidmAndUDCIdMapping a
           WHERE a.udcId = :udcId
       """),
@NamedQuery(name = "PidmAndUDCIdMapping.fetchByUdcList",
    query = """ FROM PidmAndUDCIdMapping a
        WHERE a.udcId IN :udcId
    """)
])
@NamedNativeQueries(value = [
@NamedNativeQuery(name = "PidmAndUDCIdMapping.fetchEnterpriseIdsByBannerIdList",
    query = """select gobumap_udc_id
  from gobumap
  inner join spriden on spriden_pidm = gobumap_pidm
 where spriden_id in (:banner_ids)
   and spriden_change_ind is null""",
    resultSetMapping="PidmAndUDCIdMapping.enterpriseId"),
@NamedNativeQuery(name = "PidmAndUDCIdMapping.generateByBannerIdList",
    query = """DECLARE
  lv_rowid_out VARCHAR2(18);
  CURSOR get_missing_udc_id_c IS
    SELECT spriden_pidm
      FROM spriden
     INNER JOIN TABLE(string_nt(:banner_ids)) ON column_value = spriden_id
                                             AND spriden_change_ind IS NULL
     WHERE NOT EXISTS
     (SELECT 'X' FROM gobumap WHERE gobumap_pidm = spriden.spriden_pidm);
BEGIN
  FOR result IN get_missing_udc_id_c LOOP
    gb_gobumap.p_create(p_udc_id      => gokuuid.f_create_unique_id(),
                        p_pidm        => result.spriden_pidm,
                        p_create_date => SYSDATE,
                        p_user_id     => gb_common.f_sct_user,
                        p_data_origin => gb_common.data_origin,
                        p_rowid_out   => lv_rowid_out);
  END LOOP;
END;""",
    resultSetMapping="PidmAndUDCIdMapping.generate")
])
@SqlResultSetMappings(value = [
@SqlResultSetMapping(name = "PidmAndUDCIdMapping.enterpriseId"),
@SqlResultSetMapping(name = "PidmAndUDCIdMapping.generate")
])

class PidmAndUDCIdMapping implements Serializable {

    /**
     * Surrogate ID for GOBUMAP
     */
    @Id
    @Column(name = "GOBUMAP_SURROGATE_ID")
    @SequenceGenerator(name = "GOBUMAP_SEQ_GEN", allocationSize = 1, sequenceName = "GOBUMAP_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GOBUMAP_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GOBUMAP
     */
    @Version
    @Column(name = "GOBUMAP_VERSION")
    Long version

    /**
     * UDC ID:  UDC Identifier.
     */
    @Column(name = "GOBUMAP_UDC_ID")
    String udcId

    /**
     * PIDM: Internal identification number of the person.
     */
    @Column(name = "GOBUMAP_PIDM")
    Integer pidm

    /**
     * CREATE DATE: Date on which the record was created.
     */
    @Column(name = "GOBUMAP_CREATE_DATE")
    @Temporal(TemporalType.DATE)
    Date createDate

    /**
     * ACTIVITY DATE: Date on which the record was created or last updated.
     */
    @Column(name = "GOBUMAP_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * User ID: User ID of the user who created or last updated the record.
     */
    @Column(name = "GOBUMAP_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row.
     */
    @Column(name = "GOBUMAP_DATA_ORIGIN")
    String dataOrigin


    public String toString() {
        """PidmAndUDCIdMapping[
					id=$id, 
					version=$version, 
					udcId=$udcId, 
					pidm=$pidm, 
					createDate=$createDate, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin]"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof PidmAndUDCIdMapping)) return false
        PidmAndUDCIdMapping that = (PidmAndUDCIdMapping) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (udcId != that.udcId) return false
        if (pidm != that.pidm) return false
        if (createDate != that.createDate) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (udcId != null ? udcId.hashCode() : 0)
        result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
        result = 31 * result + (createDate != null ? createDate.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        return result
    }

    static constraints = {
        pidm(nullable: false, min: -99999999, max: 99999999)
        createDate(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['udcId']


    public static PidmAndUDCIdMapping fetchByUdcId(String udcId) {
        def pidmAndUDCIdMapping = PidmAndUDCIdMapping.withSession { session ->
            session.getNamedQuery(
                    'PidmAndUDCIdMapping.fetchByUdcId')
                    .setString('udcId', udcId).uniqueResult()
        }
        return pidmAndUDCIdMapping
    }


    public static List fetchByUdcList(List udcId) {
        def pidmAndUDCIdMapping = PidmAndUDCIdMapping.withSession { session ->
            session.getNamedQuery(
                    'PidmAndUDCIdMapping.fetchByUdcList')
                    .setParameterList('udcId', udcId).list()
        }
        return pidmAndUDCIdMapping
    }


	public static List fetchEnterpriseIdsByBannerIdList(List bannerIds) {
		def enterpriseIds = PidmAndUDCIdMapping.withSession { session ->
			session.getNamedQuery(
					'PidmAndUDCIdMapping.fetchEnterpriseIdsByBannerIdList')
					.setParameterList('banner_ids', bannerIds).list()
		}
		return enterpriseIds
	}
	

	public static void generateByBannerIdList(List bannerIds) {
		PidmAndUDCIdMapping.withSession { session ->
			session.getNamedQuery(
					'PidmAndUDCIdMapping.generateByBannerIdList')
					.setParameterList('banner_ids', bannerIds).executeUpdate()
		}
	}

}
