package hudson.plugins.selenium.callables;

import hudson.remoting.Callable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiCallable<T extends Exception> implements Callable<Void, T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8163154842147093972L;
	
	private List<Callable<?, T>> callables;
	
	public MultiCallable(List<Callable<?, T>> mcalls) {
		callables = new ArrayList<Callable<?, T>>(mcalls);
	}

	public MultiCallable() {
		this(Collections.<Callable<?, T>>emptyList());
	}

	public void addCallable(Callable<?, T> callable) {
		callables.add(callable);
	}
	
	public Void call() throws T {
		for (Callable<?, T> callable : callables) {
			callable.call();
		}
		return null;
	}

}
