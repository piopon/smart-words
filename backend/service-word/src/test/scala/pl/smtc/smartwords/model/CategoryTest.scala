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

  test("testUppercaseFromString") {
    assert(Category.fromString("VERB") === Category.verb)
    assert(Category.fromString("NOUN") === Category.noun)
    assert(Category.fromString("ADJECTIVE") === Category.adjective)
    assert(Category.fromString("LATIN") === Category.latin)
    assert(Category.fromString("PERSON") === Category.person)
    assert(Category.fromString("UNKNOWN") === Category.unknown)
  }

  test("testUnknownFromString") {
    assert(Category.fromString("VER") === Category.unknown)
    assert(Category.fromString("NO-u-N") === Category.unknown)
    assert(Category.fromString("DETECT-i/ve") === Category.unknown)
    assert(Category.fromString("persian") === Category.unknown)
  }
}
