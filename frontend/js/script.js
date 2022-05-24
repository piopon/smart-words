const URL = 'http://localhost:1234/';

const getWords = (callback) => {
    const request = new XMLHttpRequest();

    request.addEventListener('readystatechange', () => {
        if (request.DONE !== request.readyState) return;
        if (request.status === 200) {
            callback(undefined, JSON.parse(request.responseText));
        } else {
            callback('cannot get words [' + request.status + ']', undefined);
        }
    });
    request.open('GET', URL + 'words');
    request.send();
};

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