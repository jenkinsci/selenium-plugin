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

    public boolean isRegistered(RemoteControlProxy proxy) {
        for (RemoteControlProxy rc : all) {
            if (rc.equals(proxy)) {
                return true;
            }
        }
        return false;
    }

    public List<RemoteControlProxy> allRegisteredRemoteControls() {
        return new ArrayList<RemoteControlProxy>(all);
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
            if(rc.canHandleNewSession())
                r.add(rc);
        return r;
    }

    public void unregisterAllUnresponsiveRemoteControls() {
        for (RemoteControlProxy rc : all) {
            if (rc.unreliable()) {
                LOGGER.log(Level.WARNING, "Unregistering unreliable RC " + rc);
                unregister(rc);
            }
        }
    }

    public void recycleAllSessionsIdleForTooLong(double maxIdleTimeInSeconds) {
        final int maxIdleTimeInMilliseconds = (int) (maxIdleTimeInSeconds * 1000);
        /*if (session.innactiveForMoreThan(maxIdleTimeInMilliseconds)) {
            LOGGER.warn("Releasing session IDLE for more than " + maxIdleTimeInSeconds + " seconds: " + session);
            releaseForSession(session.sessionId());
        }*/
    }

    public synchronized RemoteControlProxy reserve(Environment env) {
        String[] keys = env.name().split("&");
        for (int i = 0; i < keys.length; i++)
            keys[i] = '/'+keys[i]+'/';

        while(true) {
            boolean hadMatch=false;
            for (RemoteControlProxy rc : all) {
                boolean doesMatch = matches(rc,keys);
                hadMatch |= doesMatch;
                if(doesMatch && rc.canHandleNewSession()) {
                    rc.registerNewSession();
                    return rc;
                }
            }

            // is there any point in waiting?
            if(!hadMatch) {
                if(all.isEmpty())
                    throw new IllegalArgumentException("No RCs available");
                else
                    throw new IllegalArgumentException("No RC satisfies the label criteria: "+env.name()+" - "+all);
            }

            try {
                wait();
            } catch (InterruptedException e) {
                // this is totally broken IMO, but the reserve method doesn't allow us to return
                LOGGER.log(Level.WARNING, "Interrupted while reserving remote control for "+env.name(), e);
            }
        }
    }

    private boolean matches(RemoteControlProxy rc, String[] keys) {
        for (String key : keys)
            if(!rc.environment().contains(key))
                return false;
        return true;
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

    public void updateSessionLastActiveAt(String sessionId) {
        sessions.get(sessionId).registerNewSession();
    }

    private static final Logger LOGGER = Logger.getLogger(HudsonRemoteControlPool.class.getName());
}
