# smart-words / service-word / resources

This directory contains dictionary JSON files with words
names, categories, and definitions for all supported types
of games with language and mode distinction.

> **Warning**<br>
> Current logic is implemented to handle only `game=quiz`
> with optional `mode` integer identifiers and different
> `language` values. This document describes the desired
> state of "Smart Words" web application, and it will be updated
> accordingly.

To work properly with the whole "Smart Words" ecosystem and
the words service (this project) the JSON dictionary file
names have to follow the specified convention:

### `words-[game]-[mode]-[language]@[description].json`

where:
* `mode` presence in the file name depends on the type
  of the `game` value. It is an integer value, and it is
  supported only for `game = quiz` for now.
* supported `language` values are defined in a specific
  `game` and `mode` logic implemented in an appropriate
  service. Additionally, there are language flags images
  present in frontend directory which can be used to
  determine the overall internationalization status.
* `description` can contain any string value which can be
  useful for partitioning words of the same `game`, `mode`,
  and `language` in a couple of files, or while adding a
  new user file without the risk of changing the built-in
  (provided) dictionary files.

# Examples
* `words-quiz-1-pl@test.json` - this dictionary contains
  words for `quiz` game type, mode: `1`, and language: `pl`.
  It has description: `test`.
* `words-wordle-en@dictionary.json` - this dictionary
  contains words for `wordle` game type and language: `en`.
  It has description: `dictionary`.
* `words-hangman-fr@words.json` - this dictionary contains
  words for `hangman` game type and language: `fr`.
  It has description: `words`.

---
<p align="center">Created by PNK with 💚 @ 2022-2023</p>