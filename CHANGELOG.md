Changelog
===

### 3.14

Release date: Jun 12, 2019

* [JENKINS-54530](https://issues.jenkins-ci.org/browse/JENKINS-54530) Upgrade maven embedder from 3.1.0 to at least 3.5.4

### 3.12.1

Release date: Jul 06, 2017

* [JENKINS-40621](https://issues.jenkins-ci.org/browse/JENKINS-40621) - 
Prevent leaked file descriptors when invoking `MavenEmbedderUtils#getMavenVersion()`
([PR #5](https://github.com/jenkinsci/lib-jenkins-maven-embedder/pull/5))
* [JENKINS-42549](https://issues.jenkins-ci.org/browse/JENKINS-42549) -
Prevent file access errors in `JARUrlConnection` due to the parallel reading of JAR resources in `MavenEmbedderUtils#getMavenVersion()`
(regression in 3.12)


### 3.12

Release date: Feb 16, 2017

:exclamation: The release introduced the [JENKINS-42549](https://issues.jenkins-ci.org/browse/JENKINS-42549) regression.
It is recommended to use newer versions.

* Update from sisu-guice `3.1.3` to guice `4.0`
* Update plexus-classworlds from `2.4.2` to `2.5.1`
* Update Sonatype Aether `0.9.0.M2` to Eclipse Aether `1.1.0`
* Update Apache Wagon `2.4` to `2.12`
* Update org.eclipse.sisu:org.eclipse.sisu.plexus `0.0.0.M2a` to `0.3.3`

### 3.11

Release date: Jul 23, 2013

* Inherit distribution management from the Jenkins Parent POM

### Previous versions

No Changelog, see the commit history
