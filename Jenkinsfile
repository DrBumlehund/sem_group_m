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
        sh '''chmod +x Server/TipReportRest/gradlew
cd Server/TipReportRest
./gradlew build'''
      }
    }
    stage('Test') {
      steps {
        echo 'All tests passed'
      }
    }
    stage('Deploy') {
      parallel {
        stage('Deploy') {
          steps {
            echo 'Server deployed'
            sh '''chmod +x Server/TipReportRest/build/libs/TipReportRest-1.0.jar

trap \'screen -X -S TipReportRestProduction quit\' QUIT TERM INT EXIT
screen -DR TipReportRestProduction java -jar Server/TipReportRest/build/libs/TipReportRest-1.0.jar'''
          }
        }
        stage('Deploy test environment') {
          steps {
            sh '''trap \'screen -X -S TipReportRestTest quit\' QUIT TERM INT EXIT
screen -DR TipReportRestTest java -jar Server/TipReportRest/build/libs/TipReportRest-1.0.jar 8181 tip_report_test'''
          }
        }
      }
    }
  }
}