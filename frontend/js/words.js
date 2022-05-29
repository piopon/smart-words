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
    console.log("delete: " + name);
}

// called on words.html site load
getWords((err, data) => {
    if (err) {
        console.log('ERROR: ' + err);
    } else {
        const words = data.map((item) => {
            return `<tr>
                        <td>${item.name}</td>
                        <td>
                            <button class="buttonEdit">EDIT</button>
                            <button class="buttonDelete">DELETE</button>
                        </td>
                        <td>${item.category}</td>
                        <td>${item.description}</td>
                    </tr>`;
        }).join("");
        document.querySelector('tbody').innerHTML = words;
    }
})