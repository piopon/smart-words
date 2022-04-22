package pl.smtc.smartwords.model

/**
 * Model class representing a single word
 * @param name smart word title/name
 * @param category word category
 * @param definition word correct definition
 */
case class Word(name: String, category: Category.Value, definition: String)
