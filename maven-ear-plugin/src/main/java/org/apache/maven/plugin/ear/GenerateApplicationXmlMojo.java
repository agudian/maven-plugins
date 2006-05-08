package org.apache.maven.plugin.ear;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Mojo that generates the application.xml deployment descriptor file.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 * @version $Id$
 * @goal generate-application-xml
 * @phase generate-resources
 * @requiresDependencyResolution test
 */
public class GenerateApplicationXmlMojo
    extends AbstractEarMojo
{

    public static final String VERSION_1_3 = "1.3";

    public static final String VERSION_1_4 = "1.4";

    public static final String VERSION_5 = "5";

    public static final String UTF_8 = "UTF-8";


    /**
     * Whether the application.xml should be generated or not.
     *
     * @parameter
     */
    private Boolean generateApplicationXml = Boolean.TRUE;

    /**
     * The version of the application.xml to generate. Valid values
     * are 1.3, 1.4 and 5.
     *
     * @parameter
     */
    private String version = VERSION_1_3;

    /**
     * Display name of the application to be used when application.xml
     * file is autogenerated.
     *
     * @parameter expression="${project.artifactId}"
     */
    private String displayName = null;

    /**
     * Description of the application to be used when application.xml
     * file is autogenerated.
     *
     * @parameter expression="${project.description}"
     */
    private String description = null;

    /**
     * Character encoding for the auto-generated application.xml file.
     *
     * @parameter
     */
    private String encoding = UTF_8;

    /**
     * The security-roles to be added to the auto-generated
     * application.xml file.
     *
     * @parameter
     */
    private PlexusConfiguration security;

    /**
     * Directory where the application.xml file will be auto-generated.
     *
     * @parameter expression="${project.build.directory}"
     */
    private String generatedDescriptorLocation;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // Initializes ear modules
        super.execute();

        if ( !generateApplicationXml.booleanValue() )
        {
            getLog().debug( "Generation of application.xml is disabled" );
            return;
        }

        // Check version
        if ( !version.equals( VERSION_1_3 ) && !version.equals( VERSION_1_4 ) && !version.equals( VERSION_5 ) )
        {
            throw new MojoExecutionException( "Invalid version[" + version + "]" );
        }

        // Generate deployment descriptor and copy it to the build directory
        getLog().info( "Generating application.xml" );
        try
        {
            generateDeploymentDescriptor();
        }
        catch ( EarPluginException e )
        {
            throw new MojoExecutionException( "Failed to generate application.xml", e );
        }

        try
        {
            FileUtils.copyFileToDirectory( new File( generatedDescriptorLocation, "application.xml" ),
                                           new File( getWorkDirectory(), "META-INF" ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy application.xml to final destination", e );
        }
    }

    /**
     * Generates the deployment descriptor if necessary.
     */
    protected void generateDeploymentDescriptor()
        throws EarPluginException
    {
        File outputDir = new File( generatedDescriptorLocation );
        if ( !outputDir.exists() )
        {
            outputDir.mkdirs();
        }

        File descriptor = new File( outputDir, "application.xml" );

        ApplicationXmlWriter writer = new ApplicationXmlWriter( version, encoding );
        writer.write( descriptor, getModules(), buildSecurityRoles(), displayName, description );
    }

    /**
     * Builds the security roles based on the configuration.
     *
     * @return a list of SecurityRole object(s)
     * @throws EarPluginException if the configuration is invalid
     */
    private List buildSecurityRoles()
        throws EarPluginException
    {
        final List result = new ArrayList();
        if ( security == null )
        {
            return result;
        }
        try
        {
            final PlexusConfiguration[] securityRoles = security.getChildren( SecurityRole.SECURITY_ROLE );

            for ( int i = 0; i < securityRoles.length; i++ )
            {
                PlexusConfiguration securityRole = securityRoles[i];
                final String id = securityRole.getAttribute( SecurityRole.ID_ATTRIBUTE );
                final String roleName = securityRole.getChild( SecurityRole.ROLE_NAME ).getValue();
                final String roleNameId =
                    securityRole.getChild( SecurityRole.ROLE_NAME ).getAttribute( SecurityRole.ID_ATTRIBUTE );
                final String description = securityRole.getChild( SecurityRole.DESCRIPTION ).getValue();
                final String descriptionId =
                    securityRole.getChild( SecurityRole.DESCRIPTION ).getAttribute( SecurityRole.ID_ATTRIBUTE );

                if ( roleName == null )
                {
                    throw new EarPluginException( "Invalid security-role configuration, role-name could not be null." );
                }
                else
                {
                    result.add( new SecurityRole( roleName, roleNameId, id, description, descriptionId ) );
                }
            }
            return result;
        }
        catch ( PlexusConfigurationException e )
        {
            throw new EarPluginException( "Invalid security-role configuration", e );
        }

    }
}
