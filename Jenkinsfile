#!/usr/bin/env groovy

node {
    checkout scm

    withEnv([
        "JAVA_HOME=${tool 'jdk8'}",
        "PATH+MAVEN=${tool 'mvn'}/bin",
        "PATH+JAVA=${env.JAVA_HOME}/bin",
        ]) {
        stage 'Invoke Maven'
        sh 'mvn clean install -B -U -e'

        stage 'Archive'
        junit 'target/surefire-reports/**/*.xml'
        archiveArtifacts artifacts: 'target/**/*.jar'
    }
}
