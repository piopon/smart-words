This directory contains dictionary files with words for all
possible word modes and supported languages.

Naming convention of JSON files which is supported by word
service is displayed below:

###`words-[game]-[mode]-[language]-[description].json`

where:
* `mode` presence in the file name depends on the type
  of the `game` value. Currently, it is supported only
  for `game = quiz`.
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

Examples
* `words-quiz-pl-1.json` - this dictionary contains words for
  mode: `quiz`, sub-mode: `1`, and language: `pl`
* `words-wordle-en.json` - this dictionary contains words for
  mode: `wordle` and language: `en`
* `words-hangman-fr.json` - this dictionary contains words for
  mode: `hangman` and language: `fr`

