package pl.smtc.smartwords.model

/**
 * Model class representing quiz mode
 * @param id the unique mode ID defining concrete mode
 * @param name the mode name which will be displayed in UI
 * @param description mode detailed description (also visible in UI)
 * @param settings the list of setting of a particular quiz mode
 */
case class Mode(var id: Int, name: String, description: String, settings: List[Setting])
