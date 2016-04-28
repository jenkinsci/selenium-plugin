/**
 * 
 */
package hudson.plugins.selenium;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author Richard Lavoie
 * 
 */
@ExportedBean
public class SeleniumTestSlotGroup implements Comparable<SeleniumTestSlotGroup>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8967484262051147802L;

    private URL host;

    private List<SeleniumTestSlot> slots = new ArrayList<SeleniumTestSlot>();

    public SeleniumTestSlotGroup(URL host) {
        this.host = host;
    }

    public List<SeleniumTestSlot> getSlots() {
        return slots;
    }

    public void addTestSlot(SeleniumTestSlot slot) {
        this.slots.add(slot);
    }

    public String getHostAndPort() {
        return host.toExternalForm();
    }

    @Exported
    public String getHost() {
        return host.getHost();
    }

    @Exported
    public int getPort() {
        return host.getPort();
    }

    private static class BusyCounter {

        int free = 0;
        int count = 0;
    }

    public String getSummary() {
        Map<String, BusyCounter> counters = new TreeMap<String, BusyCounter>();

        for (SeleniumTestSlot slot : slots) {
            String browser = slot.getBrowserName();
            BusyCounter c = counters.get(browser);
            if (c == null) {
                c = new BusyCounter();
                counters.put(browser, c);
            }
            c.count++;
            if (!slot.isReserved()) {
                c.free++;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, BusyCounter> entry : counters.entrySet()) {
            BusyCounter counter = entry.getValue();
            sb.append(entry.getKey()).append(" ").append(counter.free).append("/").append(counter.count).append(", ");
        }

        return sb.substring(0, sb.length() - 2);
    }

    public int compareTo(SeleniumTestSlotGroup that) {
        int r = this.getHost().compareTo(that.getHost());
        if (r != 0)
            return r;
        return this.getPort() - that.getPort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SeleniumTestSlotGroup that = (SeleniumTestSlotGroup) o;

        return new EqualsBuilder()
                .append(host, that.host)
                .append(slots, that.slots)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(host)
                .append(slots)
                .toHashCode();
    }
}
