package hudson.maven;

/*
 * Olivier Lamy
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.tools.ant.AntClassLoader;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.IOUtil;


/**
 * @author Olivier Lamy
 *
 */
public class MavenEmbedderUtils
{
    
    private static final String POM_PROPERTIES_PATH = "META-INF/maven/org.apache.maven/maven-core/pom.properties";
    
    private MavenEmbedderUtils() {
        // no op only to prevent construction
    }
    
    /**
     * <p>
     * build a {@link ClassRealm} with all jars in mavenHome/lib/*.jar
     * </p>
     * <p>
     * the {@link ClassRealm} is ChildFirst with the current classLoader as parent.
     * </p> 
     * @param mavenHome cannot be <code>null</code>
     * @param world can be <code>null</code>
     * @return
     */
    public static ClassRealm buildClassRealm(File mavenHome, ClassWorld world, ClassLoader parentClassLoader )
        throws MavenEmbedderException {
        
        if ( mavenHome == null ) {
            throw new IllegalArgumentException( "mavenHome cannot be null" );
        }
        if ( !mavenHome.exists() ) {
            throw new IllegalArgumentException( "mavenHome '" + mavenHome.getPath() + "' doesn't seem to exist on this node (or you don't have sufficient rights to access it)" );
        }

        // list all jar under mavenHome/lib

        File libDirectory = new File( mavenHome, "lib" );
        if ( !libDirectory.exists() ) {
            throw new IllegalArgumentException( mavenHome.getPath() + " doesn't have a 'lib' subdirectory - thus cannot be a valid maven installation!" );
        }

        File[] jarFiles = libDirectory.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name ) {
                return name.endsWith( ".jar" );
            }
        } );
        
        
        AntClassLoader antClassLoader = new AntClassLoader( Thread.currentThread().getContextClassLoader(), false );
        

        for ( File jarFile : jarFiles ) {
            antClassLoader.addPathComponent( jarFile );
        }
        
        if (world == null) {
            world = new ClassWorld();
        }
        
        ClassRealm classRealm = new ClassRealm( world, "plexus.core", parentClassLoader == null ? antClassLoader : parentClassLoader );

        for ( File jarFile : jarFiles ) {
            try {
                classRealm.addURL( jarFile.toURI().toURL() );
            } catch ( MalformedURLException e ) {
                throw new MavenEmbedderException( e.getMessage(), e );
            }
        }
        return classRealm;
    }
    
    public static PlexusContainer buildPlexusContainer(File mavenHome, MavenRequest mavenRequest) throws MavenEmbedderException {
        ClassWorld world = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());

        ClassRealm classRealm = MavenEmbedderUtils.buildClassRealm( mavenHome, world, Thread.currentThread().getContextClassLoader() );

        DefaultContainerConfiguration conf = new DefaultContainerConfiguration();

        conf.setContainerConfigurationURL( mavenRequest.getOverridingComponentsXml() )
            .setRealm( classRealm )
            .setClassWorld( world )
            .setClassPathScanning( mavenRequest.getContainerClassPathScanning() )
            .setComponentVisibility( mavenRequest.getContainerComponentVisibility() );
        
        return buildPlexusContainer(mavenRequest,conf);
    }

    /**
     * used by PomParser in Jenkins
     * @param mavenClassLoader
     * @param parent
     * @param mavenRequest
     * @return
     * @throws MavenEmbedderException
     */
    public static PlexusContainer buildPlexusContainer(ClassLoader mavenClassLoader, ClassLoader parent, MavenRequest mavenRequest) throws MavenEmbedderException {
        DefaultContainerConfiguration conf = new DefaultContainerConfiguration();
        
        conf.setAutoWiring( mavenRequest.isContainerAutoWiring() )
            .setClassPathScanning( mavenRequest.getContainerClassPathScanning() )
            .setComponentVisibility( mavenRequest.getContainerComponentVisibility() )
            .setContainerConfigurationURL( mavenRequest.getOverridingComponentsXml() );

        ClassWorld classWorld = new ClassWorld();

        ClassRealm classRealm = new ClassRealm( classWorld, "maven", mavenClassLoader );
        classRealm.setParentRealm( new ClassRealm( classWorld, "maven-parent",
                                                   parent == null ? Thread.currentThread().getContextClassLoader()
                                                                   : parent ) );
        conf.setRealm( classRealm );

        conf.setClassWorld( classWorld );
        
        return buildPlexusContainer(mavenRequest,conf);
    }

    private static PlexusContainer buildPlexusContainer(MavenRequest mavenRequest,ContainerConfiguration containerConfiguration )
        throws MavenEmbedderException {
        try
        {
            DefaultPlexusContainer plexusContainer = new DefaultPlexusContainer( containerConfiguration );
            if (mavenRequest.getMavenLoggerManager() != null) {
                plexusContainer.setLoggerManager( mavenRequest.getMavenLoggerManager() );
            }
            if (mavenRequest.getLoggingLevel() > 0) {
                plexusContainer.getLoggerManager().setThreshold( mavenRequest.getLoggingLevel() );
            }
            return plexusContainer;
        } catch ( PlexusContainerException e ) {
            throw new MavenEmbedderException( e.getMessage(), e );
        }
    }
        
    
    
    /**
     * @param mavenHome
     * @return the maven version 
     * @throws MavenEmbedderException
     */
    public static MavenInformation getMavenVersion(File mavenHome) throws MavenEmbedderException {
        
        ClassRealm realm = buildClassRealm( mavenHome, null, null );
        if (debug) {
            debugMavenVersion(realm);
        }
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = null;
        JarFile jarFile = null;
        MavenInformation information = null;
        try {
            Thread.currentThread().setContextClassLoader( realm );
            // TODO is this really intending to use findResource rather than getResource? Cf. https://github.com/sonatype/plexus-classworlds/pull/8
            URL resource = realm.findResource( POM_PROPERTIES_PATH );
            if (resource == null) {
                throw new MavenEmbedderException("Couldn't find maven version information in '" + mavenHome.getPath()
                        + "'. Are you sure that this is a valid maven home?");
            }
            URLConnection uc = resource.openConnection();
            if (uc instanceof JarURLConnection) {
                final JarURLConnection connection = (JarURLConnection)uc;
                final String entryName = connection.getEntryName();
                try {
                    jarFile = connection.getJarFile();
                    final JarEntry entry = (entryName != null && jarFile != null) ? jarFile.getJarEntry(entryName) : null;
                    if (entry != null) {
                        inputStream = jarFile.getInputStream(entry);
                        Properties properties = new Properties();
                        properties.load( inputStream );
                        information = new MavenInformation( properties.getProperty( "version" ) , resource.toExternalForm() );
                    }
                } finally {
                    if (jarFile != null) {
                        jarFile.close();
                    }
                }
            }
        } catch ( IOException e ) {
            throw new MavenEmbedderException( e.getMessage(), e );
        } finally {
            IOUtil.close( inputStream );
            Thread.currentThread().setContextClassLoader( original );
        }

        return information;
    }
    
    public static boolean isAtLeastMavenVersion(File mavenHome, String version)  throws MavenEmbedderException {
        ComparableVersion found = new ComparableVersion( getMavenVersion( mavenHome ).getVersion() );
        ComparableVersion testedOne = new ComparableVersion( version );
        return found.compareTo( testedOne ) >= 0;
    }
    
    private static void debugMavenVersion(ClassRealm realm ) {
        try {
            // TODO as above, consider getResources
            @SuppressWarnings("unchecked")
            Enumeration<URL> urls = realm.findResources( POM_PROPERTIES_PATH );
            System.out.println("urls for " + POM_PROPERTIES_PATH );
            while(urls.hasMoreElements()) {
                System.out.println("url " + urls.nextElement().toExternalForm());
            }
        } catch (IOException e) {
            System.out.println("Ignore IOException during searching " + POM_PROPERTIES_PATH + ":" + e.getMessage());
        }
    }
    
    public static boolean debug = Boolean.getBoolean( "hudson.maven.MavenEmbedderUtils.debug" );

}
