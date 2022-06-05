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

function acceptWord() {
  if (FORM_MODE_ADD === wordFormMode) {
    console.log("call word add here...");
  } else if (FORM_MODE_EDIT === wordFormMode) {
    console.log("call word edit here...");
  } else {
    console.log("ERROR: Unknown form mode: " + wordFormMode);
  }
}

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
      loadWords();
    }
  });
}

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
