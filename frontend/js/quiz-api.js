const URL = "http://localhost:2222/";

/**
 * Method used to send a request to smart-words service to start a new quiz
 *
 * @param {String} questionsNo number of questions for new quiz instance
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
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

/**
 * Method used to send a request to smart-words service to receive a specified question number
 *
 * @param {String} quizID the UUID of the quiz
 * @param {Integer} questionNo the number of question/word to receive
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
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
};

/**
 * Method used to send a request to smart-words service to post an answer for specified question number
 *
 * @param {String} quizID the UUID of the quiz
 * @param {Integer} questionNo the number of question/word to be answered
 * @param {Integer} answerNo the number of answer for specified question number
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
const postQuestionAnswer = (quizID, questionNo, answerNo, callback) => {
  const postRequest = new XMLHttpRequest();
  postRequest.addEventListener("readystatechange", () => {
    if (postRequest.DONE !== postRequest.readyState) return;
    if (postRequest.status === 200) {
      callback(undefined, JSON.parse(postRequest.responseText));
    } else {
      callback("cannot post answer for question no " + questionNo + " [" + postRequest.status + "]", undefined);
    }
  });
  postRequest.open("POST", URL + "quiz/" + quizID + "/question/" + questionNo + "/" + answerNo);
  postRequest.send();
};

/**
 * Method used to send a request to smart-words service to stop a quiz with specified ID
 *
 * @param {String} quizID the UUID of the quiz
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
const getQuizStop = (quizID, callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      callback("cannot stop quiz [" + getRequest.status + "]", undefined);
    }
  });
  getRequest.open("GET", URL + "quiz/" + quizID + "/stop");
  getRequest.send();
};
