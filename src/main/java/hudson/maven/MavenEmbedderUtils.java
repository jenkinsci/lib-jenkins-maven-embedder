package hudson.maven;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.tools.ant.AntClassLoader;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

/*
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

/**
 * @author Olivier Lamy
 *
 */
public class MavenEmbedderUtils
{
    
    private MavenEmbedderUtils()
    {
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
        throws MavenEmbedderException
    {
        
        if ( mavenHome == null )
        {
            throw new IllegalArgumentException( "mavenHome cannot be null" );
        }
        if ( !mavenHome.exists() )
        {
            throw new IllegalArgumentException( "mavenHome must exists" );
        }

        // list all jar under mavenHome/lib

        File libDirectory = new File( mavenHome, "lib" );
        if ( !libDirectory.exists() )
        {
            throw new IllegalArgumentException( mavenHome.getPath() + " without lib directory" );
        }

        File[] jarFiles = libDirectory.listFiles( new FilenameFilter()
        {

            public boolean accept( File dir, String name )
            {
                return name.endsWith( ".jar" );
            }
        } );
        
        
        AntClassLoader antClassLoader = new AntClassLoader( Thread.currentThread().getContextClassLoader(), false );
        

        for ( File jarFile : jarFiles )
        {
            antClassLoader.addPathComponent( jarFile );
        }
        
        if (world == null)
        {
            world = new ClassWorld();
        }
        
        ClassRealm classRealm = new ClassRealm( world, "maven", parentClassLoader == null ? antClassLoader : parentClassLoader );

        for ( File jarFile : jarFiles )
        {
            try
            {
                classRealm.addURL( jarFile.toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                throw new MavenEmbedderException( e.getMessage(), e );
            }
        }
        return classRealm;
    }
    
    /**
     * @param mavenHome
     * @return the maven version 
     * @throws MavenEmbedderException
     */
    public static String getMavenVersion(File mavenHome) throws MavenEmbedderException
    {
        
        ClassRealm realm = buildClassRealm( mavenHome, null, null );
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( realm );
            InputStream inStream = realm.getResourceAsStream( "META-INF/maven/org.apache.maven/maven-core/pom.properties" );
            Properties properties = new Properties();
            properties.load( inStream );
            return properties.getProperty( "version" );
        }
        catch ( IOException e )
        {
            throw new MavenEmbedderException( e.getMessage(), e );
        } finally
        {
            Thread.currentThread().setContextClassLoader( original );
        }
        
    }
    
    public static boolean isAtLeastMavenVersion(File mavenHome, String version)  throws MavenEmbedderException
    {
        ComparableVersion found = new ComparableVersion( getMavenVersion( mavenHome ) );
        ComparableVersion testedOne = new ComparableVersion( version );
        return found.compareTo( testedOne ) >= 0;
    }

}
