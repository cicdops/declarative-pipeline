package pro.javatar.pipeline.stage

import com.cloudbees.groovy.cps.NonCPS
import pro.javatar.pipeline.exception.PipelineException
import pro.javatar.pipeline.model.Env
import pro.javatar.pipeline.model.ReleaseInfo
import pro.javatar.pipeline.service.DeploymentService
import pro.javatar.pipeline.service.impl.DockerDeploymentService
import pro.javatar.pipeline.service.orchestration.DockerService
import pro.javatar.pipeline.util.Logger

import static pro.javatar.pipeline.service.PipelineDslHolder.dsl

class DatabaseBackwardCompatibilityStage extends Stage {

    DockerService dockerService

    DeploymentService deploymentService

    DatabaseBackwardCompatibilityStage(DockerService dockerService, DeploymentService deploymentService) {
        this.dockerService = dockerService
        this.deploymentService = deploymentService
    }

    def execute() throws PipelineException {
        Logger.info("Database backward compatibility execute started:" + toString())
        dsl.timeout(time: 10, unit: 'MINUTES') {
            // deploy current docker image
//            deploymentService.deployArtifact(Env.DEV, releaseInfo)
            // define previous version
            ReleaseInfo previousReleaseInfo = buildPreviousReleaseInfo(releaseInfo)
            if (previousReleaseInfo != null) {
                deploymentService.deployArtifact(Env.DEV, previousReleaseInfo)
            }
        }
        Logger.info("Database backward compatibility execute finished")
    }

    private String lastSuccessfulBuild(build) {
        if (build != null && build.result == "SUCCESS") {
            return build.description
        }
        if (build.getPreviousBuild() != null) {
            lastSuccessfulBuild(build.getPreviousBuild())
        }
        return null
    }

    private ReleaseInfo buildPreviousReleaseInfo(ReleaseInfo info) {
        def previousBuildVersion = lastSuccessfulBuild(dsl.currentBuild)
        if (previousBuildVersion != null) {
            String[] ver = previousBuildVersion.split(":")
            if (ver.length == 2) {
                ReleaseInfo previous = new ReleaseInfo()
                previous.setReleaseVersion(ver[1])
                previous.setDockerImageName(ver[0])
                previous.setDockerImageVersion(ver[1])
                return previous
            }
        }
        return null
    }

    @NonCPS
    @Override
    public String getName() {
        return "Database backward compatibility"
    }
}
