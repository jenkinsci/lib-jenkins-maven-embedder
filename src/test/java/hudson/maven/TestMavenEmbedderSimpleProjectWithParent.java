package hudson.maven;

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

import java.io.File;

import junit.framework.TestCase;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author olamy
 *
 */
public class TestMavenEmbedderSimpleProjectWithParent extends TestCase {

    public void testSimpleProjectRead() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setLoggingLevel( 1 );

        mavenRequest.setPom( new File( "src/test/projects-tests/one-module-with-parent/pom.xml" ).getAbsolutePath() );

        String localRepoPath = System.getProperty( "localRepository" , "./target/repo-maven" );
        
        System.out.println(" use localRepo path " + localRepoPath );
        
        File dir = new File(localRepoPath + "/org/sonatype/oss/oss-parent/5/");
        
        if (dir.exists()) {
            FileUtils.deleteDirectory( dir );
        }
        
        mavenRequest.setLocalRepositoryPath( localRepoPath );
        
        
        
        mavenRequest.setBaseDirectory( new File( "src/test/projects-tests/scm-git-test-one-module" ).getAbsolutePath() );
        MavenEmbedder mavenEmbedder = new MavenEmbedder( Thread.currentThread().getContextClassLoader(), mavenRequest );
        
        MavenProject project = mavenEmbedder.readProject( new File( "src/test/projects-tests/one-module-with-parent/pom.xml" ) );
        System.out.println("artficatId " + project.getArtifactId());
    }
        
}
