package hudson.maven;

import java.io.File;

import org.apache.maven.artifact.versioning.ComparableVersion;

import junit.framework.TestCase;
import org.jvnet.hudson.test.Issue;

/**
 * @author Olivier Lamy
 *
 */
public class TestMavenEmbedderUtils extends TestCase {
    
    public void testMavenVersion() throws Exception {
        MavenInformation mavenInformation = MavenEmbedderUtils.getMavenVersion( new File( System.getProperty( "maven.home" ) ));
        
        String version = mavenInformation.getVersion();
        assertNotNull( mavenInformation.getVersionResourcePath() );
        System.out.println("maven version " + version );
        
        assertNotNull( version );
        ComparableVersion current = new ComparableVersion( version );
        
        ComparableVersion old = new ComparableVersion( "2.2.1" );
        
        assertTrue( current.compareTo( old ) > 0 );
        
        assertTrue( current.compareTo(  new ComparableVersion( "3.0" ) ) >= 0 );
    }
    
    public void testMavenVersion2_2_1() throws Exception {
        MavenInformation mavenInformation = MavenEmbedderUtils.getMavenVersion( new File( "src/test/maven-2.2.1" ) );
        assertNotNull( mavenInformation.getVersionResourcePath() );

        assertEquals("2.2.1", mavenInformation.getVersion());
    }
    
    public void testGetMavenVersionFromInvalidLocation() {
        try {
            MavenInformation mavenInformation =  MavenEmbedderUtils.getMavenVersion( new File(System.getProperty("java.home")));
            fail("We should have gotten a MavenEmbedderException but: " + mavenInformation);
        } catch (MavenEmbedderException e) {
            // expected
        }
    }

    public void testisAtLeastMavenVersion() throws Exception {
       assertTrue( MavenEmbedderUtils.isAtLeastMavenVersion( new File( System.getProperty( "maven.home" ) ), "3.0" ) );
       assertFalse( MavenEmbedderUtils.isAtLeastMavenVersion( new File( "src/test/maven-2.2.1" ), "3.0" ) );
    }
    
    @Issue("JENKINS-42549")
    public void testIfFailsInTheCaseOfRaceConditions() throws Exception {
        final File mvnHome = new File( System.getProperty( "maven.home" ));
        final MavenEmbedderCallable nestedLoad = new MavenEmbedderCallable() {
            @Override
            public void call() throws MavenEmbedderException {
                // Here we invoke the nested call in order to emulate the race condition
                // between multiple threads.
                MavenEmbedderUtils.getMavenVersion(mvnHome);
            }
        };
         
        MavenEmbedderUtils.getMavenVersion(mvnHome, nestedLoad); 
    }
}
