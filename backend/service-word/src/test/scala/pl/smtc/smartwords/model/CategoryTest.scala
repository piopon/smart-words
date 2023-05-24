package pl.smtc.smartwords.model

import org.scalatest.funsuite.AnyFunSuite

class CategoryTest extends AnyFunSuite {

  test("testFromString") {
    assert(Category.fromString("verb") === Category.verb)
    assert(Category.fromString("noun") === Category.noun)
    assert(Category.fromString("adjective") === Category.adjective)
    assert(Category.fromString("latin") === Category.latin)
    assert(Category.fromString("person") === Category.person)
    assert(Category.fromString("unknown") === Category.unknown)
  }

}
