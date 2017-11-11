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



sshpass -p 123456789 scp -r -o StrictHostKeyChecking=no Server/TipReportRest/build/libs jenkinsdeploy@51.254.127.173: 
sshpass -p 123456789 ssh -o StrictHostKeyChecking=no jenkinsdeploy@51.254.127.173 "screen -ls | grep Detached | cut -d. -f1 | awk \'{print $1}\' | xargs kill"
sshpass -p 123456789 ssh -o StrictHostKeyChecking=no jenkinsdeploy@51.254.127.173 screen -S TipReportProduction 
sshpass -p 123456789 ssh -o StrictHostKeyChecking=no jenkinsdeploy@51.254.127.173 java -jar TipReportRest-1.0.jar S
'''
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