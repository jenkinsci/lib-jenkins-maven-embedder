package hudson.maven;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Olivier Lamy
 *
 */
public class TestMavenEmbedderUtils {

    @Test
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

    @Test
    public void testMavenVersion2_2_1() throws Exception {
        MavenInformation mavenInformation = MavenEmbedderUtils.getMavenVersion( new File( "src/test/maven-2.2.1" ) );
        assertNotNull( mavenInformation.getVersionResourcePath() );

        assertEquals("2.2.1", mavenInformation.getVersion());
    }

    @Test(expected = MavenEmbedderException.class)
    public void testGetMavenVersionFromInvalidLocation() throws Exception {
        MavenEmbedderUtils.getMavenVersion( new File(System.getProperty("java.home")));
    }

    @Test
    public void testisAtLeastMavenVersion() throws Exception {
       assertTrue( MavenEmbedderUtils.isAtLeastMavenVersion( new File( System.getProperty( "maven.home" ) ), "3.0" ) );
       assertFalse( MavenEmbedderUtils.isAtLeastMavenVersion( new File( "src/test/maven-2.2.1" ), "3.0" ) );
    }

    @Test
    @Issue("JENKINS-42549")
    public void testIfFailsInTheCaseOfRaceConditions() throws Exception {
        final File mvnHome = new File( System.getProperty( "maven.home" ));
        final MavenEmbedderCallable nestedLoad = () -> {
            // Here we invoke the nested call in order to emulate the race condition
            // between multiple threads.
            MavenEmbedderUtils.getMavenVersion(mvnHome);
        };
         
        MavenEmbedderUtils.getMavenVersion(mvnHome, nestedLoad); 
    }
}
