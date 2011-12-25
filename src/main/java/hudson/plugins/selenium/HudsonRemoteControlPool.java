package hudson.plugins.selenium;

/**
 * {@link DynamicRemoteControlPool} that uses labels for matching.
 *
 * <P>
 * The "environment" that Selenium see is '/'-separated list of labels for the slave
 * (like /a/b/c/d), and we match incoming label to determine the {@link RemoteControlProxy}. 
 *
 * @author Kohsuke Kawaguchi
 */
class HudsonRemoteControlPool {}
//public class HudsonRemoteControlPool implements DynamicRemoteControlPool {
//    private final Set<RemoteControlProxy> all = new HashSet<RemoteControlProxy>();
//    private final Map<String,RemoteControlSession> sessions = new HashMap<String, RemoteControlSession>();
//
//    public synchronized void register(RemoteControlProxy rc) {
//        all.add(rc);
//    }
//
//    public synchronized boolean unregister(RemoteControlProxy rc) {
//        return all.remove(rc);
//    }
//
//    public boolean isRegistered(RemoteControlProxy proxy) {
//        return all.contains(proxy);
//    }
//
//    public List<RemoteControlProxy> allRegisteredRemoteControls() {
//        return new ArrayList<RemoteControlProxy>(all);
//    }
//
//    public synchronized List<RemoteControlProxy> availableRemoteControls() {
//        List<RemoteControlProxy> r = new ArrayList<RemoteControlProxy>(all.size());
//        for (RemoteControlProxy rc : all)
//            if(rc.canHandleNewSession())
//                r.add(rc);
//        return r;
//    }
//
//    public synchronized List<RemoteControlProxy> reservedRemoteControls() {
//        List<RemoteControlProxy> r = new ArrayList<RemoteControlProxy>(all.size());
//        for (RemoteControlProxy rc : all)
//            if(rc.sessionInProgress())
//                r.add(rc);
//        return r;
//    }
//
//    public synchronized void unregisterAllUnresponsiveRemoteControls() {
//        for (RemoteControlProxy rc : all.toArray(new RemoteControlProxy[0])) {
//            if (rc.unreliable()) {
//                LOGGER.log(Level.WARNING, "Unregistering unreliable RC " + rc);
//                unregister(rc);
//            }
//        }
//    }
//
//    public synchronized void recycleAllSessionsIdleForTooLong(double maxIdleTimeInSeconds) {
//        final int maxIdleTimeInMilliseconds = (int) (maxIdleTimeInSeconds * 1000);
//
//        for (RemoteControlSession session : sessions.values().toArray(new RemoteControlSession[0])) {
//            if (session.innactiveForMoreThan(maxIdleTimeInMilliseconds))
//                releaseForSession(session.sessionId());
//        }
//    }
//
//    public synchronized RemoteControlProxy reserve(Environment env) {
//        String[] keys = env.name().split("&");
//        for (int i = 0; i < keys.length; i++)
//            keys[i] = '/'+keys[i]+'/';
//
//        while(true) {
//            boolean hadMatch=false;
//            for (RemoteControlProxy rc : all) {
//                boolean doesMatch = matches(rc,keys);
//                hadMatch |= doesMatch;
//                if(doesMatch && rc.canHandleNewSession()) {
//                    rc.registerNewSession();
//                    return rc;
//                }
//            }
//
//            // is there any point in waiting?
//            if(!hadMatch) {
//                if(all.isEmpty())
//                    throw new IllegalArgumentException("No RCs available");
//                else
//                    throw new IllegalArgumentException("No RC satisfies the label criteria: "+env.name()+" - "+all);
//            }
//
//            try {
//                wait();
//            } catch (InterruptedException e) {
//                // this is totally broken IMO, but the reserve method doesn't allow us to return
//                LOGGER.log(Level.WARNING, "Interrupted while reserving remote control for "+env.name(), e);
//            }
//        }
//    }
//
//    private boolean matches(RemoteControlProxy rc, String[] keys) {
//        for (String key : keys)
//            if(!rc.environment().contains(key))
//                return false;
//        return true;
//    }
//
//    public synchronized void release(RemoteControlProxy rc) {
//        rc.unregisterSession();
//        notifyAll();
//    }
//
//    public synchronized void associateWithSession(RemoteControlProxy rc, String sessionId) {
//        RemoteControlSession old = sessions.put(sessionId, new RemoteControlSession(sessionId,rc));
//        if(old!=null)
//            throw new IllegalStateException("Session ID "+sessionId+" is already used by "+old);
//    }
//
//    public synchronized RemoteControlProxy retrieve(String sessionId) {
//        RemoteControlSession session = sessions.get(sessionId);
//        if (session==null)  throw new NoSuchSessionException(sessionId);
//        return session.remoteControl();
//    }
//
//    public synchronized void releaseForSession(String sessionId) {
//        RemoteControlProxy rc = retrieve(sessionId);
//        sessions.remove(sessionId);
//        rc.terminateSession(sessionId);
//        rc.unregisterSession();
//        notifyAll();
//    }
//
//    public synchronized void updateSessionLastActiveAt(String sessionId) {
//        sessions.get(sessionId).updateLastActiveAt();
//    }
//
//
//    private static final Logger LOGGER = Logger.getLogger(HudsonRemoteControlPool.class.getName());
//}
