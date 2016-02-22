package org.jenkinsci.plugins.DependencyCheck;

import java.net.URL;

import org.jenkinsci.plugins.DependencyCheck.threadfix.ThreadFixClient;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.util.FormValidation;

/**
 * Descriptor for the class {@link DependencyCheckThreadFixPublisher}. Used as a singleton.
 * Class can be accessed from views. Based on {@link DependencyCheckDescriptor}. Difference
 * is configuration for threadfix.
 * 
 * @author Emilio Escobar <eescobar@gmail.com>
 *
 */
@Extension(ordinal = 100)
public final class DependencyCheckThreadFixDescriptor extends PluginDescriptor {
    //note: if the artifactId in pom.xml changes, the ICONS_PREFIX and PLUGIN_ID also need to change

    private static final String ICONS_PREFIX = "/plugin/dependency-check-jenkins-plugin/icons/";

    // The ID of this plug-in is used as URL.
    static final String PLUGIN_ID = "dependency-check-jenkins-plugin";

    // The URL of the result action.
    static final String RESULT_URL = PluginDescriptor.createResultUrlName(PLUGIN_ID);

    // Icon to use for the result and project action.
    static final String ICON_URL = ICONS_PREFIX + "dependency-check-24x24.png";
    
    private String threadFixUrl;
    private String threadFixAPIKey;
    
    public DependencyCheckThreadFixDescriptor() {
    	super(DependencyCheckThreadFixPublisher.class);
    }
    
    @Override
    public String getDisplayName() {
    	return Messages.ThreadFixPublisher_Name();
    }
    
    @Override
    public String getPluginName() {
        return PLUGIN_ID;
    }

    @Override
    public String getIconUrl() {
        return ICON_URL;
    }

    @Override
    public String getSummaryIconUrl() {
        return ICONS_PREFIX + "dependency-check-48x48.png";
    }
    
    public String getThreadFixUrl() {
		return threadFixUrl;
	}

	public void setThreadFixUrl(String threadFixUrl) {
		this.threadFixUrl = threadFixUrl;
	}

	public String getThreadFixAPIKey() {
		return threadFixAPIKey;
	}

	public void setThreadFixAPIKey(String threadFixAPIKey) {
		this.threadFixAPIKey = threadFixAPIKey;
	}

	/**
     * By default, isApplicable will return true for all projects except Maven projects. We
     * want the publisher to be available on Maven projects as well.
     * @param jobType the type of Jenkins job (freestyle, maven, etc)
     * @return true
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return true;
    }
    
    //FormValidations
    public FormValidation doCheckThreadFixUrl(@QueryParameter("threadFixUrl")String threadFixUrl) {
    	try {
    		new URL(threadFixUrl);
    		this.threadFixUrl = threadFixUrl;
    		return FormValidation.ok();
    	} catch (Exception ex) {
    		return FormValidation.error("Invalid ThreadFix URL: " + ex.getMessage());
    	}
    }
    
    public FormValidation doCheckThreadFixAPIKey(@QueryParameter("threadFixAPIKey") String threadFixAPIKey) {
    	this.threadFixAPIKey = threadFixAPIKey;
    	return FormValidation.ok();
    }
    
    public FormValidation doTestThreadFixConnection(@QueryParameter("threadFixUrl") String threadFixUrl,
    												@QueryParameter("threadFixAPIKey") String threadFixAPIKey){
    	this.threadFixUrl = threadFixUrl;
    	this.threadFixAPIKey = threadFixAPIKey;
    	
    	ThreadFixClient threadFixClient = new ThreadFixClient(this);
    	
    	boolean success = false;
    	
    	try {
    		success = threadFixClient.checkConnection();
    	} catch (Exception ex) {
    		return FormValidation.warning("Unable to connect to ThreadFix: " + ex);
    	}
    	
    	if (success) {
    		return FormValidation.ok("Connection successful");
    	} else {
    		return FormValidation.warning("Failed to connect to ThreadFix");
    	}
    	
    }
}
