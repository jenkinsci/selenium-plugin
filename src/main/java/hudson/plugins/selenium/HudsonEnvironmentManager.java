package hudson.plugins.selenium;

/**
 * @author Kohsuke Kawaguchi
 */
class HudsonEnvironmentManager {}
//public class HudsonEnvironmentManager extends EnvironmentManager {
//    /**
//     * We accept "label:browser"
//     */
//    @Override
//    public Environment environment(String name) {
//        String[] tokens = name.split(":");
//        if(tokens.length==2)
//            return new Environment(tokens[0],tokens[1]);
//        // take a chance and let it run on any RC.
//        // this is useful for maintaining compatibility with standalone RCs
//        return new Environment("&",name);
//    }
//
//    /**
//     * Listing available names not very useful.
//     */
//    @Override
//    public List<Environment> environments() {
//        return Collections.emptyList();
//    }
//}
