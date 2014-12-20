package com.tailf.jnc;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.StringWriter;

public class YangJsonParserTest {

    private final String ns = "http://acme.com/ns/simple/1.0";
    private final String prefix = "simple";

    DummyElement hosts = null;

    @Before
    public void setUp() {
        hosts = new DummyElement(ns,"hosts") {
            public String[] childrenNames() {
                return new String[] {
                        "host",
                        "users",
                };
            }
        };
        try {
            hosts.addChild(getHost("ftp-1", "1.2.2.2", 2));
            DummyElement host2 = getHost("web", "1.2.2.2", 1);
            host2.addChild(getProcesses("httpd", true, 9982));
            hosts.addChild(host2);
            hosts.getChild("host").addChild(getProcesses("ftpd", true, 12001));

        } catch (YangException e) {
            e.printStackTrace();
        }

    }
    private DummyElement getHost(String name, String ip, Number noOfServers) throws YangException {
        DummyElement host = new DummyElement("", "host") {
            public String[] childrenNames() {
                return new String[] {
                        "name",
                        "ip",
                        "numberOfServers",
                        "processes"
                };
            }
        };
        host.addChild(createLeaf("name", new YangString(name)));
        host.addChild(createLeaf("ip", new YangString(ip)));
        host.addChild(createLeaf("numberOfServers", new YangInt32(noOfServers)));
        return host;
    }

    private DummyElement getProcesses(String name, boolean enabled, int pid) throws YangException {
        DummyElement process = new DummyElement("", "processes") {
            public String[] childrenNames() {
                return new String[] {
                        "name",
                        "enabled",
                        "pid",
                };
            }
        };
        process.addChild(createLeaf("name", new YangString(name)));
        process.addChild(createLeaf("enabled", new YangString(Boolean.toString(enabled))));
        process.addChild(createLeaf("pid", new YangString(Integer.toString(pid))));
        return process;
    }

    private DummyElement createLeaf(String name, YangType value) {
        DummyElement leaf = new DummyElement(ns,name);
        leaf.setValue(value);
        return leaf;
    }

    @Test
    public void testJsonWithoutSchema() {
        try {
            StringWriter writer = new StringWriter();
            hosts.toJson(writer, true);
            YangJsonParser parser = new YangJsonParser();
            Element parsedHost = parser.parse(writer.toString(), null);
            assertEquals(hosts.toJson(false), parsedHost.toJson(false));
        } catch (Exception e) {
            fail("Failed to test json with exception "+e.getMessage());
        }
    }
}