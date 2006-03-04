package net.hyperic.sigar.test;

import net.hyperic.sigar.Sigar;
import net.hyperic.sigar.SigarException;
import net.hyperic.sigar.SigarNotImplementedException;
import net.hyperic.sigar.NetInterfaceConfig;
import net.hyperic.sigar.NetInterfaceStat;
import net.hyperic.sigar.NetFlags;

public class TestNetIf extends SigarTestCase {

    public TestNetIf(String name) {
        super(name);
    }

    private void getNetIflist(Sigar sigar, boolean getStats) throws Exception {
        String[] ifNames = sigar.getNetInterfaceList();

        for (int i=0; i<ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ifconfig =
                sigar.getNetInterfaceConfig(name);

            traceln("name=" + name);

            assertTrueTrace("Address", ifconfig.getAddress());
            assertTrueTrace("Netmask", ifconfig.getNetmask());

            if (!getStats) {
                continue;
            }

            if ((ifconfig.getFlags() & NetFlags.IFF_UP) <= 0) {
                traceln("!IFF_UP...skipping getNetInterfaceStat");
                continue;
            }

            try {
                NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);
                assertGtEqZeroTrace("RxPackets", ifstat.getRxPackets());
                assertGtEqZeroTrace("TxPackets", ifstat.getTxPackets());
            } catch (SigarNotImplementedException e) {
                //ok
            } catch (SigarException e) {
                fail("getNetInterfaceStat(" + name + "): " +
                     e.getMessage());
            }
        }
    }

    private void getGarbage(Sigar sigar) {
        //test bogus arg results in exception (and not a segfault)
        try {
            traceln("testing bogus getNetInterfaceStat");
            sigar.getNetInterfaceStat("were switching to night vision");
            fail("switched to night vision");
        } catch (SigarException e) {
            //expected
        }

        //test bogus arg results in exception (and not a segfault)
        try {
            traceln("testing bogus getNetInterfaceConfig");
            sigar.getNetInterfaceConfig("happy meal");
            fail("unexpected treat in happy meal");
        } catch (SigarException e) {
            //expected
        }
    }

    public void testCreate() throws Exception {
        Sigar sigar = getSigar();

        /* call twice to make sure caching works */
        getNetIflist(sigar, false);
        getNetIflist(sigar, false);
        getNetIflist(sigar, true);
        traceln("Default IP=" +
                sigar.getNetInterfaceConfig().getAddress());

        getGarbage(sigar);
    }
}
