const FORM_MODE_ADD = 0;
const FORM_MODE_EDIT = 1;
const WORD_TOAST_INFO = 0;
const WORD_TOAST_WARNING = 1;
const WORD_TOAST_ERROR = 2;
var wordFormMode = FORM_MODE_ADD;
var toastTimeout = 3000;
var wordUnderEdition = undefined;

/**
 * Method used to add new word and show edit word UI form with empty word data.
 */
function addWord() {
  document.getElementById("word-form-title").innerHTML = "add word:";
  document.getElementById("word-form-name").value = "";
  document.getElementById("word-form-cat").value = "";
  document.getElementById("word-form-def").value = "";
  document.getElementById("word-form-def").innerHTML = "";
  document.getElementById("word-form-btn-accept").innerHTML = "add";
  wordFormMode = FORM_MODE_ADD;
  wordUnderEdition = undefined;
}

/**
 * Method used to update and show edit word UI form with specified word data.
 *
 * @param {String} name current word name to be used in UI form and available for edition
 * @param {String} category current word category to be used in UI form and available for edition
 * @param {String} definition current word definition to be used in UI form and available for edition
 */
function editWord(name, category, definition) {
  document.getElementById("word-form-title").innerHTML = "edit word:";
  document.getElementById("word-form-name").value = name;
  document.getElementById("word-form-cat").value = category;
  document.getElementById("word-form-def").value = definition.replaceAll("; ", "\n");
  document.getElementById("word-form-def").innerHTML = definition;
  document.getElementById("word-form-btn-accept").innerHTML = "edit";
  wordFormMode = FORM_MODE_EDIT;
  wordUnderEdition = getWordFromUi();
}

/**
 * Method used to accept word form and add or edit selected word
 */
function acceptWord() {
  var acceptedWord = getWordFromUi();
  if (FORM_MODE_ADD === wordFormMode) {
    if (!validateWord(acceptedWord)) {
      return;
    }
    changeWordUpdateUI(STATE_WORDS_LOAD);
    postWord(acceptedWord, selectedMode, selectedLanguage, refreshWordsCallback);
  } else if (FORM_MODE_EDIT === wordFormMode) {
    if (undefined === wordUnderEdition) {
      wordChangeShowToast(WORD_TOAST_ERROR, "ERROR: word under edition cannot be undefined");
      return;
    }
    if (JSON.stringify(acceptedWord) === JSON.stringify(wordUnderEdition)) {
      wordChangeShowToast(WORD_TOAST_WARNING, "no changes made");
      return;
    }
    if (!validateWord(acceptedWord)) {
      return;
    }
    changeWordUpdateUI(STATE_WORDS_LOAD);
    putWord(wordUnderEdition.name, acceptedWord, selectedMode, selectedLanguage, refreshWordsCallback);
  } else {
    wordChangeShowToast(WORD_TOAST_ERROR, "ERROR: Unknown form mode: " + wordFormMode);
  }
}

/**
 * Method used to construct word object from UI word edit form
 *
 * @returns word object
 */
function getWordFromUi() {
  return {
    name: document.getElementById("word-form-name").value,
    category: document.getElementById("word-form-cat").value,
    description: document.getElementById("word-form-def").value.split("\n"),
  };
}

/**
 * Method used to delete word with specified name.
 *
 * @param {String} name word name to be deleted
 */
function removeWord(name) {
  changeWordUpdateUI(STATE_WORDS_LOAD);
  deleteWord(name, selectedMode, selectedLanguage, refreshWordsCallback);
}

/**
 * Method used as a common callback with refresh words logic when no error is present
 *
 * @param {String} err error string or undefined if no error is present
 * @param {Object} data communication data received in callback
 */
function refreshWordsCallback(err, data) {
  if (err) {
    wordChangeShowToast(WORD_TOAST_ERROR, "ERROR: " + err.message);
    changeWordUpdateUI(0 === err.status ? STATE_WORDS_ERROR : STATE_WORDS_OK);
  } else {
    wordChangeShowToast(WORD_TOAST_INFO, data);
    loadWords();
    changeWordUpdateUI(STATE_WORDS_OK);
  }
}

/**
 * Method used to get word table row HTML content based on input word object
 *
 * @param {Object} item JSON word object to get data from
 * @returns HTML code in a String format to be added to words table DOM
 */
function getWordTableRow(item) {
  let descriptionString = item.description.join("; ");
  let editMethod = `editWord('${item.name}', '${item.category}', '${descriptionString}')`;
  let deleteMethod = `removeWord('${item.name}')`;
  return `<tr>
            <td>${item.name}</td>
            <td>
              <a class="btn-edit no-select" href="#modal" onclick="${editMethod}">EDIT</a>
              <a class="btn-delete no-select" onclick="${deleteMethod}">DELETE</a>
            </td>
            <td>${item.category}</td>
            <td>${descriptionString}</td>
          </tr>`;
}

/**
 * Method used to load all words and add them to HTML table DOM
 *
 * @param {String} game type of the game for which we want to (re)load words view
 * @param {String} mode specific game mode for which we want to (re)load words view
 * @param {String} language type of language for which we want to (re)load words view
 */
function loadWords(game, mode, language) {
  loadWordsUpdateUI(STATE_WORDS_LOAD);
  getWords(game, mode, language, (err, data) => {
    if (err) {
      loadWordsUpdateUI(STATE_WORDS_ERROR);
    } else {
      loadWordsUpdateUI(STATE_WORDS_OK);
      document.querySelector("tbody").innerHTML = Object.values(data)
        .map((item) => getWordTableRow(item))
        .join("");
    }
  });
}

/**
 * Method used to display word change confirmation toast to the user
 *
 * @param {Integer} type of toast to be displayed (accepted values: WORD_TOAST_INFO, WORD_TOAST_WARNING, WORD_TOAST_ERROR)
 * @param {String} message text to be displayed in word change toast
 */
function wordChangeShowToast(type, message) {
  console.log(message);
  var wordToast = document.getElementById("word-change-toast");
  if (wordToast === null) return;
  if (WORD_TOAST_INFO === type) {
    wordToast.className = "information show";
  } else if (WORD_TOAST_WARNING === type) {
    wordToast.className = "warning show";
  } else if (WORD_TOAST_ERROR === type) {
    wordToast.className = "error show";
  } else {
    wordToast.className = "fatal show";
  }
  wordToast.innerHTML = message;
  setTimeout(() => (wordToast.className = wordToast.className.replace("show", "")), toastTimeout);
}

/**
 * Method used to validate specified word object (name, category and description not empty)
 *
 * @param {Object} word to be validated
 * @returns true if object is correct, false otherwise
 */
function validateWord(word) {
  if (word.name === "") {
    wordChangeShowToast(WORD_TOAST_WARNING, "please specify word name");
    return false;
  }
  if (word.category === "") {
    wordChangeShowToast(WORD_TOAST_WARNING, "please specify word category");
    return false;
  }
  if (0 === word.description.length || undefined !== word.description.find((item) => item === "")) {
    wordChangeShowToast(WORD_TOAST_WARNING, "please provide correct word definition");
    return false;
  }
  return true;
}
