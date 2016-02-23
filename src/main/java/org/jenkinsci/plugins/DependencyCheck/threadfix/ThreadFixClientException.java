package org.jenkinsci.plugins.DependencyCheck.threadfix;

public class ThreadFixClientException extends Throwable {
	private static final long serialVersionUID = 2619865893784408650L;
	
	public ThreadFixClientException(String msg) {
		super(msg);
	}

}
