package hudson.plugins.selenium.callables;

import hudson.remoting.ChannelProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PropertyUtils {

    private static Map<String, Object> properties = new ConcurrentHashMap<String, Object>();

    public static <T> T getProperty(ChannelProperty<T> property) {
        return property.type.cast(properties.get(property.displayName));
    }

    public static void setProperty(ChannelProperty<?> property, Object object) {
        properties.put(property.displayName, object);
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public static <T> T getMapProperty(ChannelProperty<?> property, String key) {
        ChannelProperty<Map> cp = new ChannelProperty<Map>(Map.class, property.displayName);
        Map map = getProperty(cp);
        if (map == null) {
            return null;
        }
        return (T) map.get(key);
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public static void setMapProperty(ChannelProperty<?> property, String key, Object object) {
        ChannelProperty<Map> cp = new ChannelProperty<Map>(Map.class, property.displayName);
        Map map = getProperty(cp);
        if (map == null) {
            map = new HashMap();
            setProperty(cp, map);
        }
        map.put(key, object);
        setProperty(cp, map);
    }

}
