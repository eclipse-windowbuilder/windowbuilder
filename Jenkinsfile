pipeline {
  options {
    timeout(time: 50, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr:'10'))
    disableConcurrentBuilds(abortPrevious: true)
  }

  agent {
    label "centos-latest"
  }

  tools {
    maven 'apache-maven-latest'
    jdk 'temurin-jdk17-latest'
  }

  environment {
    CLONE_URL = 'https://github.com/eclipse-windowbuilder/windowbuilder.git'
    CHECKOUT = 'false'
  }

  parameters {
    choice(
      name: 'BUILD_TYPE',
      choices: ['nightly', 'milestone', 'release'],
      description: '''
      Choose the type of build.
      Note that a release build will not promote the build, but rather will promote the most recent milestone build.
      '''
    )

    booleanParam(
      name: 'PROMOTE',
      defaultValue: true,
      description: 'Whether to promote the build to the download server.'
    )
  }

  stages {
    stage('Display Parameters') {
        steps {
            echo "BUILD_TYPE=${params.BUILD_TYPE}"
            echo "PROMOTE=${params.PROMOTE}"
            script {
                env.BUILD_TYPE = params.BUILD_TYPE
                if (env.BRANCH_NAME == 'master') {
                  env.WITH_CREDENTIALS = true
                  if (params.PROMOTE) {
                    env.MAVEN_PROFILES = "-Psign -Ppromote"
                  } else {
                    env.MAVEN_PROFILES = "-Psign"
                  }
                } else {
                  env.WITH_CREDENTIALS = false
                  env.MAVEN_PROFILES = ""
                }
            }
        }
    }

    stage('Git Checkout') {
      when {
        environment name: 'CHECKOUT', value: 'true'
      }
      steps {
        script {
          def gitVariables = checkout(
            poll: false,
            scm: [
              $class: 'GitSCM',
              branches: [[name: '*' + '/master']],
              doGenerateSubmoduleConfigurations: false,
              submoduleCfg: [],
              userRemoteConfigs: [[url: env.CLONE_URL ]]
            ]
          )

          echo "$gitVariables"
          env.GIT_COMMIT = gitVariables.GIT_COMMIT

          env.WITH_CREDENTIALS = true
          if (params.PROMOTE) {
            env.MAVEN_PROFILES = "-Psign -Ppromote"
           } else {
             env.MAVEN_PROFILES = "-Psign"
           }
        }
      }
    }

    stage('Initialize PGP') {
      when {
        environment name: 'WITH_CREDENTIALS', value: 'true'
      }
      steps {
        withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
         sh '''
           gpg --batch --import "${KEYRING}"
           for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done
           '''
        }
      }
    }


    stage('Build') {
      steps {
        script {
          if (env.WITH_CREDENTIALS) {
            sshagent (['projects-storage.eclipse.org-bot-ssh']) {
              withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
                mvn()
              }
            }
          } else {
            mvn()
          }
        }
      }

      post {
        always {
          archiveArtifacts artifacts: '**/target/repository/**/*,**/target/*.zip,**/target/work/data/.metadata/.log', allowEmptyArchive: true
          junit testResults: '**/target/surefire-reports/TEST-*.xml', allowEmptyResults: true
        }
      }
    }
  }
}

def void mvn() {
  wrap([$class: 'Xvnc', useXauthority: true]) {
    sh '''
      mvn \
      $MAVEN_PROFILES \
      -Dmaven.repo.local=$WORKSPACE/.m2/repository \
      --no-transfer-progress \
      -Ddash.fail=false \
      -Dgpg.passphrase="${KEYRING_PASSPHRASE}" \
      -Dorg.eclipse.justj.p2.manager.build.url=$JOB_URL \
      -Dbuild.type=$BUILD_TYPE \
      -Dgit.commit=$GIT_COMMIT \
      clean \
      verify
    '''
  }
}
