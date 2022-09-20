package pl.smtc.smartwords.model

/**
 * Model class representing a single word
 * @param name smart word title/name
 * @param category word category
 * @param definition word correct definition
 * @param dictionary word source dictionary file
 */
case class Word(name: String, category: Category.Value, definition: List[String], var dictionary: String)
