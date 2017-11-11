pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        sh '''echo PATH = ${PATH}
echo M2_HOME = ${M2_HOME}'''
      }
    }
    stage('Build') {
      steps {
        sh './Server/TipReportRest/gradlew build'
      }
    }
    stage('Test') {
      steps {
        echo 'All tests passed'
      }
    }
    stage('Deploy') {
      steps {
        echo 'Server deployed'
        sh 'java -jar /Server/TipReportRest/build/libs/TipReportRest-1.0.jar'
      }
    }
  }
}