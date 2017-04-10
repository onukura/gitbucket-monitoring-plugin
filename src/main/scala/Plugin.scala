import gitbucket.core.controller.Context
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.plugin._
import gitbucket.core.service.SystemSettingsService.SystemSettings
import gitbucket.monitoring.controllers.MonitoringController
import io.github.gitbucket.solidbase.model.Version
import javax.servlet.ServletContext

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "monitoring"
  override val pluginName: String = "Monitoring Plugin"
  override val description: String = "Monitoring resources"
  override val versions: List[Version] = List(new Version("1.0.0"))

  override val systemSettingMenus: Seq[(Context) => Option[Link]] = Seq(
    (context: Context) => Some(Link("monitoring", "Monitoring", "admin/monitoring"))
  )

  override val controllers = Seq(
    "/admin/monitoring" -> new MonitoringController
  )
}