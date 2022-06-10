const URL = "http://localhost:1234/";

const postQuizStart = (questionsNo, callback) => {
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

const getQuestionNo = (quizID, questionNo, callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      callback("cannot get question no " + questionNo + " [" + getRequest.status + "]", undefined);
    }
  });
  getRequest.open("GET", URL + "quiz/" + quizID + "/question/" + questionNo);
  getRequest.send();
}