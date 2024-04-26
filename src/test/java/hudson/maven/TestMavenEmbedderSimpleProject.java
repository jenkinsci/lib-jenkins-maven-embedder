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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.junit.Test;

/**
 * @author olamy
 *
 */
public class TestMavenEmbedderSimpleProject {

    @Test
    public void testSimpleProjectRead() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(new File("src/test/projects-tests/one-module/pom.xml").getAbsolutePath());

        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));

        mavenRequest.setBaseDirectory(new File("src/test/projects-tests/scm-git-test-one-module").getAbsolutePath());
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);
        // new MavenEmbedder( new File( System.getProperty( "maven.home" ) ), mavenRequest );

        MavenProject project = mavenEmbedder.readProject(new File("src/test/projects-tests/one-module/pom.xml"));
        assertEquals("my-app", project.getArtifactId());
    }

    @Test
    public void testSimpleProjectBuild() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setUserSettingsFile(
                new File(System.getProperty("user.home"), ".m2/settings.xml").getAbsolutePath());
        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));
        mavenRequest.setPom(new File("src/test/projects-tests/one-module/pom.xml").getAbsolutePath());
        mavenRequest.setGoals(Arrays.asList("clean", "test"));
        mavenRequest.getUserProperties().put("failIfNoTests", "false");

        final List<String> executedMojos = new ArrayList<>();

        AbstractExecutionListener listener = new AbstractExecutionListener() {
            public void mojoStarted(ExecutionEvent event) {
                executedMojos.add(event.getMojoExecution().getArtifactId());
            }
        };

        mavenRequest.setExecutionListener(listener);

        // mavenRequest.setBaseDirectory( new File( "src/test/projects-tests/scm-git-test-one-module"
        // ).getAbsolutePath() );
        MavenEmbedder mavenEmbedder = new MavenEmbedder(new File(System.getProperty("maven.home")), mavenRequest);

        MavenExecutionResult result = mavenEmbedder.execute(mavenRequest);

        System.out.println(result.getExceptions().toString());

        assertTrue(result.getExceptions().isEmpty());

        assertTrue(executedMojos.contains("maven-clean-plugin"));
        assertTrue(executedMojos.contains("maven-surefire-plugin"));
    }

    @Test
    public void testEclipsePluginProjectRead() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(new File("src/test/projects-tests/eclipse-plugin/pom.xml").getAbsolutePath());

        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));

        mavenRequest.setBaseDirectory(new File("src/test/projects-tests/scm-git-test-one-module").getAbsolutePath());
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);
        // new MavenEmbedder( new File( System.getProperty( "maven.home" ) ), mavenRequest );

        MavenProject project = mavenEmbedder.readProject(new File("src/test/projects-tests/eclipse-plugin/pom.xml"));
        assertEquals("my-app", project.getArtifactId());
        assertEquals("eclipse-plugin", project.getPackaging());
    }

    @Test
    public void testEclipsePluginProjectReadMultiModule() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(
                new File("src/test/projects-tests/eclipse-plugin-with-parent/parent/pom.xml").getAbsolutePath());

        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));

        mavenRequest.setBaseDirectory(
                new File("src/test/projects-tests/eclipse-plugin-with-parent/parent/").getAbsolutePath());
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);
        // new MavenEmbedder( new File( System.getProperty( "maven.home" ) ), mavenRequest );

        List<MavenProject> projects = mavenEmbedder.readProjects(
                new File("src/test/projects-tests/eclipse-plugin-with-parent/parent/pom.xml"), true);
        assertEquals("not 2 projects", 2, projects.size());
    }

    @Test
    public void testWrongScopeWithMaven2() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(new File("src/test/projects-tests/test-pom-8395.xml").getAbsolutePath());

        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));

        mavenRequest.setBaseDirectory(new File("src/test/projects-tests/").getAbsolutePath());
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);
        // new MavenEmbedder( new File( System.getProperty( "maven.home" ) ), mavenRequest );

        mavenEmbedder.readProjects(new File("src/test/projects-tests/test-pom-8395.xml"), true);
    }

    @Test(expected = ProjectBuildingException.class)
    public void testWrongScopeWithMaven3() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0);
        mavenRequest.setPom(new File("src/test/projects-tests/test-pom-8395.xml").getAbsolutePath());

        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));

        mavenRequest.setBaseDirectory(new File("src/test/projects-tests/").getAbsolutePath());
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);
        // new MavenEmbedder( new File( System.getProperty( "maven.home" ) ), mavenRequest );
        mavenEmbedder.readProjects(new File("src/test/projects-tests/test-pom-8395.xml"), true);
    }
}
