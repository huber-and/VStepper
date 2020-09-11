#!/usr/bin/env groovy
pipeline {

    agent any
    
    options {
        timestamps()
        disableConcurrentBuilds()
		// Keep only the last 5 builds
    	buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
        skipStagesAfterUnstable()
	}
    stages {

        stage('Build') {
            steps {
				withMaven(maven: 'M3', mavenSettingsConfig: 'iot_maven') {
					sh "mvn -B -DskipTests clean package"
    			} 
			}
        }
        stage('Test') {
            steps {
				withMaven(maven: 'M3', mavenSettingsConfig: 'iot_maven') {
					sh "mvn test"
    			} 
			}
		//	post {
        //        always {
        //            junit 'target/surefire-reports/*.xml' 
        //        }
        //    }
        }
        stage('Deploy') {
            steps {
				withMaven(maven: 'M3', mavenSettingsConfig: 'iot_maven') {
					sh "mvn deploy"
    			} 
			}
        }

 
    }

}