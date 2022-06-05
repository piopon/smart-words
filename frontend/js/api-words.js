const URL = 'http://localhost:1234/';

const getWords = (callback) => {
    const getRequest = new XMLHttpRequest();
    getRequest.addEventListener('readystatechange', () => {
        if (getRequest.DONE !== getRequest.readyState) return;
        if (getRequest.status === 200) {
            callback(undefined, JSON.parse(getRequest.responseText));
        } else {
            callback('cannot get words [' + getRequest.status + ']', undefined);
        }
    });
    getRequest.open('GET', URL + 'words');
    getRequest.send();
};

const postWord = (newWordObject, callback) => {
    const postRequest = new XMLHttpRequest();
    postRequest.addEventListener('readystatechange', () => {
        if (postRequest.DONE !== postRequest.readyState) return;
        if (postRequest.status === 200) {
            callback(undefined, postRequest.responseText);
        } else {
            callback('cannot add word [' + postRequest.status + ']', undefined);
        }
    });
    postRequest.open('POST', URL + 'words');
    postRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    postRequest.send(JSON.stringify(newWordObject));
};

const deleteWord = (wordName, callback) => {
    const deleteRequest = new XMLHttpRequest();
    deleteRequest.addEventListener('readystatechange', () => {
        if (deleteRequest.DONE !== deleteRequest.readyState) return;
        if (deleteRequest.status === 200) {
            callback(undefined, JSON.parse(deleteRequest.responseText));
        } else {
            callback('cannot get words [' + deleteRequest.status + ']', undefined);
        }
    });
    deleteRequest.open('DELETE', URL + 'words/' + wordName);
    deleteRequest.send();
}