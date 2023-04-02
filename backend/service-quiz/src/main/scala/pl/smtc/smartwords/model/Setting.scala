package pl.smtc.smartwords.model

/**
 * Model class representing single quiz mode setting
 * @param kind type that will determine the control in the UI
 * @param label text value to be displayed in UI
 * @param details additional data for concrete type settings
 */
case class Setting(kind: Kind.Value, label: String, details: String)
