/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.DependencyCheck;

import java.net.URL;

import org.jenkinsci.plugins.DependencyCheck.threadfix.ThreadFixClient;
import org.jenkinsci.plugins.DependencyCheck.threadfix.ThreadFixClientException;
import org.kohsuke.stapler.QueryParameter;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.util.FormValidation;

/**
 * Descriptor for the class {@link DependencyCheckPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Steve Springett (steve.springett@owasp.org), based on PmdDescriptor by Ulli Hafner
 */
@Extension(ordinal = 100)
public final class DependencyCheckDescriptor extends PluginDescriptor {

    //note: if the artifactId in pom.xml changes, the ICONS_PREFIX and PLUGIN_ID also need to change

    private static final String ICONS_PREFIX = "/plugin/dependency-check-jenkins-plugin/icons/";

    // The ID of this plug-in is used as URL.
    static final String PLUGIN_ID = "dependency-check-jenkins-plugin";

    // The URL of the result action.
    static final String RESULT_URL = PluginDescriptor.createResultUrlName(PLUGIN_ID);

    // Icon to use for the result and project action.
    static final String ICON_URL = ICONS_PREFIX + "dependency-check-24x24.png";
    
    //URL for ThreadFix
    private String threadFixUrl;
    
    //API Key to use when calling ThreadFix
    private String threadFixAPIKey;
    
    private boolean usingThreadFix;

    /**
     * Creates a new instance of {@link DependencyCheckDescriptor}.
     */
    public DependencyCheckDescriptor() {
        super(DependencyCheckPublisher.class);
        load();
    }

    @Override
    public String getDisplayName() {
        return Messages.Publisher_Name();
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

    public boolean isUsingThreadFix() {
		return usingThreadFix;
	}

	public void setUsingThreadFix(boolean usingThreadFix) {
		this.usingThreadFix = usingThreadFix;
	}

	/**
     * By default, isApplicable will return true for all projects except Maven projects. We
     * want the publisher to be available on Maven projects as well.
     * @param jobType the type of Jenkins job (freestyle, maven, etc)
     * @return true
     */
    @SuppressWarnings("rawtypes")
	@Override
    public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
        return true;
    }
    
    //FormValidations
    public FormValidation doCheckThreadFixUrl(@QueryParameter("threadFixUrl")String threadFixUrl) {
    	try {
    		new URL(threadFixUrl);
    		this.threadFixUrl = threadFixUrl;
    		this.usingThreadFix = true;
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
    												@QueryParameter("threadFixAPIKey") String threadFixAPIKey) {
    	this.threadFixUrl = threadFixUrl;
    	this.threadFixAPIKey = threadFixAPIKey;
    	
    	ThreadFixClient threadFixClient = new ThreadFixClient(this);
    	
    	
    	try {
    		threadFixClient.checkConnection();
    		this.usingThreadFix = true;
    	} catch (Exception ex) {
    		return FormValidation.warning("Error talking to ThreadFix: " + ex);
    	} catch (ThreadFixClientException e) {
			return FormValidation.warning("Error while talking to ThreadFix: " + e);
		}
    	
    	return FormValidation.ok("Connection successful");
    	
    }
}
