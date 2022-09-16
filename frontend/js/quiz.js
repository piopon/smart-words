const STATUS_NO_ANSWER = 0;
const STATUS_ANSWER_OK = 1;
const STATUS_ANSWER_NOK = -1;
const START_QUIZ_OK = 0;
const START_QUIZ_LOAD = 1;
const START_QUIZ_ERROR = 2;
var quizID = undefined;
var totalQuestionsNo = undefined;
var currentQuestionNo = undefined;
var questionsStatus = undefined;

/**
 * Method used to receive number of question, start quiz and receive UUID
 */
function startQuiz() {
  startQuizUpdateUiState(START_QUIZ_LOAD);
  totalQuestionsNo = document.getElementById("quiz-mode-settings-question-no").value;
  questionsStatus = Array(parseInt(totalQuestionsNo)).fill(STATUS_NO_ANSWER);
  postQuizStart(totalQuestionsNo, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
      startQuizUpdateUiState(START_QUIZ_ERROR);
    } else {
      quizID = data;
      currentQuestionNo = 0;
      requestQuestionNo(currentQuestionNo);
      updateQuestionStatus();
      startQuizUpdateUiState(START_QUIZ_OK);
    }
  });
}

/**
 * Method used to update GUI state while starting quiz from service
 *
 * @param {Integer} state current loading state (from: START_QUIZ_OK, START_QUIZ_LOAD, START_QUIZ_ERROR)
 */
function startQuizUpdateUiState(state) {
  let startQuizBtn = document.getElementById("quiz-mode-controls-start");
  let startQuizInfo = document.getElementById("quiz-mode-controls-info");
  if (startQuizBtn === null || startQuizInfo === null) return;
  if (START_QUIZ_OK === state) {
    startQuizBtn.addEventListener("click", startQuiz);
    startQuizBtn.className = "dynamic-border";
    startQuizBtn.disabled = false;
    startQuizBtn.innerHTML = "start";
    startQuizInfo.className = "hide";
    return;
  }
  if (START_QUIZ_LOAD === state) {
    startQuizBtn.onclick = null;
    startQuizBtn.className = "loading";
    startQuizBtn.disabled = false;
    startQuizBtn.innerHTML = "connecting...";
    startQuizInfo.className = "hide";
    return;
  }
  if (START_QUIZ_ERROR === state) {
    startQuizBtn.onclick = null;
    startQuizBtn.disabled = true;
    startQuizBtn.innerHTML = "service unavailable";
    startQuizInfo.className = "";
    startQuizInfo.title =
      "Cannot connect to a backend service!\n" +
      "Please verify its running and connection status and refresh this page.";
    return;
  }
}

/**
 * Method used to request a question with specified number
 *
 * @param {Integer} number of a requested question
 */
function requestQuestionNo(number) {
  getQuestionNo(quizID, number, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      currentQuestionNo = number;
      console.log(data);
      displayQuestion(data);
    }
  });
}

/**
 * Method (wrapper) used request a next question (relative to currently displayed one)
 */
function requestNextQuestion() {
  if (!verifyQuestionNo(++currentQuestionNo)) {
    console.log("Invalid question number value [" + number + "]");
  }
  requestQuestionNo(currentQuestionNo);
}

/**
 * Method (wrapper) used request a previous question (relative to currently displayed one)
 */
function requestPrevQuestion() {
  if (!verifyQuestionNo(--currentQuestionNo)) {
    console.log("Invalid question number value [" + number + "]");
  }
  requestQuestionNo(currentQuestionNo);
}

/**
 * Method used to verify specified question number
 *
 * @param {Integer} number of a question to be verified
 * @returns true if number is correct, false otherwise
 */
function verifyQuestionNo(number) {
  if (number === undefined) return false;
  if (number < 0 || number > totalQuestionsNo) {
    return false;
  }
  return true;
}

/**
 * Method used to display a specified question number with its all four options
 *
 * @param {Object} questionObject to be displayed (word + four options)
 */
function displayQuestion(questionObject) {
  let questionStatus = `question ${currentQuestionNo + 1}/${totalQuestionsNo}`;
  document.getElementById("quiz-title-container-label").innerHTML = `quiz - guess definition - ${questionStatus}:`;
  document.getElementById("quiz-question-container").className = "container-visible";
  document.getElementById("quiz-modes-container").className = "container-hidden";
  questionHtml = getWordHtml(questionObject.word);
  questionHtml += `<div id="question-options">`;
  for (var optionNo = 0; optionNo < questionObject.options.length; optionNo++) {
    questionHtml += getOptionHtml(questionObject, optionNo);
  }
  questionHtml += `</div>`;
  questionHtml += getControlButtonsHtml();
  document.getElementById("quiz-question-container").innerHTML = questionHtml;
  document.getElementById("prev-question").disabled = currentQuestionNo <= 0;
  document.getElementById("stop-quiz").disabled = currentQuestionNo >= totalQuestionsNo - 1;
}

/**
 * Method used to receive question word HTML code
 *
 * @param {String} word name to be displayed as a question word
 * @returns HTML code with word name
 */
function getWordHtml(word) {
  return `<div id="question-word" class="question-word-div">${word}</div>`;
}

/**
 * Method used to receive question option HTML code
 *
 * @param {Object} question from which to receive option no and init status
 * @param {Integer} optionNo number of option
 * @returns HTML code with word option
 */
function getOptionHtml(question, optionNo) {
  let isNewQuestion = null === question.correct;
  buttonAction = getAnswerButtonAction(isNewQuestion, optionNo);
  buttonClass = getAnswerButtonClass(isNewQuestion, optionNo == question.answer ? question.correct : null);
  return `<div id="question-option-${optionNo}">
            <button id="answer-${optionNo}" class="${buttonClass}" onclick="${buttonAction}">
              ${optionNo}) ${question.options[optionNo]}
            </button>
          </div>`;
}

/**
 * Method used to receive control buttons HTML code
 *
 * @returns HTML code for question control buttons (previous and next)
 */
function getControlButtonsHtml() {
  let placeholderForPrevBtn = getControlButtonHtml("prev-question", "PREVIOUS", "requestPrevQuestion()", "static-border");
  let placeholderForNextBtn = currentQuestionNo === totalQuestionsNo - 1
      ? getControlButtonHtml("finish-quiz", "FINISH", "checkQuizEnd()", "static-border")
      : getControlButtonHtml("next-question", "NEXT", "requestNextQuestion()", "static-border");
  let placeholderForStopBtn = getControlButtonHtml("stop-quiz", "STOP", "checkQuizEnd()", "dynamic-border");
  return `<div id="question-control">
            ${placeholderForPrevBtn}
            ${placeholderForNextBtn}
            ${placeholderForStopBtn}
          </div>`;
}

/**
 * Method used to receive a HTML code for a single control button
 *
 * @param {String} id identifier for created control button (used for specific button style)
 * @param {String} text label of the button
 * @param {String} action method name executed after pressing button
 * @param {String} borderType static or dynamic border
 * @returns HTML code for question control button
 */
function getControlButtonHtml(id, text, action, borderType) {
  return `<button id="${id}" class="question-control-btn ${borderType}" onclick="${action}">
            ${text}
          </button>`;
}

/**
 * Method used to answer a specified question number with input answer number
 *
 * @param {Integer} number of a question to be answered (accepted values: 0 - totalQuestionsNo)
 * @param {Integer} answerNo number of answer for specified question (accepted values: 0-3)
 */
function answerQuestionNo(number, answerNo) {
  postQuestionAnswer(quizID, number, answerNo, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      questionsStatus[currentQuestionNo] = data === true ? STATUS_ANSWER_OK : STATUS_ANSWER_NOK;
      for (let i in [0, 1, 2, 3]) {
        document.getElementById("answer-" + i).onclick = null;
        document.getElementById("answer-" + i).className = getAnswerButtonClass(false, answerNo === i ? data : null);
      }
      updateQuestionStatus();
    }
  });
}

/**
 * Method used to receive answer button action for HTML code
 *
 * @param {Boolean} isNewQuestion flag indicating if this question is new or not
 * @param {Integer} optionNo number of option for which to update action
 * @returns answer option action (if new question is true), or empty (if new question is false)
 */
function getAnswerButtonAction(isNewQuestion, optionNo) {
  return isNewQuestion ? `answerQuestionNo('${currentQuestionNo}', '${optionNo}')` : ``;
}

/**
 * Method used to receive answer button class for HTML code
 *
 * @param {Boolean} isAnswerCorrect flag indicating the current status of answer correctness
 * @returns "regular" class if input boolean is null, "ok" class if input is true, "nok" when false
 */
function getAnswerButtonClass(isNewQuestion, isAnswerCorrect) {
  if (null === isAnswerCorrect) {
    return isNewQuestion ? "question-option-btn" : "question-option-btn-disabled";
  }
  return isAnswerCorrect ? "question-option-btn-ok" : "question-option-btn-nok";
}

/**
 * Method used to create and update question depending on current questionStatus array contents
 *
 * @param {Boolean} enableStatusNavigation flag indicating if status should also have question navigation functionalities
 */
function updateQuestionStatus(enableStatusNavigation = true) {
  let questionStatusHtml = `quiz questions:`;
  for (let i = 0; i < questionsStatus.length; i++) {
    let clickClass = (true === enableStatusNavigation) ? "navigation-on" : "navigation-off";
    let clickAction = (true === enableStatusNavigation) ? `onclick="requestQuestionNo(${i})"` : ``;
    questionStatusHtml += `<div class="question-status${questionsStatus[i]} ${clickClass}" ${clickAction}>
                            ${i+1}
                           </div>`;
  }
  document.getElementById("quiz-title-container-status").innerHTML = questionStatusHtml;
}

/**
 * Method used to check is all questions are answered and depending on the result stop quiz or show confirmation modal
 */
function checkQuizEnd() {
  if (questionsStatus.includes(STATUS_NO_ANSWER)) {
    showQuizEndModalDialog();
  } else {
    stopQuiz();
  }
}

/**
 * Method wrapper to display modal dialog with quiz end confirmation question
 */
function showQuizEndModalDialog() {
  document.getElementById("modal").className = "overlay open";
}

/**
 * Method wrapper to hide modal dialog with quiz end confirmation question
 */
function hideQuizEndModalDialog() {
  document.getElementById("modal").className = "overlay";
}

/**
 * Method used to stop quiz (via quiz API) with specified ID
 */
function stopQuiz() {
  hideQuizEndModalDialog();
  getQuizStop(quizID, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      displaySummary(data);
    }
  });
}

/**
 * Method used to display summary (hide question and show percentage correctness)
 *
 * @param {Float} summaryValue correct answers percentage
 */
function displaySummary(summaryValue) {
  document.getElementById("quiz-question-container").className = "container-visible";
  document.getElementById("quiz-modes-container").className = "container-hidden";
  document.getElementById("quiz-title-container-label").innerHTML = "results:";
  updateQuestionStatus(false);
  document.getElementById("quiz-question-container").innerHTML = getSummaryHtml(summaryValue);
}

/**
 * Method used to receive summary HTML code (percentage result and OK button)
 *
 * @param {Float} summaryValue correct answers percentage to be displayed in HTML
 * @returns HTML code for quiz summary
 */
function getSummaryHtml(summaryValue) {
  let summaryTitle = "RESULT";
  let summaryImage = "images/summary-medium.png";
  if (summaryValue >= 0.75) {
    summaryTitle = "AWESOME";
    summaryImage = "images/summary-good.png";
  } else if (summaryValue <= 0.25) {
    summaryTitle = "YOU CAN DO BETTER";
    summaryImage = "images/summary-bad.png";
  }
  return `<div id="quiz-summary">
            <img id="quiz-summary-img" src="${summaryImage}">
            <p id="quiz-summary-title"><u>${summaryTitle}: </u></p>
            <p><strong>${summaryValue * 100}%</strong> of correct answers.</p>
            <button id="end-summary" class="question-control-btn dynamic-border" onclick="cleanQuiz()">
              OK
            </button>
          </div>`;
}

/**
 * Method used to clean quiz summary and display initial quiz selector
 */
function cleanQuiz() {
  document.getElementById("quiz-question-container").className = "container-hidden";
  document.getElementById("quiz-modes-container").className = "container-visible";
  document.getElementById("quiz-title-container-status").innerHTML = "";
  document.getElementById("quiz-title-container-label").innerHTML = "select mode:";
  document.getElementById("quiz-question-container").innerHTML = "";
}
