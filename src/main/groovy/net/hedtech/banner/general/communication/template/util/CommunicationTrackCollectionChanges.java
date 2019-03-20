/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template.util;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used to indicate if a collection supports tracking of
 * additions and deletions.  DAOs may use the existence of this annotation to
 * determine whether processing of new and removed elements is needed when
 * persisting an Entity.  Specify delete=true to delete the tracked
 * element when its association with the Entity is deleted. Otherwise, the
 * tracked element will remain in the database when delete=false.
 *
 * @author charlie.hardt
 * @author brian.bell
 */
@Retention(RUNTIME)
public @interface CommunicationTrackCollectionChanges {
    boolean delete();
}
