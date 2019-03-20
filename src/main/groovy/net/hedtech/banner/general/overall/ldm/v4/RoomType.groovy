/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.overall.ldm.v4

/**
 * Decorator for room-types resource
 */
class RoomType {
    String id
    String title
    String type

    RoomType(String id,String title,String type) {
        this.id = id
        this.title=title
        this.type=type
    }


    @Override
    public String toString() {
        return "RoomType{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
