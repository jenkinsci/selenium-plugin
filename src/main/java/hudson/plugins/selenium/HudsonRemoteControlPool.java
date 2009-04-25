package hudson.plugins.selenium;

import com.thoughtworks.selenium.grid.hub.Environment;
import com.thoughtworks.selenium.grid.hub.remotecontrol.DynamicRemoteControlPool;
import com.thoughtworks.selenium.grid.hub.remotecontrol.RemoteControlProxy;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * {@link DynamicRemoteControlPool} that uses labels for matching.
 *
 * <P>
 * The "environment" that Selenium see is '/'-separated list of labels for the slave
 * (like /a/b/c/d), and we match incoming label to determine the {@link RemoteControlProxy}. 
 *
 * @author Kohsuke Kawaguchi
 */
public class HudsonRemoteControlPool implements DynamicRemoteControlPool {
    private final Set<RemoteControlProxy> all = new HashSet<RemoteControlProxy>();
    private final Map<String,RemoteControlProxy> sessions = new HashMap<String, RemoteControlProxy>();

    public synchronized void register(RemoteControlProxy rc) {
        all.add(rc);
    }

    public synchronized boolean unregister(RemoteControlProxy rc) {
        return all.remove(rc);
    }

    public synchronized List<RemoteControlProxy> availableRemoteControls() {
        List<RemoteControlProxy> r = new ArrayList<RemoteControlProxy>(all.size());
        for (RemoteControlProxy rc : all)
            if(rc.canHandleNewSession())
                r.add(rc);
        return r;
    }

    public synchronized List<RemoteControlProxy> reservedRemoteControls() {
        List<RemoteControlProxy> r = new ArrayList<RemoteControlProxy>(all.size());
        for (RemoteControlProxy rc : all)
            if(rc.concurrentSesssionCount() > 0)
                r.add(rc);
        return r;
    }

    public synchronized RemoteControlProxy reserve(Environment env) {
        String key = '/'+env.name()+'/';
        while(true) {
            for (RemoteControlProxy rc : all) {
                if((rc.environment().contains(key) || key.equals("/*/")) && rc.canHandleNewSession()) {
                    rc.registerNewSession();
                    return rc;
                }
            }
            try {
                wait();
            } catch (InterruptedException e) {
                // this is totally broken IMO, but the reserve method doesn't allow us to return
                LOGGER.log(Level.WARNING, "Interrupted while reserving remote control for "+env.name(), e);
            }
        }
    }

    public synchronized void release(RemoteControlProxy rc) {
        rc.unregisterSession();
        notifyAll();
    }

    public synchronized void associateWithSession(RemoteControlProxy rc, String sessionId) {
        RemoteControlProxy old = sessions.put(sessionId, rc);
        if(old!=null)
            throw new IllegalStateException("Session ID "+sessionId+" is already used by "+old);
    }

    public synchronized RemoteControlProxy retrieve(String sessionId) {
        return sessions.get(sessionId);
    }

    public synchronized void releaseForSession(String sessionId) {
        sessions.remove(sessionId).unregisterSession();
    }

    private static final Logger LOGGER = Logger.getLogger(HudsonRemoteControlPool.class.getName());
}
