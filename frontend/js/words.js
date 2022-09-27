const FORM_MODE_ADD = 0;
const FORM_MODE_EDIT = 1;
const LOAD_WORDS_OK = 0;
const LOAD_WORDS_LOAD = 1;
const LOAD_WORDS_ERROR = 2;
var wordFormMode = FORM_MODE_ADD;
var wordUnderEdition = undefined;

/**
 * Method used to add new word and show edit word UI form with empty word data.
 */
function addWord() {
  wordFormMode = FORM_MODE_ADD;
  wordUnderEdition = undefined;
  document.getElementById("word-form-title").innerHTML = "add word:";
  document.getElementById("word-form-name").value = "";
  document.getElementById("word-form-cat").value = "";
  document.getElementById("word-form-def").value = "";
  document.getElementById("word-form-def").innerHTML = "";
  document.getElementById("word-form-btn-accept").innerHTML = "add";
}

/**
 * Method used to update and show edit word UI form with specified word data.
 *
 * @param {String} name current word name to be used in UI form and available for edition
 * @param {String} category current word category to be used in UI form and available for edition
 * @param {String} definition current word definition to be used in UI form and available for edition
 */
function editWord(name, category, definition) {
  wordFormMode = FORM_MODE_EDIT;
  wordUnderEdition = name;
  document.getElementById("word-form-title").innerHTML = "edit word:";
  document.getElementById("word-form-name").value = name;
  document.getElementById("word-form-cat").value = category;
  document.getElementById("word-form-def").value = definition.replaceAll("; ", "\n");
  document.getElementById("word-form-def").innerHTML = definition;
  document.getElementById("word-form-btn-accept").innerHTML = "edit";
}

/**
 * Method used to accept word form and add or edit selected word
 */
function acceptWord() {
  var acceptedWord = getWordFromUi();
  if (FORM_MODE_ADD === wordFormMode) {
    postWord(acceptedWord, refreshWordsCallback);
  } else if (FORM_MODE_EDIT === wordFormMode) {
    if (undefined === wordUnderEdition) {
      console.log("ERROR: word under edition cannot be undefined");
      return;
    }
    putWord(wordUnderEdition, acceptedWord, refreshWordsCallback);
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
  } else {
    console.log(data);
    loadWords();
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
 * Method used to update GUI state while loading words from service
 *
 * @param {Integer} state current loading state (from: LOAD_WORDS_OK, LOAD_WORDS_LOAD, LOAD_WORDS_ERROR)
 */
function loadWordsUpdateUiState(state) {
  let addWordBtn = document.getElementById("btn-add-word");
  let rowElement = document.getElementById("no-words-row");
  let textElement = document.getElementById("no-words-text");
  if (rowElement === null || textElement === null) return;
  if (LOAD_WORDS_OK === state) {
    addWordBtn.className = "enabled";
    addWordBtn.href = "#modal";
    addWordBtn.addEventListener("click", addWord);
    rowElement.className = "row-hidden";
    textElement.innerHTML = "";
    return;
  }
  if (LOAD_WORDS_LOAD === state) {
    addWordBtn.className = "disabled";
    addWordBtn.removeAttribute("href");
    addWordBtn.onclick = null;
    rowElement.className = "row-loading";
    textElement.innerHTML = addLoadingWidget() + "<br>loading words...";
    return;
  }
  if (LOAD_WORDS_ERROR === state) {
    addWordBtn.className = "disabled";
    addWordBtn.removeAttribute("href");
    addWordBtn.onclick = null;
    rowElement.className = "row-visible";
    textElement.innerHTML = addErrorWidget() + "<br>cannot receive words...";
    return;
  }
}

/**
 * Method used to generate HTML code responsible for creating a loader
 *
 * @returns HTML code with loader section
 */
function addLoadingWidget() {
  return `<div id="loader-wrapper">
            <div class="loader">
              <div class="line"></div>
              <div class="line"></div>
              <div class="line"></div>
            </div>
          </div>`;
}

/**
 * Method used to generate HTML code with empty loader placeholder
 *
 * @returns HTML code with loader section placeholder
 */
function addErrorWidget() {
  return `<div id="loader-wrapper"></div>`;
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

// called on words.html site load
loadWords();
