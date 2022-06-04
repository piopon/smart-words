/**
 * Method used to edit word with specified name.
 *
 * @param {String} name word name to be edited
 */
function editWord(name, category, definition) {
  // update UI elements text
  document.getElementById("word-edit-title").innerHTML = "Edit word:";
  document.getElementById("word-name").value = name;
  document.getElementById("word-cat").value = category;
  document.getElementById("word-def").innerHTML = definition;
  document.getElementById("btn-word-edit-accept").value = "edit";
  // show edit form
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
