/*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.overall

import javax.persistence.*
 
/**
 * Bank Routing Number Table entity.
 */
@Entity
@Table(name = "GXVDIRD")
class BankRoutingInfo implements Serializable {

    /**
     * BANK ROUTING NUMBER:  Routing number for bank.
     */
    @Id
    @Column(name = "GXVDIRD_CODE_BANK_ROUT_NUM")
    String bankRoutingNum

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GXVDIRD_VERSION")
    Long version

    /**
     * BANK NAME:  Description contains name of bank.
     */
    @Column(name = "GXVDIRD_DESC")
    String bankName

    public String toString() {
        """BankRoutingInfo [
            bankRoutingNum= $bankRoutingNum,
            version= $version,
            bankName= $bankName]"""
    }
    
    static constraints = {
        bankRoutingNum(nullable: false, maxSize: 11)
        bankName(nullable: false, maxSize: 60)
    }
}