package pl.smtc.smartwords.model

import org.scalatest.funsuite.AnyFunSuite

class KindTest extends AnyFunSuite {

  test("testFromString") {
    assert(Kind.fromString("languages") === Kind.languages)
    assert(Kind.fromString("questions") === Kind.questions)
    assert(Kind.fromString("unknown") === Kind.unknown)
  }

  test("testUppercaseFromString") {
    assert(Kind.fromString("LANGUAGES") === Kind.languages)
    assert(Kind.fromString("QUESTIONS") === Kind.questions)
    assert(Kind.fromString("UNKNOWN") === Kind.unknown)
  }

  test("testUnknownFromString") {
    assert(Kind.fromString("random") === Kind.unknown)
    assert(Kind.fromString("not-recognized") === Kind.unknown)
    assert(Kind.fromString("longs") === Kind.unknown)
    assert(Kind.fromString("quest-ions") === Kind.unknown)
  }
}
