package pl.smtc.smartwords.model

/**
 * Model class representing quiz mode
 * @param name the unique mode name which will be displayed in UI
 * @param description mode detailed description (also visible in UI)
 * @param settings the list of setting of a particular quiz mode
 */
case class Mode(name: String, description: String, settings: List[Setting])
