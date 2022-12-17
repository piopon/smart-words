package pl.smtc.smartwords.model

/**
 * Model class representing single quiz mode setting
 * @param label text value to be displayed in UI
 * @param kind type that will determine the control in the UI
 * @param details additional data for concrete type settings
 */
case class Setting(label: String, kind: Kind.Value, details: String)
