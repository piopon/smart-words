const URL = 'http://localhost:1234/';
const request = new XMLHttpRequest();

const getWords = (callback) => {
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