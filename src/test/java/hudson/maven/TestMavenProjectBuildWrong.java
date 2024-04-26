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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.junit.Test;

/**
 * @author olamy
 *
 */
public class TestMavenProjectBuildWrong {

    @Test
    public void testWrongInheritenceWithMaven2() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(
                new File("src/test/projects-tests/incorrect-inheritence-testcase/pom.xml").getAbsolutePath());

        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));

        ReactorReader reactorReader =
                new ReactorReader(new HashMap<>(), new File(mavenRequest.getPom()).getParentFile());

        mavenRequest.setWorkspaceReader(reactorReader);

        mavenRequest.setBaseDirectory(new File("src/test/projects-tests/").getAbsolutePath());
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);

        MavenProject root =
                mavenEmbedder.readProject(new File("src/test/projects-tests/incorrect-inheritence-testcase/pom.xml"));

        reactorReader.addProject(root);

        assertNotNull(root);
        System.out.println("modules " + root.getModules());
        for (String module : root.getModules()) {
            MavenProject mavenProject = mavenEmbedder.readProject(new File(root.getBasedir(), module + "/pom.xml"));
            reactorReader.addProject(mavenProject);
            assertNotNull(mavenProject);
        }
    }

    @Test(expected = ProjectBuildingException.class)
    public void testWrongInheritenceWithMaven3() throws Exception {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0);
        mavenRequest.setPom(
                new File("src/test/projects-tests/incorrect-inheritence-testcase/pom.xml").getAbsolutePath());

        mavenRequest.setLocalRepositoryPath(System.getProperty("localRepository", "./target/repo-maven"));

        mavenRequest.setBaseDirectory(new File("src/test/projects-tests/").getAbsolutePath());
        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);
        // new MavenEmbedder( new File( System.getProperty( "maven.home" ) ), mavenRequest );
        mavenEmbedder.readProjects(new File("src/test/projects-tests/incorrect-inheritence-testcase/pom.xml"), true);
    }
}
