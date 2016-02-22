package org.jenkinsci.plugins.DependencyCheck;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.DependencyCheck.parser.ReportParser;
import org.jenkinsci.plugins.DependencyCheck.parser.Warning;
import org.jenkinsci.plugins.DependencyCheck.threadfix.ThreadFixClient;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.analysis.util.model.FileAnnotation;

/**
 * Publishes results from the dependency checker to ThreadFix.
 * Based on {@link DependencyCheckPublisher}
 * 
 * @author Emilio Escobar <eescobar@gmail.com>
 *
 */
public class DependencyCheckThreadFixPublisher extends HealthAwarePublisher {
	private static final long serialVersionUID = 5515183512239123807L;
	
	// Default Dependency-Check report filename pattern.
    private static final String DEFAULT_PATTERN = "**/dependency-check-report.xml";
	
	private String pattern;
	private String threadFixAppId;
	
    /**
     * Creates a new instance of <code>DependencyCheckPublisher</code>.
     *
     * @param healthy                   Report health as 100% when the number of warnings is less than
     *                                  this value
     * @param unHealthy                 Report health as 0% when the number of warnings is greater
     *                                  than this value
     * @param thresholdLimit            determines which warning priorities should be considered when
     *                                  evaluating the build stability and health
     * @param defaultEncoding           the default encoding to be used when reading and parsing files
     * @param useDeltaValues            determines whether the absolute annotations delta or the
     *                                  actual annotations set difference should be used to evaluate
     *                                  the build stability
     * @param unstableTotalAll          annotation threshold
     * @param unstableTotalHigh         annotation threshold
     * @param unstableTotalNormal       annotation threshold
     * @param unstableTotalLow          annotation threshold
     * @param unstableNewAll            annotation threshold
     * @param unstableNewHigh           annotation threshold
     * @param unstableNewNormal         annotation threshold
     * @param unstableNewLow            annotation threshold
     * @param failedTotalAll            annotation threshold
     * @param failedTotalHigh           annotation threshold
     * @param failedTotalNormal         annotation threshold
     * @param failedTotalLow            annotation threshold
     * @param failedNewAll              annotation threshold
     * @param failedNewHigh             annotation threshold
     * @param failedNewNormal           annotation threshold
     * @param failedNewLow              annotation threshold
     * @param canRunOnFailed            determines whether the plug-in can run for failed builds, too
     * @param useStableBuildAsReference determines whether only stable builds should be used as reference builds or not
     * @param shouldDetectModules       determines whether module names should be derived from Maven POM or Ant build files
     * @param canComputeNew             determines whether new warnings should be computed (with
     *                                  respect to baseline)
     * @param pattern                   Ant file-set pattern to scan for Dependency-Check report files
     * @param threadFixAppId          Name of application inside of ThreadFix
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
	public DependencyCheckThreadFixPublisher(String healthy, String unHealthy,
			String thresholdLimit, String defaultEncoding,
			boolean useDeltaValues, String unstableTotalAll,
			String unstableTotalHigh, String unstableTotalNormal,
			String unstableTotalLow, String unstableNewAll,
			String unstableNewHigh, String unstableNewNormal,
			String unstableNewLow, String failedTotalAll,
			String failedTotalHigh, String failedTotalNormal,
			String failedTotalLow, String failedNewAll, String failedNewHigh,
			String failedNewNormal, String failedNewLow,
			boolean canRunOnFailed, boolean useStableBuildAsReference,
			boolean shouldDetectModules, boolean canComputeNew,
			final String pattern, final String threadFixAppId) {
		super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
				unstableTotalAll, unstableTotalHigh, unstableTotalNormal,
				unstableTotalLow, unstableNewAll, unstableNewHigh, unstableNewNormal,
				unstableNewLow, failedTotalAll, failedTotalHigh, failedTotalNormal,
				failedTotalLow, failedNewAll, failedNewHigh, failedNewNormal,
				failedNewLow, canRunOnFailed, useStableBuildAsReference,
				shouldDetectModules, canComputeNew, false, DependencyCheckPlugin.PLUGIN_NAME + " ThreadFix");
		this.pattern = pattern;
		this.threadFixAppId = threadFixAppId;
		// TODO Auto-generated constructor stub
	}
    
    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }
    
    /**
     * Returns the threadFixAppId of the application.
     * 
     * @return threadFixAppId
     */
    public String getThreadFixAppId() {
    	return threadFixAppId;
    }
    
    @Override
    public DependencyCheckThreadFixDescriptor getDescriptor() {
    	return (DependencyCheckThreadFixDescriptor)super.getDescriptor();
    }

	public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher,
			BuildListener listener) {
		return new DependencyCheckAnnotationsAggregator(build, launcher, listener, this, getDefaultEncoding(), useOnlyStableBuildsAsReference());
	}

	@Override
	protected BuildResult perform(AbstractBuild<?, ?> build, PluginLogger logger)
			throws InterruptedException, IOException {
        logger.log("Collecting Dependency-Check analysis files...");
        FilesParser dcCollector = new FilesParser(DependencyCheckPlugin.PLUGIN_NAME, StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                new ReportParser(getDefaultEncoding()), shouldDetectModules(), isMavenBuild(build));
        ParserResult project = build.getWorkspace().act(dcCollector);
        DependencyCheckResult result = new DependencyCheckResult(build, getDefaultEncoding(), project, useOnlyStableBuildsAsReference());
        build.getActions().add(new DependencyCheckResultAction(build, this, result));
        ThreadFixClient threadFixClient = new ThreadFixClient(getDescriptor());
        
        for(FileAnnotation annotation : result.getAnnotations()) {
        	if (annotation instanceof Warning) {
        		Warning w = (Warning) annotation;
        		threadFixClient.submitWarning(threadFixAppId, w);
        	}
        }
	
        return result;
	}

}
