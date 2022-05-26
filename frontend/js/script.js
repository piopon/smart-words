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