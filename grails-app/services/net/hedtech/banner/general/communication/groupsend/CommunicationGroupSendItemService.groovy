/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.service.ServiceBase
import org.springframework.orm.jpa.JpaCallback
import org.springframework.security.core.context.SecurityContextHolder

import javax.persistence.EntityManager
import javax.persistence.Query

/**
 * Manages group send items.
 */
class CommunicationGroupSendItemService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationGroupSendItem groupSendItem = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationGroupSendItem
        if (groupSendItem.getCreationDateTime() == null) {
            groupSendItem.setCreationDateTime( new Date() )
        };
    }

    public List fetchByGroupSend( CommunicationGroupSend groupSend ) {
        return CommunicationGroupSendItem.fetchByGroupSend( groupSend )
    }

    public List fetchPending( int max ) {

    }


//    public List fetchPending( int max ) {
//        return getJpaTemplate().executeFind( new JpaCallback() {
//            public Object doInJpa( EntityManager em ) throws javax.persistence.PersistenceException {
//                Query query = em.createQuery( "SELECT gsi from CommunicationGroupSendItem gsi, CommunicationGroupSend gs where (gsi.groupSendKey = gs.id) and (gs.currentExecutionState IN (:processableGroupSendStates)) and (gsi.currentExecutionState = :itemState) order by gsi.creationDateTime asc" );
//
//
//                Query query = em.createQuery( "SELECT gsi from CommunicationGroupSendItem gsi, CommunicationGroupSend gs where (gsi.groupSendKey = gs.id) and (gs.currentExecutionState IN (:processableGroupSendStates)) and (gsi.currentExecutionState = :itemState) order by gsi.creationDateTime asc" );
//                query.setParameter( "processableGroupSendStates", processableGroupSendStates );
//                query.setParameter( "itemState", GroupSendItemExecutionState.Ready );
//                query.setMaxResults( max );
//                return query.getResultList();
//            }
//        });
//    }
}
