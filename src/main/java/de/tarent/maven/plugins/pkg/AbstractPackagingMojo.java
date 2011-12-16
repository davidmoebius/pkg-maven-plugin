/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb, ipk, izpack)
 * Copyright (C) 2000-2008 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'Maven Packaging Plugin'
 * Signature of Elmar Geese, 11 March 2008
 * Elmar Geese, CEO tarent GmbH.
 */

/*
 * Maven Packaging Plugin,
 * Maven plugin to package a Project (deb and izpack)
 * Copyright (C) 2000-2007 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License,version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'Maven Packaging Plugin'
 * Signature of Elmar Geese, 14 June 2007
 * Elmar Geese, CEO tarent GmbH.
 */

/* $Id: AbstractPackagingMojo.java,v 1.16 2007/08/07 11:29:59 robert Exp $
 *
 * maven-pkg-plugin, Packaging plugin for Maven2 
 * Copyright (C) 2007 tarent GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * tarent GmbH., hereby disclaims all copyright
 * interest in the program 'maven-pkg-plugin'
 * written by Robert Schuster, Fabian Koester. 
 * signature of Elmar Geese, 1 June 2002
 * Elmar Geese, CEO tarent GmbH
 */

package de.tarent.maven.plugins.pkg;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

/**
 * Base Mojo for all packaging mojos. It provides convenient access to a mean to
 * resolve the project's complete dependencies.
 */
public abstract class AbstractPackagingMojo extends AbstractMojo {

	private static final String DEFAULT_SRC_AUXFILESDIR = "src/main/auxfiles";

	public static String getDefaultSrcAuxfilesdir() {
		return DEFAULT_SRC_AUXFILESDIR;
	}

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * Artifact factory, needed to download source jars.
	 * 
	 * @component role="org.apache.maven.project.MavenProjectBuilder"
	 * @required
	 * @readonly
	 */
	protected MavenProjectBuilder mavenProjectBuilder;

	/**
	 * Temporary directory that contains the files to be assembled.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected File buildDir;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
	 * @required
	 * @readonly
	 */
	protected ArtifactFactory factory;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
	 * @required
	 * @readonly
	 */
	protected ArtifactResolver resolver;

	/**
	 * Used to look up Artifacts in the remote repository.
	 * 
	 * @component 
	 *            role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
	 * @required
	 * @readonly
	 */
	protected ArtifactMetadataSource metadataSource;

	/**
	 * Location of the local repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @readonly
	 * @required
	 */
	protected ArtifactRepository local;

	/**
	 * List of Remote Repositories used by the resolver
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 * @required
	 */
	protected List<ArtifactRepository> remoteRepos;

	/**
	 * @parameter expression="${project.artifact}"
	 * @required
	 * @readonly
	 */
	protected Artifact artifact;

	/**
	 * @parameter expression="${project.artifactId}"
	 * @required
	 * @readonly
	 */
	protected String artifactId;

	/**
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 * @readonly
	 */
	protected String finalName;

	/**
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected File outputDirectory;

	/**
	 * @parameter expression="${project.version}"
	 * @required
	 * @readonly
	 */
	protected String version;

	/**
	 * JVM binary used to run Java programs from within the Mojo.
	 * 
	 * @parameter expression="${javaExec}" default-value="java"
	 * @required
	 * 
	 */
	protected String javaExec;

	/**
	 * 7Zip binary used to run Java programs from within the Mojo.
	 * 
	 * @parameter expression="${7zipExec}" default-value="7zr"
	 * @required
	 * 
	 */
	protected String _7zipExec;

	/**
	 * Location of the custom package map file. When specifying this one the
	 * internal package map will be overridden completely.
	 * 
	 * @parameter expression="${defPackageMapURL}"
	 */
	protected URL defaultPackageMapURL;

	/**
	 * Location of the auxiliary package map file. When this is specified the
	 * information in the document will be added to the default one.
	 * 
	 * @parameter expression="${auxPackageMapURL}"
	 */
	protected URL auxPackageMapURL;

	/**
	 * Overrides "defaultDistro" parameter. For use on the command-line.
	 * 
	 * @parameter expression="${distro}"
	 */
	protected String distro;

	/**
	 * Overrides "defaultIgnorePackagingTypes" defines a list of comma
	 * speparated packaging types that, when used, will skip copying the main
	 * artifact for the project (if any) in the final package. For use on the
	 * command-line.
	 * 
	 * @parameter expression="${ignorePackagingTypes}" default-value="pom"
	 * @required
	 */
	protected String ignorePackagingTypes;
	
	/**
	 * Parameter with a comma separated list of targets. For use on the command-line.
	 * 
	 * @parameter expression="${target}"
	 */
	protected String target;

	/**
	 * @parameter
	 */
	protected List<TargetConfiguration> targetConfigurations;
	
	public void setSignPassPhrase(String phrase){
		this.signPassPhrase = phrase;
	}
	public String getSignPassPhrase(){
		return signPassPhrase;
	}
	protected String signPassPhrase;
	
	public String get_7zipExec() {
		return _7zipExec;
	}

	public File getBuildDir() {
		return buildDir;
	}

	public ArtifactFactory getFactory() {
		return factory;
	}

	public String getFinalName() {
		return finalName;
	}

	public String getIgnorePackagingTypes() {
		return ignorePackagingTypes;
	}

	public String getJavaExec() {
		return javaExec;
	}

	public ArtifactRepository getLocalRepo() {
		return local;
	}

	public ArtifactMetadataSource getMetadataSource() {
		return metadataSource;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public MavenProject getProject() {
		return project;
	}

	public List<ArtifactRepository> getRemoteRepos() {
		return remoteRepos;
	}

	public ArtifactResolver getResolver() {
		return resolver;
	}

	public List<TargetConfiguration> getTargetConfigurations() {
		return targetConfigurations;
	}

	protected final boolean packagingTypeBelongsToIgnoreList() {
		boolean inList = false;
		Log l = getLog();
		l.info("ignorePackagingTypes set. Contains: " + ignorePackagingTypes + " . Project packaging is "
				+ project.getPackaging());
		for (String s : ignorePackagingTypes.split(",")) {
			if (project.getPackaging().compareToIgnoreCase(s) == 0) {
				inList = true;
			}
		}
		return inList;
	}
	  
	  /**
	   * 
	   * Returns the comma separated target list as String[].</br></br> 
	   * 
	   * In order to allow multiple target to be called through the command line we allow the user to provide
	   * a comma separated list (Maven < 3.0.3 does not transform comma separated values 
	   * from the command line to String[]). 
	   * 
	   * @param target
	   * @return
	   */
	  protected String[] getTargets()throws MojoExecutionException{
	  
		String[] targetArray = null;
		
		if (target!=null){
			targetArray =  target.split(",");
		}else{
			throw new MojoExecutionException("No target(s) specified for execution.");
		}
		
		return targetArray;
		
	  }
	

	  /**
	   * Validates arguments and test tools.
	   * 
	   * @throws MojoExecutionException
	   */
	  protected void checkEnvironment(Log l, TargetConfiguration tc) throws MojoExecutionException
	  {
	    l.info("distribution             : " + tc.getChosenDistro());
	    l.info("default package map      : "
	           + (defaultPackageMapURL == null ? "built-in"
	                                          : defaultPackageMapURL.toString()));
	    l.info("auxiliary package map    : "
	           + (auxPackageMapURL == null ? "no" : auxPackageMapURL.toString()));
	    l.info("type of project          : "
	           + ((tc.getMainClass() != null) ? "application" : "library"));
	    l.info("section                  : " + tc.getSection());
	    l.info("bundle all dependencies  : " + ((tc.isBundleAll()) ? "yes" : "no"));
	    l.info("ahead of time compilation: " + ((tc.isAotCompile()) ? "yes" : "no"));
	    l.info("custom jar libraries     : "
	            + ((tc.getJarFiles().isEmpty()) ? "<none>"
	                                      : String.valueOf(tc.getJarFiles().size())));
	    l.info("JNI libraries            : "
	           + ((tc.getJniFiles().isEmpty()) ? "<none>"
	                                     : String.valueOf(tc.getJniFiles().size())));
	    l.info("auxiliary file source dir: "
	           + (tc.getSrcAuxFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                             : tc.getSrcAuxFilesDir()));
	    l.info("auxiliary files          : "
	           + ((tc.getAuxFiles().isEmpty()) ? "<none>"
	                                     : String.valueOf(tc.getAuxFiles().size())));
	    l.info("prefix                   : "
	           + (tc.getPrefix().length() == 1 ? "/ (default)" : tc.getPrefix()));
	    l.info("sysconf files source dir : "
	           + (tc.getSrcSysconfFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                                 : tc.getSrcSysconfFilesDir()));
	    l.info("sysconfdir               : "
	           + (tc.getSysconfdir().length() == 0 ? "(default)" : tc.getSysconfdir()));
	    l.info("dataroot files source dir: "
	           + (tc.getSrcDatarootFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                                  : tc.getSrcDatarootFilesDir()));
	    l.info("dataroot                 : "
	           + (tc.getDatarootdir().length() == 0 ? "(default)" : tc.getDatarootdir()));
	    l.info("data files source dir    : "
	           + (tc.getSrcDataFilesDir().length() == 0 ? (getDefaultSrcAuxfilesdir() + " (default)")
	                                              : tc.getSrcDataFilesDir()));
	    l.info("datadir                  : "
	           + (tc.getDatadir().length() == 0 ? "(default)" : tc.getDatadir()));
	    l.info("bindir                   : "
	           + (tc.getBindir().length() == 0 ? "(default)" : tc.getBindir()));

	    if (tc.getChosenDistro() == null){
	      throw new MojoExecutionException("No distribution configured!");
	    }

	    if (tc.isAotCompile())
	      {
	        l.info("aot compiler             : " + tc.getGcjExec());
	        l.info("aot classmap generator   : " + tc.getGcjDbToolExec());
	      }

	    if (tc.getMainClass() == null)
	      {
	        if (! "libs".equals(tc.getSection())){
	          throw new MojoExecutionException(
	                                           "section has to be 'libs' if no main class is given.");
	        }
	        if (tc.isBundleAll()){
	          throw new MojoExecutionException(
	                                           "Bundling dependencies to a library makes no sense.");
	        }
	      }
	    else
	      {
	        if ("libs".equals(tc.getSection())){
	          throw new MojoExecutionException(
	                                           "Set a proper section if main class parameter is set.");
	        }
	      }

	    if (tc.isAotCompile())
	      {
	        AotCompileUtils.setGcjExecutable(tc.getGcjExec());
	        AotCompileUtils.setGcjDbToolExecutable(tc.getGcjDbToolExec());

	        AotCompileUtils.checkToolAvailability();
	      }
	  }

	  public void execute() throws MojoExecutionException, MojoFailureException
	  {
		// For some tasks it is practical to have the TargetConfiguration instances as a
		// map. This transformation step also serves as check for double entries.
	    Map<String, TargetConfiguration> targetConfigurationMap = Utils.toMap(targetConfigurations);
		  
		// Container for collecting target configurations that have been built. This
		// is used to make sure that TCs are not build repeatedly when the given target
		// configuration have a dependency to a common target configuration.
		HashSet<String> finishedTargets = new HashSet<String>();
		
		for(String t : getTargets()){
			// A single target (and all its dependent target configurations are supposed to use the same
			// distro value).
		    String d = (distro != null) ? distro : Utils.getDefaultDistro(t, targetConfigurations, getLog());
		    
		    // Retrieve all target configurations that need to be build for /t/
			List<TargetConfiguration> buildChain = Utils.createBuildChain(t, d, targetConfigurations);

			for (TargetConfiguration tc : buildChain) {
				if (!finishedTargets.contains(tc.getTarget()) && tc.isReady())
				{
					WorkspaceSession ws = new WorkspaceSession();
					ws.setMojo(this); // its us
					ws.setTargetConfigurationMap(targetConfigurationMap);
					ws.setTargetConfiguration(tc);
					
					executeTargetConfiguration(ws, d);

					// Mark as done.
				    finishedTargets.add(tc.getTarget());
				}

			}
		}
	  }
	  
	  protected abstract void executeTargetConfiguration(WorkspaceSession workspaceSession, String distro)
			  throws MojoExecutionException, MojoFailureException;
}
