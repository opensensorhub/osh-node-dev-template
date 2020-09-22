pipeline {
    agent { label 'oot-bottsinc-build' }
    environment {
        // Change the following with the appropriate git URL to pull from and the docker URL to push to.
        GIT_URL = 'GIT-URL'
        GIT_CREDENTIALS = 'JENKINS-GIT-CREDENTIALS'
        DCR_URL = 'DOCKER-REGISTRY-URL'
        BUILD_DIR = '.'
        IMAGE = ''
    }
    stages {
        stage('SCM Checkout Node') {
            steps {
                dir(NODE_DIR) {
                    checkout([$class: 'GitSCM', branches: [[name: '*/integ']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: false]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: GIT_CREDENTIALS, url: GIT_URL]]])
                }
            }
        }
        stage('Build Image') {
            steps {
                dir(BUILD_DIR) {
                    script {
                        GIT_COMMIT_HASH = sh(script: "git log -n 1 --pretty=format:'%h'", returnStdout: true)
                        GIT_COMMIT_TIME = sh(script: "git log -n 1 --pretty=format:'%ct'", returnStdout: true)
                        IMAGE = docker.build("${DCR_URL}:${GIT_COMMIT_TIME}-${GIT_COMMIT_HASH}", "-f dockerfile .")
                        IMAGE.tag("BUILD-${currentBuild.number}")
                    }
                }
            }
        }
        stage('Push Image') {
            steps {
                dir(BUILD_DIR) {
                    withCredentials([usernamePassword(credentialsId: 'DCR-REGISTRY-CREDENTIALS', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        script {
                            sh "echo ${PASSWORD} | docker login ${DCR_URL} -u ${USERNAME} --password-stdin"
                            IMAGE.push()
                            sh "docker logout ${DCR_URL}"
                        }
                    }
                }
            }
        }
        stage("Cleanup") {
            steps {
                script {
                    sh "docker system prune -f"
                }
            }
        }
    }
}
