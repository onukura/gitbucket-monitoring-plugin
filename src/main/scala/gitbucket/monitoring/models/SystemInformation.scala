package gitbucket.monitoring.models

import java.util._
import java.time._
import java.nio.file.{Paths, Files}
import scala.sys.process._
import gitbucket.monitoring.utils._

trait SystemInformationBase {
  def timeZone = ZoneId.systemDefault()
  def nowTime = LocalDateTime.now()
  def zoneOffset = timeZone.getRules().getOffset(nowTime)
  def dayOfWeek = nowTime.getDayOfWeek()
  def onDocker: Boolean = {
    try {
      Files.exists(Paths.get("/.dockerenv"))
    } catch {
      case e: Exception => false
    }
  }
  def getUpTime: Either[String, UpTime] = {
    try {
      val result = Process("uptime") !!
      val list = result.drop(result.indexOf("up") + 2).split(",")
      Right(UpTime(
        list(0),
        Process("uptime -s") !!
      ))
    } catch {
      case e: Exception => Left(Message.error)
    }
  }
}

class SystemInformation extends SystemInformationBase {
  val instance = OperatingSystem.osType match {
    case OperatingSystem.Linux => new SystemInformationBase with Linux
    case OperatingSystem.Mac => new SystemInformationBase with Mac
    case OperatingSystem.Windows => new SystemInformationBase with  Windows
    case _ => new SystemInformationBase with Other
  }

  trait Linux extends SystemInformationBase {
    override def getUpTime: Either [String, UpTime] = {
      try {
        val ut = (Process("cat /proc/uptime") !!).split(" ")
        val dt = Time.secondsToDateTime(Rounding.ceil(BigDecimal(ut(0)),0).toInt)
        Right(UpTime(
          dt match {
            case Left(message) => (message)
            case Right(l) => (l.days.toString + " days " + l.hours.toString + " hours " + l.minutes.toString + " minutes ")
          },
          Process("uptime -s") !!
        ))
      } catch {
        case e: Exception => Left(Message.error)
      }
    }
  }

  trait Mac extends SystemInformationBase {

  }

  trait Windows extends SystemInformationBase {
    override def getUpTime: Either[String, UpTime] = {
      try {
        Right(UpTime(
          (Process("powershell -Command \"&{$os=Get-WmiObject win32_operatingsystem;$time=((Get-Date) - $os.ConvertToDateTime($os.lastbootuptime)); $time.Days.ToString() + \\\" days \\\" +  $time.Hours.ToString() + \\\" hours \\\" + $time.Minutes.ToString() + \\\" minutes \\\"}\"") !!),
          (Process("powershell -Command [Management.ManagementDateTimeConverter]::ToDateTime((Get-WmiObject Win32_OperatingSystem).LastBootUpTime)") !!)
        ))
      } catch {
        case e: Exception => Left(Message.error)
      }
    }
  }

  trait Other extends SystemInformationBase {
    override def getUpTime: Either[String, UpTime] = {
      Left(Message.notSupported)
    }
  }
}

case class UpTime (
  uptime: String,
  startTime: String
)