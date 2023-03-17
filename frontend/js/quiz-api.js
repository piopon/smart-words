const URL = "http://localhost:2222/";
const REQUEST_TIMEOUT = 15_000;

/**
 * Method used to send a request to the quiz service to start a new quiz
 *
 * @param {String} questionsNo number of questions for new quiz instance
 * @param {Integer} modeId unique identifier of quiz mode which we want to start
 * @param {String} language selection for new quiz instance
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const postQuizStart = (questionsNo, modeId, language, callback) => {
  const postRequest = new XMLHttpRequest();
  postRequest.addEventListener("readystatechange", () => {
    if (postRequest.DONE !== postRequest.readyState) return;
    if (postRequest.status === 200) {
      callback(undefined, postRequest.responseText);
    } else {
      callback(createErrorObject(postRequest.responseText, postRequest.status), undefined);
    }
  });
  postRequest.open("POST", URL + "quiz/start?size=" + questionsNo + "&lang=" + language + "&mode=" + modeId);
  postRequest.timeout = REQUEST_TIMEOUT;
  postRequest.send();
};

/**
 * Method used to send a request to the quiz service to receive a specified question number
 *
 * @param {String} quizID the UUID of the quiz
 * @param {Integer} questionNo the number of question/word to receive
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const getQuestionNo = (quizID, questionNo, callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      callback(createErrorObject("cannot get question no " + questionNo, getRequest.status), undefined);
    }
  });
  getRequest.open("GET", URL + "quiz/" + quizID + "/question/" + questionNo);
  getRequest.timeout = REQUEST_TIMEOUT;
  getRequest.send();
};

/**
 * Method used to send a request to the quiz service to post an answer for specified question number
 *
 * @param {String} quizID the UUID of the quiz
 * @param {Integer} questionNo the number of question/word to be answered
 * @param {Integer} answerNo the number of answer for specified question number
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const postQuestionAnswer = (quizID, questionNo, answerNo, callback) => {
  const postRequest = new XMLHttpRequest();
  postRequest.addEventListener("readystatechange", () => {
    if (postRequest.DONE !== postRequest.readyState) return;
    if (postRequest.status === 200) {
      callback(undefined, JSON.parse(postRequest.responseText));
    } else {
      callback(createErrorObject("cannot post answer for question no " + questionNo, postRequest.status), undefined);
    }
  });
  postRequest.open("POST", URL + "quiz/" + quizID + "/question/" + questionNo + "/" + answerNo);
  postRequest.timeout = REQUEST_TIMEOUT;
  postRequest.send();
};

/**
 * Method used to send a request to the quiz service to stop a quiz with specified ID
 *
 * @param {String} quizID the UUID of the quiz
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const getQuizStop = (quizID, callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      callback(createErrorObject("cannot stop quiz", getRequest.status), undefined);
    }
  });
  getRequest.open("GET", URL + "quiz/" + quizID + "/stop");
  getRequest.timeout = REQUEST_TIMEOUT;
  getRequest.send();
};

/**
 * Method used to send a request to the quiz service to create a new empty quiz mode
 *
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const postQuizMode = (callback) => {
  const postRequest = new XMLHttpRequest();
  postRequest.addEventListener("readystatechange", () => {
    if (postRequest.DONE !== postRequest.readyState) return;
    if (postRequest.status === 200) {
      callback(undefined, JSON.parse(postRequest.responseText));
    } else {
      callback(createErrorObject("cannot create new quiz mode", postRequest.status), undefined);
    }
  });
  postRequest.open("POST", URL + "modes");
  postRequest.timeout = REQUEST_TIMEOUT;
  postRequest.send();
};

const putQuizMode = (id, newMode, callback) => {
  const putRequest = new XMLHttpRequest();
  putRequest.addEventListener("readystatechange", () => {
    if (putRequest.DONE !== putRequest.readyState) return;
    if (putRequest.status === 200) {
      callback(undefined, putRequest.responseText);
    } else {
      callback(createErrorObject("cannot create new quiz mode", putRequest.status), undefined);
    }
  });
  putRequest.open("PUT", URL + "modes/" + id);
  putRequest.timeout = REQUEST_TIMEOUT;
  putRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  putRequest.send(JSON.stringify(newMode));
};

/**
 * Method used to send a request to the quiz service to receive all quiz modes
 *
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
 const getQuizModes = (callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      callback(createErrorObject("cannot get quiz modes", getRequest.status), undefined);
    }
  });
  getRequest.open("GET", URL + "modes");
  getRequest.timeout = REQUEST_TIMEOUT;
  getRequest.send();
};

/**
 * Method used to send a request to the quiz service to receive all quiz modes settings
 *
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const getModeSettings = (callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      callback(createErrorObject("cannot get quiz mode settings", getRequest.status), undefined);
    }
  });
  getRequest.open("GET", URL + "modes/settings");
  getRequest.timeout = REQUEST_TIMEOUT;
  getRequest.send();
};

/**
 * Method used to create error object from message and response status
 *
 * @param {String} message of the error to be stored in error object
 * @param {Integer} status of the response to be stored in error object
 * @returns error callback object
 */
 function createErrorObject(message, status) {
  return {
    message: message,
    status: status,
  };
}