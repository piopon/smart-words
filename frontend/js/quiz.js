const STATUS_NO_ANSWER = 0;
const STATUS_ANSWER_OK = 1;
const STATUS_ANSWER_NOK = -1;
const END_QUIZ_FINISH = 0;
const END_QUIZ_STOP = 1;
var quizID = undefined;
var endQuizReason = undefined;
var totalQuestionsNo = undefined;
var currentQuestionNo = undefined;

/**
 * Method used to receive number of question, start quiz and receive UUID
 */
function startQuiz() {
  totalQuestionsNo = document.getElementById("quiz-mode-settings-question-no").value;
  startQuizUpdateUI(STATE_QUIZ_LOAD);
  postQuizStart(totalQuestionsNo, (err, data) => {
    if (err) {
      startQuizUpdateUI(STATE_QUIZ_ERROR, err);
      console.log("ERROR: " + err);
    } else {
      startQuizUpdateUI(STATE_QUIZ_OK);
      quizID = data;
      currentQuestionNo = 0;
      requestQuestionNo(currentQuestionNo);
    }
  });
}

/**
 * Method (wrapper) used request a next question (relative to currently displayed one)
 */
function requestNextQuestion() {
  if (!verifyQuestionNo(++currentQuestionNo)) {
    questionViewUpdateUI(STATE_QUIZ_ERROR, 'next-question', `Invalid question number value [${number}]`);
  } else {
    requestQuestionNo(currentQuestionNo, 'next-question');
  }
}

/**
 * Method (wrapper) used request a previous question (relative to currently displayed one)
 */
function requestPrevQuestion() {
  if (!verifyQuestionNo(--currentQuestionNo)) {
    questionViewUpdateUI(STATE_QUIZ_ERROR, 'prev-question', `Invalid question number value [${number}]`);
  } else {
    requestQuestionNo(currentQuestionNo, 'prev-question');
  }
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
 * Method used to request a question with specified number
 *
 * @param {Integer} number of a requested question
 * @param {String} buttonId which button was pressed (next or previous, undefined by default)
 */
function requestQuestionNo(number, buttonId = undefined) {
  questionViewUpdateUI(STATE_QUIZ_LOAD, buttonId);
  getQuestionNo(quizID, number, (err, data) => {
    if (err) {
      questionViewUpdateUI(STATE_QUIZ_ERROR, buttonId);
      console.log("ERROR: " + err);
    } else {
      questionViewUpdateUI(STATE_QUIZ_OK, buttonId);
      currentQuestionNo = number;
      displayQuestion(data);
    }
  });
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
  let headersMap = new Map([[0, 'A'], [1, 'B'], [2, 'C'], [3, 'D']]);
  let isNewQuestion = null === question.correct;
  buttonAction = getAnswerButtonAction(isNewQuestion, optionNo);
  buttonClass = getAnswerButtonClass(isNewQuestion, optionNo == question.answer ? question.correct : null);
  return `<div id="question-option-${optionNo}">
            <div class="answer-header"><span>${headersMap.get(optionNo)}</span></div>
            <button id="answer-${optionNo}" class="${buttonClass}" onclick="${buttonAction}">
              ${question.options[optionNo]}
            </button>
          </div>`;
}

/**
 * Method used to receive answer button action for HTML code
 *
 * @param {Boolean} isNewQuestion flag indicating if this question is new or not
 * @param {Integer} optionNo number of option for which to update action
 * @returns answer option action (if new question is true), or empty (if new question is false)
 */
function getAnswerButtonAction(isNewQuestion, optionNo) {
  return isNewQuestion ? `answerQuestionNo('${currentQuestionNo}', '${optionNo}', 'question-option-${optionNo}')` : ``;
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
 * Method used to receive control buttons HTML code
 *
 * @returns HTML code for question control buttons (previous and next)
 */
function getControlButtonsHtml() {
  let placeholderForPrevBtn = getControlButtonHtml("prev-question", "PREVIOUS", "requestPrevQuestion()", "static-border");
  let placeholderForNextBtn = currentQuestionNo === totalQuestionsNo - 1
      ? getControlButtonHtml(getButtonIdFromEndReason(END_QUIZ_FINISH), "FINISH", `checkQuizEnd(${END_QUIZ_FINISH})`, "static-border")
      : getControlButtonHtml("next-question", "NEXT", "requestNextQuestion()", "static-border");
  let placeholderForStopBtn =
      getControlButtonHtml(getButtonIdFromEndReason(END_QUIZ_STOP), "STOP", `checkQuizEnd(${END_QUIZ_STOP})`, "dynamic-border");
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
  return `<div id="${id}-wrapper">
            <button id="${id}" class="question-control-btn ${borderType}" onclick="${action}">${text}</button>
            <div id="${id}-info" class="service-ok" title="PUT EXTRA INFO HERE"></div>
          </div>`;
}

/**
 * Method used to answer a specified question number with input answer number
 *
 * @param {Integer} number of a question to be answered (accepted values: 0 - totalQuestionsNo)
 * @param {Integer} answerNo number of answer for specified question (accepted values: 0-3)
 * @param {String} buttonId which button was pressed (answer button ID, undefined by default)
 */
function answerQuestionNo(number, answerNo, buttonId = undefined) {
  questionViewUpdateUI(STATE_QUIZ_LOAD, buttonId);
  postQuestionAnswer(quizID, number, answerNo, (err, data) => {
    if (err) {
      questionViewUpdateUI(STATE_QUIZ_ERROR, buttonId);
      console.log("ERROR: " + err);
    } else {
      questionsStatus[currentQuestionNo] = data === true ? STATUS_ANSWER_OK : STATUS_ANSWER_NOK;
      questionViewUpdateUI(STATE_QUIZ_OK, buttonId);
      for (let i in [0, 1, 2, 3]) {
        document.getElementById("answer-" + i).onclick = null;
        document.getElementById("answer-" + i).className = getAnswerButtonClass(false, answerNo === i ? data : null);
      }
    }
  });
}

/**
 * Method used to check is all questions are answered and depending on the result stop quiz or show confirmation modal
 *
 * @param {Integer} currentEndMode reson (FINISH or END) why we should check quiz end status
 */
function checkQuizEnd(currentEndMode) {
  endQuizReason = currentEndMode;
  if (quizHasUnansweredQuestions()) {
    showQuizEndModalDialog();
  } else {
    stopQuiz();
  }
}

/**
 * Method wrapper to display modal dialog with quiz end confirmation question
 */
function showQuizEndModalDialog() {
  document.getElementById("modal").className = "overlay open no-select";
}

/**
 * Method wrapper to hide modal dialog with quiz end confirmation question
 */
function hideQuizEndModalDialog() {
  document.getElementById("modal").className = "overlay no-select";
  endQuizReason = undefined;
}

/**
 * Method used to stop quiz (via quiz API) with specified ID
 */
function stopQuiz() {
  var endButtonId = getButtonIdFromEndReason(endQuizReason);
  hideQuizEndModalDialog();
  questionViewUpdateUI(STATE_QUIZ_LOAD, endButtonId);
  getQuizStop(quizID, (err, data) => {
    if (err) {
      questionViewUpdateUI(STATE_QUIZ_ERROR, endButtonId);
      console.log("ERROR: " + err);
    } else {
      questionViewUpdateUI(STATE_QUIZ_OK, endButtonId);
      displaySummary(data);
    }
  });
}

/**
 * Method used to map end reason state to correct button ID
 *
 * @param {Integer} endQuizReason state with appropriate end quiz value
 * @returns string containing button ID responsinble for specified end quiz reason
 */
function getButtonIdFromEndReason(endQuizReason) {
  var buttonId = undefined;
  if (END_QUIZ_FINISH === endQuizReason) {
    buttonId = 'finish-quiz';
  } else if(END_QUIZ_STOP === endQuizReason) {
    buttonId = 'stop-quiz';
  }
  return buttonId;
}

/**
 * Method used to display summary (hide question and show percentage correctness)
 *
 * @param {Float} summaryValue correct answers percentage
 */
function displaySummary(summaryValue) {
  questionViewUpdateUI(STATE_QUIZ_OFF);
  document.getElementById("quiz-question-container").className = "container-visible";
  document.getElementById("quiz-modes-container").className = "container-hidden";
  document.getElementById("quiz-title-container-label").innerHTML = "results:";
  document.getElementById("quiz-question-container").innerHTML = getSummaryHtml(summaryValue);
}

/**
 * Method used to receive summary HTML code (percentage result and OK button)
 *
 * @param {Float} summaryValue correct answers percentage to be displayed in HTML
 * @returns HTML code for quiz summary
 */
function getSummaryHtml(summaryValue) {
  let displayImage = getSummaryImage(summaryValue);
  let displayTitle = getSummaryTitle(summaryValue);
  let displayValue = summaryValue * 100;
  displayValue = +displayValue.toFixed(2);
  return `<div id="quiz-summary">
            <img id="quiz-summary-img" src="${displayImage}">
            <p id="quiz-summary-title"><u>${displayTitle}: </u></p>
            <p><strong>${displayValue}%</strong> of correct answers.</p>
            <button id="end-summary" class="question-control-btn dynamic-border" onclick="cleanQuiz()">
              OK
            </button>
          </div>`;
}

/**
 * Method used to receive summary title based on current score
 *
 * @param {Float} summaryValue correct answers percentage to determine the end title
 * @returns summary title string (PERFECT, AWESOME, RESULT, YOU CAN DO BETTER)
 */
function getSummaryTitle(summaryValue) {
  let summaryTitle = "RESULT";
  if (summaryValue === 1.0) {
    summaryTitle = "PERFECT";
  } else if (summaryValue >= 0.75) {
    summaryTitle = "AWESOME";
  } else if (summaryValue <= 0.25) {
    summaryTitle = "YOU CAN DO BETTER";
  }
  return summaryTitle;
}

/**
 * Method used to receive summary image based on current score
 *
 * @param {Float} summaryValue correct answers percentage to determine the end image
 * @returns summary image path
 */
function getSummaryImage(summaryValue) {
  let summaryImage = "images/summary-medium.png";
  if (summaryValue === 1.0) {
    summaryImage = "images/summary-100.png";
  } else if (summaryValue >= 0.75) {
    summaryImage = "images/summary-good.png";
  } else if (summaryValue <= 0.25) {
    summaryImage = "images/summary-bad.png";
  }
  return summaryImage;
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
