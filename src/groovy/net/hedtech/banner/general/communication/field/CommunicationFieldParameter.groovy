/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Embeddable


@EqualsAndHashCode
@ToString
@Embeddable
public class CommunicationFieldParameter implements Serializable {
    /**
     * The display name to use in the template builder.
     * Can be changed at will.  Will not be used in any maps.
     */
    String displayName;
    /**
     * The description of the parameter to use in the template builder.
     */
    String description;
    /**
     * The internal, immutable key of the parameter.  Used when a reference to the parameter
     * is needed, as in a map.
     */
     Integer key;

     def CommunicationFieldParameter( String displayName, String description, Integer key ) {
        this.displayName = displayName;
        this.description = description;
        setKey( key );
    }

    def CommunicationFieldParameter( String displayName, String description ) {
        this.displayName = displayName;
        this.description = description;
    }


    /**
     * Sets the (immutable) name.  The key can be used in maps, etc.
     * @param key
     */
    public void setKey( Integer key ) {
        if ((key != null)&&(key < 0)) throw new IllegalArgumentException( "key must be non-negative" );
        this.key = key;
    }

    @Override
    public boolean equals( Object o ) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommunicationFieldParameter that = (CommunicationFieldParameter) o;

        if (description != null ? !description.equals( that.description ) : that.description != null) {
            return false;
        }
        if (displayName != null ? !displayName.equals( that.displayName ) : that.displayName != null) {
            return false;
        }
        if (key != null ? !key.equals( that.key ) : that.key != null) {
            return false;
        }

        return true;
    }

}
