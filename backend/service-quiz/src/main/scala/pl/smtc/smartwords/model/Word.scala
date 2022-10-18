package pl.smtc.smartwords.model

/**
 * Model class representing a single word
 * @param name smart word title/name
 * @param category word category
 * @param description word correct definitions List containing at least one description
 */
case class Word(name: String, category: String, description: List[String])
