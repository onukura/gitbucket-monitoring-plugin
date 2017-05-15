package gitbucket.monitoring.controllers

import io.github.gitbucket.scalatra.forms._
import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.AdminAuthenticator
import gitbucket.core.view.helpers._
import gitbucket.monitoring.information.html._
import gitbucket.monitoring.models._

class MonitoringController extends ControllerBase with AdminAuthenticator {

  val sysInfo = OperatingSystem.osType match {
    case OperatingSystem.Linux => new systemInformation.Info with systemInformation.Linux
    case OperatingSystem.Mac => new systemInformation.Info with systemInformation.Mac
    case OperatingSystem.Windows => new systemInformation.Action with systemInformation.Windows
    case _ => new systemInformation.Action with systemInformation.Other
  }

  val machineRes = OperatingSystem.osType match {
    case OperatingSystem.Linux => new machineResources.Resources with machineResources.Linux
    case OperatingSystem.Mac => new machineResources.Resources with machineResources.Mac
    case OperatingSystem.Windows => new machineResources.Action with machineResources.Windows
    case _ => new machineResources.Action with machineResources.Other
  }

  val processInfo = OperatingSystem.osType match {
    case OperatingSystem.Linux => new processInformation.Info with processInformation.Linux
    case OperatingSystem.Mac => new processInformation.Info with processInformation.Mac
    case OperatingSystem.Windows => new processInformation.Action with processInformation.Windows
    case _ => new processInformation.Action with processInformation.Other
  }

  val gBucketLog = OperatingSystem.osType match {
    case OperatingSystem.Linux => new gitbucketLog.GitBucketLog with gitbucketLog.Linux
    case OperatingSystem.Mac => new gitbucketLog.GitBucketLog with gitbucketLog.Mac
    case OperatingSystem.Windows => new gitbucketLog.Action with gitbucketLog.Windows
    case _ => new gitbucketLog.Action with gitbucketLog.Other
  }

  get("/admin/monitoring")(adminOnly {
    redirect(s"/admin/monitoring/systeminformation");
  })

  get("/admin/monitoring/systeminformation")(adminOnly {
    gitbucket.monitoring.information.html.system(sysInfo);
  })

  get("/admin/monitoring/environmentvaliable")(adminOnly {
    gitbucket.monitoring.information.html.environmentValiable(EnvironmentVariable.valiables);
  })

  get("/admin/monitoring/java")(adminOnly {
    redirect(s"/admin/monitoring/java/systemproperties");
  })

  get("/admin/monitoring/java/systemproperties")(adminOnly {
    gitbucket.monitoring.information.java.html.systemproperties(Java.getSystemProperties);
  })

  get("/admin/monitoring/java/memory")(adminOnly {
    gitbucket.monitoring.information.java.html.memory(Java.getMemoryInfo);
  })

  get("/admin/monitoring/machineresources")(adminOnly {
    gitbucket.monitoring.information.html.resources(machineRes);
  })

  get("/admin/monitoring/process")(adminOnly {
    gitbucket.monitoring.information.html.process(processInfo);
  })

  get("/admin/monitoring/logs")(adminOnly {
    redirect(s"/admin/monitoring/logs/logback");
  })

  get("/admin/monitoring/logs/logback")(adminOnly {
    gitbucket.monitoring.information.logs.html.logback(
      LogBack.getLogBackInfo,
      LogBack.getLogBackSettings
    );
  })

  get("/admin/monitoring/logs/gitbucketlog")(adminOnly {
    val lineNum = request.getParameter("line-num")
    if (lineNum != null){
      try {
        val n = lineNum.toInt
        if (n > gitbucketLog.GitBucketLog.desplayLimit) {
          gitbucket.monitoring.information.logs.html.gitbucketlog(gBucketLog.getLog(gitbucketLog.GitBucketLog.desplayLimit));
        } else {
          gitbucket.monitoring.information.logs.html.gitbucketlog(gBucketLog.getLog(n));
        }
      } catch {
        case e: Exception => gitbucket.monitoring.information.logs.html.gitbucketlog(gBucketLog.getLog());
      }
    } else {
      gitbucket.monitoring.information.logs.html.gitbucketlog(gBucketLog.getLog());
    }
  })
}