/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation used to indicate that a property should be excluded from
 * consideration by the validation advisor.  This is used to prevent
 * cyclic validation (e.g., when a parent-child relationship is bi-directional,
 * we'll want to traverse this relationship in only one direction).
 *
 * @author charlie hardt
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface CommunicationExcludeFromValidation {

}
