/**
 * Method used to edit word with specified name.
 *
 * @param {String} name word name to be edited
 */
function editWord(name) {
    console.log("edit: " + name);
}

/**
 * Method used to delete word with specified name.
 *
 * @param {String} name word name to be deleted
 */
function removeWord(name) {
    deleteWord(name, (err, data) => {
        if (err) {
            console.log('ERROR: ' + err);
        } else {
            loadWords();
            console.log('Removed: ' + data.name);
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
            console.log('ERROR: ' + err);
        } else {
            document.querySelector('tbody').innerHTML = Object.values(data).map((item) => getWordTableRow(item)).join("");
        }
    });
}

// called on words.html site load
loadWords();