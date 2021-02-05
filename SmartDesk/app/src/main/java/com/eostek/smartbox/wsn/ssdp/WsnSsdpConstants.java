package com.eostek.smartbox.wsn.ssdp;

public class WsnSsdpConstants {
    /* New line definition */
    public static final String ADDRESS = "239.255.255.250";
    public static final int PORT = 1900;
    public static final String HOST = "Host:" + ADDRESS + ":" + PORT;
    
    public static final String MAN = "Man:\"ssdp:discover\"";
    public static final String NEWLINE = "\r\n";
    /* Definitions of start line */
    public static final String SL_NOTIFY = "NOTIFY * HTTP/1.1";
    public static final String SL_MSEARCH = "M-SEARCH * HTTP/1.1";
    public static final String SL_OK = "HTTP/1.1 200 OK";
    
    /* Definitions of search targets */
    public static final String ST_RootDevice = "ST:rootdevice";
    public static final String ST_ContentDirectory = "St: urn:schemas-upnp-org:service:ContentDirectory:1";
    public static final String ST_AVTransport = "St: urn:schemas-upnp-org:service:AVTransport:1";
    public static final String ST_Product = "St: urn:av-openhome-org:service:Product:1";

    /* Definitions of notification sub type */
    public static final String NTS_ALIVE = "NTS:ssdp:alive";
    public static final String NTS_BYE = "NTS:ssdp:byebye";
    public static final String NTS_UPDATE = "NTS:ssdp:update";
    
    
    public static final String ST_Product1 = "ST:urn:schemas-upnp-org:device:Server:1";
    public static final String Found = "ST=urn:schemas-upnp-org:device:";
    public static final String Root = "ST: urn:schemas-upnp-org:device:Server:1";
    public static final String ALL = "ST:miivii";
    public static final String ST_ALL = "ST:ssdp:all";
    
}
