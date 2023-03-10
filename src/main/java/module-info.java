module locallink.server {
    requires locallink.network;
    requires static lombok;
    requires javax.jmdns;
    requires org.apache.commons.collections4;

    exports fr.emevel.locallink.server;
    exports fr.emevel.locallink.server.sync;
}