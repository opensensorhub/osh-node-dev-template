pipeline {
    agent any
    environment {
        // Change the following with the appropriate git URL to pull from and the docker URL to push to.
        GIT_URL = 'GIT-URL'
        GIT_CREDENTIALS = 'JENKINS-GIT-CREDENTIALS'
        DCR_URL = 'DOCKER-REGISTRY-URL'
        // Set GROUP/PROJECT name example 'mine/osh'
        DCR_GROUP_PROJECT = 'GROUP/PROJECT .'
        VERSION = '0.0.0'
        CHANGELOG = ''
        BUILD_DIR = '.'
        IMAGE = ''
    }
    stages {
        stage('Checkout Source') {
            steps {
                dir(BUILD_DIR) {
                    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: false]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: GIT_CREDENTIALS, url: GIT_URL]]])
                }
            }
        }
        stage("Build OpenSensorHub Node") {
            steps {
                dir(BUILD_DIR) {
                    script {
                        sh "chmod 755 gradlew && \
                            ./gradlew build -x test"
                    }
                }
            }
        }
        stage("Tag Version") {
            steps {
                withCredentials([usernamePassword(credentialsId: GIT_CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    script {
                        VERSION = sh(script: "nawk 'BEGIN{ split(\"\", vals ); version=\"0.0.0\" } { if( \$0~\"version\" ){ split(\$0, vals); version = vals[3]; version = substr(version, 2, length(version) - 2); } } END{ printf version }' build.gradle", returnStdout: true)
                        CHANGELOG = sh(script: "git log --oneline \$(git describe --tags --abbrev=0)^..HEAD | grep -v Merge", returnStdout: true)
                        sh(script: "git config user.email \"jenkins@botts-inc.com\" && \
                                    git config user.name \"Jenkins Bot\" && \
                                    git config --local credential.helper \"!f() { echo username=\\$USERNAME; echo password=\\$PASSWORD; }; f\" && \
                                    git tag -a -f -m \"${CHANGELOG}\" ${VERSION} && \
                                    git push ${GIT_URL} tag ${VERSION}")                    }
                    }
                }
            }
        }
        stage('Build Docker image') {
            steps {
                dir(BUILD_DIR) {
                    script {
                        IMAGE = docker.build("${DCR_GROUP_PROJECT}:${VERSION}", "-f dockerfile . --build-arg version=$VERSION")
                        IMAGE.tag("${VERSION}")
                    }
                }
            }
        }
        stage('Push Docker image') {
            steps {
                withCredentials([usernamePassword(credentialsId: GIT_CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    script {
                        docker.withRegistry(DCR_URL) {
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
