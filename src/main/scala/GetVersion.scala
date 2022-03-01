def getRunningScalaVersion: String = {
  try {
    val props = new java.util.Properties
    props.load(getClass.getResourceAsStream("/library.properties"))
    val line = props.getProperty("version.number")
    val Version = """(\d+\.\d+\.\d+).*""".r
    val Version(versionStr) = line
    versionStr
  }
  catch {
    case e: Throwable =>
      e.printStackTrace()
      "0.0.0"
  }
}