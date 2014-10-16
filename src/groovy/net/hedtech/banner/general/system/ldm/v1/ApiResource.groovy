package net.hedtech.banner.general.system.ldm.v1

class ApiResource {
    String resource
    Integer version
    String path
    List<String> events

    ApiResource ( String resource,Integer version, String path, List<String> events) {
        this.resource = resource
        this.version = version
        this.path = path
        this.events = events
    }
}
