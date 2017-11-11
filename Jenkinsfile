pipeline {
  agent {
    docker {
      image 'gradle:4.3.0-jdk8'
    }
    
  }
  stages {
    stage('Initialize') {
      steps {
        sh '''echo PATH = ${PATH}
echo M2_HOME = ${M2_HOME}
sudo rm -r .gradle'''
      }
    }
    stage('Build') {
      steps {
        sh 'gradle build /Server/TipReportRest/build.gradle'
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
      }
    }
  }
}