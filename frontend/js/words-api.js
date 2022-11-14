const URL = "http://localhost:1111/";

/**
 * Method used to receive all words from smart-words service
 *
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
const getWords = (callback) => {
  const getRequest = new XMLHttpRequest();
  getRequest.addEventListener("readystatechange", () => {
    if (getRequest.DONE !== getRequest.readyState) return;
    if (getRequest.status === 200) {
      callback(undefined, JSON.parse(getRequest.responseText));
    } else {
      callback("cannot get words [" + getRequest.status + "]", undefined);
    }
  });
  getRequest.open("GET", URL + "words");
  getRequest.send();
};

/**
 * Method used to add new word to smart-words service
 *
 * @param {Object} newWordObject new word to be added to service
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
const postWord = (newWordObject, callback) => {
  const postRequest = new XMLHttpRequest();
  postRequest.addEventListener("readystatechange", () => {
    if (postRequest.DONE !== postRequest.readyState) return;
    if (postRequest.status === 200) {
      callback(undefined, postRequest.responseText);
    } else {
      callback("cannot add word [" + postRequest.status + "]", undefined);
    }
  });
  postRequest.open("POST", URL + "words");
  postRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  postRequest.send(JSON.stringify(newWordObject));
};

/**
 * Method used to update specified word name with new content in smart-words service
 *
 * @param {String} currWord object name to be updated
 * @param {Object} newWordObject new word object values to be used and updated in service
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
const putWord = (currWord, newWordObject, callback) => {
  const putRequest = new XMLHttpRequest();
  putRequest.addEventListener("readystatechange", () => {
    if (putRequest.DONE !== putRequest.readyState) return;
    if (putRequest.status === 200) {
      callback(undefined, putRequest.responseText);
    } else {
      callback("cannot edit word [" + putRequest.status + "]", undefined);
    }
  });
  putRequest.open("PUT", URL + "words/" + currWord);
  putRequest.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  putRequest.send(JSON.stringify(newWordObject));
};

/**
 * Method used to remove specified word in smart-words service
 *
 * @param {String} wordName object name to be removed
 * @param {Function} callback function to be invoked when request is completed.
 *                            It should contain 2 parameters: error string and data object.
 */
const deleteWord = (wordName, callback) => {
  const deleteRequest = new XMLHttpRequest();
  deleteRequest.addEventListener("readystatechange", () => {
    if (deleteRequest.DONE !== deleteRequest.readyState) return;
    if (deleteRequest.status === 200) {
      callback(undefined, JSON.parse(deleteRequest.responseText));
    } else {
      callback("cannot delete word [" + deleteRequest.status + "]", undefined);
    }
  });
  deleteRequest.open("DELETE", URL + "words/" + wordName);
  deleteRequest.send();
};
