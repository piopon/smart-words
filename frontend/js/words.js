const FORM_MODE_ADD = 0;
const FORM_MODE_EDIT = 1;
var wordFormMode = FORM_MODE_ADD;

/**
 * Method used to add new word and show edit word UI form with empty word data.
 */
function addWord() {
  wordFormMode = FORM_MODE_ADD;
  document.getElementById("word-form-title").innerHTML = "Add word:";
  document.getElementById("word-name").value = "";
  document.getElementById("word-cat").value = "";
  document.getElementById("word-def").innerHTML = "";
  document.getElementById("btn-word-edit-accept").value = "add";
  document.getElementById("word-form").className = "form-visible";
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
  document.getElementById("word-form-title").innerHTML = "Edit word:";
  document.getElementById("word-name").value = name;
  document.getElementById("word-cat").value = category;
  document.getElementById("word-def").innerHTML = definition;
  document.getElementById("btn-word-edit-accept").value = "edit";
  document.getElementById("word-form").className = "form-visible";
}

/**
 * Method used to accept word form and add or edit selected word
 */
function acceptWord() {
  if (FORM_MODE_ADD === wordFormMode) {
    word = {
      "name": document.getElementById("word-name").value,
      "category": document.getElementById("word-cat").value,
      "description": document.getElementById("word-def").value,
    };
    postWord(word, (err, data) => {
      if (err) {
        console.log("ERROR: " + err);
      } else {
        console.log(data);
        loadWords();
      }
    });
  } else if (FORM_MODE_EDIT === wordFormMode) {
    console.log("call word edit here...");
  } else {
    console.log("ERROR: Unknown form mode: " + wordFormMode);
  }
}

/**
 * Method used to cancel current word form and hide it (with no changes)
 */
function cancelWord() {
  document.getElementById("word-form").className = "form-hidden";
}

/**
 * Method used to delete word with specified name.
 *
 * @param {String} name word name to be deleted
 */
function removeWord(name) {
  deleteWord(name, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      console.log("Removed word: " + data);
      loadWords();
    }
  });
}

/**
 * Method used to get word table row HTML content based on input word object
 *
 * @param {Object} item JSON word object to get data from
 * @returns HTML code in a String format to be added to words table DOM
 */
function getWordTableRow(item) {
  return `<tr>
            <td>${item.name}</td>
            <td>
              <button class="btn-edit" onclick="editWord('${item.name}', '${item.category}', '${item.description}')">
                  EDIT
              </button>
              <button class="btn-delete" onclick="removeWord('${item.name}')">
                  DELETE
              </button>
            </td>
            <td>${item.category}</td>
            <td>${item.description}</td>
          </tr>`;
}

/**
 * Method used to load all words and add them to HTML table DOM
 */
function loadWords() {
  getWords((err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      document.querySelector("tbody").innerHTML = Object.values(data)
        .map((item) => getWordTableRow(item))
        .join("");
    }
  });
}

// called on words.html site load
loadWords();
