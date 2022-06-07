const URL = "http://localhost:1234/";

const startQuiz = (questionsNo, callback) => {
  const postRequest = new XMLHttpRequest();
  postRequest.addEventListener("readystatechange", () => {
    if (postRequest.DONE !== postRequest.readyState) return;
    if (postRequest.status === 200) {
      callback(undefined, postRequest.responseText);
    } else {
      callback("cannot start new quiz [" + postRequest.status + "]", undefined);
    }
  });
  postRequest.open("POST", URL + "quiz/start?size=" + questionsNo);
  postRequest.send();
};
