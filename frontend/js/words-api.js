const URL = "http://localhost:1111/";
const REQUEST_TIMEOUT = 15_000;

/**
 * Method used to receive all words from smart-words service
 *
 * @param {String} game type of the game for which we want to receive words
 * @param {String} mode specific game mode for which we want to receive words
 * @param {String} language type of language for which we want to receive words
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const getWords = (game, mode, language, callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      details = 0 === getRequest.status ? " - service unavailable" : " - " + getRequest.responseText;
      callback(createErrorObject("cannot get words" + details, getRequest.status), undefined);
    }
  });
  getRequest.open("GET", URL + "words/" + mode + "/" + language);
  getRequest.timeout = REQUEST_TIMEOUT;
  getRequest.send();
};

/**
 * Method used to add new word to smart-words service
 *
 * @param {Object} newWordObject new word to be added to service
 * @param {Integer} mode of the specific game for which we want to add new word
 * @param {String} language name for which we want to add the new word
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const postWord = (newWordObject, mode, language, callback) => {
  const postRequest = new XMLHttpRequest();
  postRequest.addEventListener("readystatechange", () => {
    if (postRequest.DONE !== postRequest.readyState) return;
    if (postRequest.status === 200) {
      callback(undefined, postRequest.responseText);
    } else {
      details = 0 === postRequest.status ? " - service unavailable" : " - " + postRequest.responseText;
      callback(createErrorObject("cannot add word" + details, postRequest.status), undefined);
    }
  });
  postRequest.open("POST", URL + "words/" + mode + "/" + language);
  postRequest.timeout = REQUEST_TIMEOUT;
  postRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  postRequest.send(JSON.stringify(newWordObject));
};

/**
 * Method used to update specified word name with new content in smart-words service
 *
 * @param {String} currWord object name to be updated
 * @param {Object} newWordObject new word object values to be used and updated in service
 * @param {Integer} mode of the specific game for which we want to update selected word
 * @param {String} language name for which we want to update selected word
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const putWord = (currWord, newWordObject, mode, language, callback) => {
  const putRequest = new XMLHttpRequest();
  putRequest.addEventListener("readystatechange", () => {
    if (putRequest.DONE !== putRequest.readyState) return;
    if (putRequest.status === 200) {
      callback(undefined, putRequest.responseText);
    } else {
      details = 0 === putRequest.status ? " - service unavailable" : " - " + putRequest.responseText;
      callback(createErrorObject("cannot edit word" + details, putRequest.status), undefined);
    }
  });
  putRequest.open("PUT", URL + "words/" + mode + "/" + language + "/" + currWord);
  putRequest.timeout = REQUEST_TIMEOUT;
  putRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  putRequest.send(JSON.stringify(newWordObject));
};

/**
 * Method used to remove specified word in smart-words service
 *
 * @param {String} wordName object name to be removed
 * @param {Integer} mode of the specific game for which we want to delete selected word
 * @param {String} language name for which we want to delete selected word
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const deleteWord = (wordName, mode, language, callback) => {
  const deleteRequest = new XMLHttpRequest();
  deleteRequest.addEventListener("readystatechange", () => {
    if (deleteRequest.DONE !== deleteRequest.readyState) return;
    if (deleteRequest.status === 200) {
      callback(undefined, deleteRequest.responseText);
    } else {
      details = 0 === deleteRequest.status ? " - service unavailable" : " - " + deleteRequest.responseText;
      callback(createErrorObject("cannot delete word" + details, deleteRequest.status), undefined);
    }
  });
  deleteRequest.open("DELETE", URL + "words/" + mode + "/" + language + "/" + wordName);
  deleteRequest.timeout = REQUEST_TIMEOUT;
  deleteRequest.send();
};

/**
 * Method used to receive used words dictionaries
 *
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error object and data object.
 */
const getDictionaries = (callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      details = 0 === getRequest.status ? " - service unavailable" : " - " + getRequest.responseText;
      callback(createErrorObject("cannot get dictionaries " + details, getRequest.status), undefined);
    }
  });
  getRequest.open("GET", URL + "dictionaries");
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