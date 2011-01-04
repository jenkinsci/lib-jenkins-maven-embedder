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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

/**
 * NOTE : <b>this class is not designed for external use so it can change without any prior notice</b>
 * class coming from ASF sources 
 * http://svn.apache.org/repos/asf/maven/maven-3/trunk/maven-core/src/main/java/org/apache/maven/ReactorReader.java
 * FIXME simplify more !!
 * @author Olivier Lamy
 * @since 1.1
 */
public class ReactorReader
    implements WorkspaceReader
{
    private Map<String, MavenProject> projectsByGAV;

    private Map<String, List<MavenProject>> projectsByGA;

    private WorkspaceRepository repository;

    private File workspaceRoot;

    public ReactorReader( Map<String, MavenProject> reactorProjects, File workspaceRoot )
    {
        projectsByGAV = reactorProjects;
        this.workspaceRoot = workspaceRoot;
        projectsByGA = new HashMap<String, List<MavenProject>>( reactorProjects.size() * 2 );
        for ( MavenProject project : reactorProjects.values() )
        {
            String key = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );

            List<MavenProject> projects = projectsByGA.get( key );

            if ( projects == null )
            {
                projects = new ArrayList<MavenProject>( 1 );
                projectsByGA.put( key, projects );
            }

            projects.add( project );
        }

        repository = new WorkspaceRepository( "reactor", new HashSet<String>( projectsByGAV.keySet() ) );
    }

    private File find( MavenProject project, Artifact artifact )
    {
        if ( "pom".equals( artifact.getExtension() ) )
        {
            return project.getFile();
        }

        org.apache.maven.artifact.Artifact matchingArtifact = findMatchingArtifact( project, artifact );
        if ( matchingArtifact != null )
        {
            return matchingArtifact.getFile();
        }
        return null;
    }

    /**
     * Tries to resolve the specified artifact from the artifacts of the given project.
     * 
     * @param project The project to try to resolve the artifact from, must not be <code>null</code>.
     * @param requestedArtifact The artifact to resolve, must not be <code>null</code>.
     * @return The matching artifact from the project or <code>null</code> if not found.
     */
    private org.apache.maven.artifact.Artifact findMatchingArtifact( MavenProject project, Artifact requestedArtifact )
    {
        String requestedRepositoryConflictId = getConflictId( requestedArtifact );

        org.apache.maven.artifact.Artifact mainArtifact = project.getArtifact();
        if ( requestedRepositoryConflictId.equals( getConflictId( mainArtifact ) ) )
        {
            mainArtifact.setFile( new File( workspaceRoot, project.getArtifactId() ) );
            return mainArtifact;
        }

        Collection<org.apache.maven.artifact.Artifact> attachedArtifacts = project.getAttachedArtifacts();
        if ( attachedArtifacts != null && !attachedArtifacts.isEmpty() )
        {
            for ( org.apache.maven.artifact.Artifact attachedArtifact : attachedArtifacts )
            {
                if ( requestedRepositoryConflictId.equals( getConflictId( attachedArtifact ) ) )
                {
                    attachedArtifact.setFile( new File( workspaceRoot, project.getArtifactId() ) );
                    return attachedArtifact;
                }
            }
        }

        return null;
    }

    /**
     * Gets the repository conflict id of the specified artifact. Unlike the dependency conflict id, the repository
     * conflict id uses the artifact file extension instead of the artifact type. Hence, the repository conflict id more
     * closely reflects the identity of artifacts as perceived by a repository.
     * 
     * @param artifact The artifact, must not be <code>null</code>.
     * @return The repository conflict id, never <code>null</code>.
     */
    private String getConflictId( org.apache.maven.artifact.Artifact artifact )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( artifact.getGroupId() );
        buffer.append( ':' ).append( artifact.getArtifactId() );
        if ( artifact.getArtifactHandler() != null )
        {
            buffer.append( ':' ).append( artifact.getArtifactHandler().getExtension() );
        }
        else
        {
            buffer.append( ':' ).append( artifact.getType() );
        }
        if ( artifact.hasClassifier() )
        {
            buffer.append( ':' ).append( artifact.getClassifier() );
        }
        return buffer.toString();
    }

    private String getConflictId( Artifact artifact )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( artifact.getGroupId() );
        buffer.append( ':' ).append( artifact.getArtifactId() );
        buffer.append( ':' ).append( artifact.getExtension() );
        if ( artifact.getClassifier().length() > 0 )
        {
            buffer.append( ':' ).append( artifact.getClassifier() );
        }
        return buffer.toString();
    }

    public File findArtifact( Artifact artifact )
    {
        String projectKey = artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getVersion();

        MavenProject project = projectsByGAV.get( projectKey );

        if ( project != null )
        {
            return find( project, artifact );
        }

        return null;
    }

    public List<String> findVersions( Artifact artifact )
    {
        String key = artifact.getGroupId() + ':' + artifact.getArtifactId();

        List<MavenProject> projects = projectsByGA.get( key );
        if ( projects == null || projects.isEmpty() )
        {
            return Collections.emptyList();
        }

        List<String> versions = new ArrayList<String>();

        for ( MavenProject project : projects )
        {
            if ( find( project, artifact ) != null )
            {
                versions.add( project.getVersion() );
            }
        }

        return Collections.unmodifiableList( versions );
    }
    
    public void addProject(MavenProject mavenProject) {
        String key = mavenProject.getGroupId() + ':' + mavenProject.getArtifactId();
        this.projectsByGA.put( key, Arrays.asList( mavenProject ) );
        
        String projectKey = mavenProject.getGroupId() + ':' + mavenProject.getArtifactId() + ':' + mavenProject.getVersion();
        
        this.projectsByGAV.put( projectKey, mavenProject );
    }

    public WorkspaceRepository getRepository()
    {
        return repository;
    }
}
