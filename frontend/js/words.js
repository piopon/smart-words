const FORM_MODE_ADD = 0;
const FORM_MODE_EDIT = 1;
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
    changeWordUpdateUiState(LOAD_WORDS_LOAD);
    postWord(acceptedWord, refreshWordsCallback);
  } else if (FORM_MODE_EDIT === wordFormMode) {
    if (undefined === wordUnderEdition) {
      console.log("ERROR: word under edition cannot be undefined");
      return;
    }
    changeWordUpdateUiState(LOAD_WORDS_LOAD);
    putWord(wordUnderEdition.name, acceptedWord, refreshWordsCallback);
  } else {
    console.log("ERROR: Unknown form mode: " + wordFormMode);
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
  changeWordUpdateUiState(LOAD_WORDS_LOAD);
  deleteWord(name, refreshWordsCallback);
}

/**
 * Method used as a common callback with refresh words logic when no error is present
 *
 * @param {String} err error string or undefined if no error is present
 * @param {Object} data communication data received in callback
 */
function refreshWordsCallback(err, data) {
  if (err) {
    console.log("ERROR: " + err);
    changeWordUpdateUiState(LOAD_WORDS_ERROR);
  } else {
    wordChangeConfirmation(data);
    loadWords();
    changeWordUpdateUiState(LOAD_WORDS_OK);
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
 */
function loadWords() {
  loadWordsUpdateUiState(LOAD_WORDS_LOAD);
  getWords((err, data) => {
    if (err) {
      loadWordsUpdateUiState(LOAD_WORDS_ERROR);
    } else {
      loadWordsUpdateUiState(LOAD_WORDS_OK);
      document.querySelector("tbody").innerHTML = Object.values(data)
        .map((item) => getWordTableRow(item))
        .join("");
    }
  });
}

/**
 * Method used to display word change confirmation toast to the user
 *
 * @param {String} message text to be displayed in word change toast
 */
function wordChangeConfirmation(message) {
  var wordToast = document.getElementById("word-change-toast");
  wordToast.className = "show";
  wordToast.innerHTML = message;
  setTimeout(() => wordToast.className = wordToast.className.replace("show", ""), toastTimeout);
}

// called on words.html site load
loadWords();
