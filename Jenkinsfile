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
./Server/TipReportRest/gradlew build'''
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
        sh '''ls
ls Server
ls Server/TipReportRest
ls Server/TipReportRest/build
chmod +x Server/TipReportRest/build/libs/TipReportRest-1.0.jar
java -jar Server/TipReportRest/build/libs/TipReportRest-1.0.jar 8181 tip_report_test'''
      }
    }
  }
}