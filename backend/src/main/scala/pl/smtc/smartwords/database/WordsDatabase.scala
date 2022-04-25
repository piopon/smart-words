package pl.smtc.smartwords.database

import io.circe._
import io.circe.literal._
import io.circe.parser._
import pl.smtc.smartwords.model._

import scala.collection.mutable.ListBuffer
import scala.io.Source

class WordsDatabase {

  private val testWordDB: ListBuffer[Word] = ListBuffer()

  implicit val WordDecoder: Decoder[Word] = Decoder.instance {
    (input: HCursor) => for {
      name <- input.downField("name").as[String]
      category <- input.downField("category").as[String]
      definition <- input.downField("description").as[String]
    } yield {
      Word(name, Category.fromString(category), definition)
    }
  }

  def initDatabase(): Boolean = {
    val fileStream = getClass.getResourceAsStream("/dictionary.json")
    val lines = Source.fromInputStream(fileStream).getLines.mkString.stripMargin
    decode[List[Word]](lines) match {
      case Left(fail) =>
        println(s"Invalid dictionary file. ${fail.getMessage}")
        false
      case Right(words) =>
        words.foreach(word => testWordDB += word)
        true
    }
  }

  def getWord(index: Integer): Option[Word] = {
    if (index >= 0 && index < testWordDB.length) {
      Some(testWordDB(index))
    } else {
      None
    }
  }

  def getWords: List[Word] = testWordDB.toList

  def getWordsByCategory(category: Category.Value): List[Word] = {
    testWordDB.toList.filter(word => word.category.equals(category))
  }

  def addWord(word: Word): Boolean = {
    val nameIndex = testWordDB.indexWhere((dbWord: Word) => dbWord.name.equals(word.name))
    if (nameIndex < 0) {
      testWordDB += word
      true
    } else {
      false
    }
  }

  def updateWord(index: Integer, word: Word): Boolean = {
    if (index >= 0 && index < testWordDB.length) {
      testWordDB.update(index, word)
      true
    } else {
      false
    }
  }
}
