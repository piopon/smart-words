const request = new XMLHttpRequest();

request.addEventListener('readystatechange', () => {
    if (request.DONE !== request.readyState) return;
    if (request.status === 200) {
        console.log(request.responseText);
    } else {
        console.log('ERROR');
    }
});

request.open('GET', 'http://localhost:1234/words');
request.send();