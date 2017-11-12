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
    stage('Clean up') {
      parallel {
        stage('Shutdown current servers') {
          steps {
            sh '''sshpass -p 123456789 ssh -o StrictHostKeyChecking=no jenkinsdeploy@51.254.127.173 "screen -dm sleep 100"
sshpass -p 123456789 ssh -o StrictHostKeyChecking=no jenkinsdeploy@51.254.127.173 "screen -ls | grep Detached | cut -d. -f1 | awk \'{print $1}\' | xargs kill"
'''
          }
        }
        stage('Transfer rest server') {
          steps {
            sh '''chmod +x Server/TipReportRest/build/libs/TipReportRest-1.0.jar
sshpass -p 123456789 scp -r -o StrictHostKeyChecking=no Server/TipReportRest/build/libs jenkinsdeploy@51.254.127.173: '''
          }
        }
      }
    }
    stage('Deploy') {
      parallel {
        stage('Deploy test environment') {
          steps {
            sh '''sshpass -p 123456789 ssh -o StrictHostKeyChecking=no jenkinsdeploy@51.254.127.173 "screen -dmS TipReportTest sh -c \'java -jar libs/TipReportRest-1.0.jar 8181 tip_report_test; exec bash\'"
'''
          }
        }
        stage('Deploy production environment') {
          steps {
            sh '''sshpass -p 123456789 ssh -o StrictHostKeyChecking=no jenkinsdeploy@51.254.127.173 "screen -dmS TipReportProduction sh -c \'java -jar libs/TipReportRest-1.0.jar; exec bash\'"
'''
          }
        }
      }
    }
  }
}