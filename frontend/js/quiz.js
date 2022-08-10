var quizID = undefined;
var totalQuestionsNo = undefined;
var currentQuestionNo = undefined;

/**
 * Method used to receive number of question, start quiz and receive UUID
 */
function startQuiz() {
  totalQuestionsNo = document.getElementById("quiz-mode-question-no").value;
  postQuizStart(totalQuestionsNo, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      console.log(data);
      quizID = data;
      currentQuestionNo = 0;
      requestQuestionNo(currentQuestionNo);
    }
  });
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
  document.getElementById("select-mode-title").innerHTML = `quiz - guess definition - ${questionStatus}:`;
  document.getElementById("quiz-question").className = "container-visible";
  document.getElementById("quiz-mode-container").className = "container-hidden";
  questionHtml = getWordHtml(questionObject.word);
  questionHtml += `<div id="question-options">`;
  for (var optionNo = 0; optionNo < questionObject.options.length; optionNo++) {
    questionHtml += getOptionHtml(questionObject, optionNo);
  }
  questionHtml += `</div>`;
  questionHtml += getControlButtonsHtml();
  document.getElementById("quiz-question").innerHTML = questionHtml;
  document.getElementById("prev-question").disabled = currentQuestionNo <= 0;
  document.getElementById("next-question").disabled = currentQuestionNo >= totalQuestionsNo - 1;
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
  buttonAction = getAnswerButtonAction(null === question.correct, optionNo);
  buttonClass = getAnswerButtonClass(optionNo == question.answer ? question.correct : null);
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
  return `<div id="question-control">
            <button id="prev-question" onclick="requestPrevQuestion()">
              PREVIOUS
            </button>
            <button id="next-question" onclick="requestNextQuestion()">
              NEXT
            </button>
            <button id="stop-quiz" onclick="stopQuiz()">
              STOP
            </button>
          </div>`;
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
      for (let i in [0, 1, 2, 3]) {
        document.getElementById("answer-" + i).onclick = null;
        document.getElementById("answer-" + i).className =
          answerNo === i ? getAnswerButtonClass(data) : "question-option-btn-disabled";
      }
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
function getAnswerButtonClass(isAnswerCorrect) {
  if (null === isAnswerCorrect) {
    return "question-option-btn";
  }
  return isAnswerCorrect ? "question-option-btn-ok" : "question-option-btn-nok";
}

/**
 * Method used to stop quiz (via quiz API) with specified ID
 */
function stopQuiz() {
  getQuizStop(quizID, (err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      console.log(data);
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
  document.getElementById("quiz-question").className = "container-visible";
  document.getElementById("quiz-mode-container").className = "container-hidden";
  document.getElementById("select-mode-title").innerHTML = "results:";
  document.getElementById("quiz-question").innerHTML = getSummaryHtml(summaryValue);
}

/**
 * Method used to receive summary HTML code (percentage result and OK button)
 *
 * @param {Float} summaryValue correct answers percentage to be displayed in HTML
 * @returns HTML code for quiz summary
 */
function getSummaryHtml(summaryValue) {
  return `<div id="quiz-summary" class="quiz-summary-div">
            <p><u>RESULT:</u><br><strong>${summaryValue * 100}%</strong> of correct answers.</p>
            <button id="end-summary" class="end-summary-btn" onclick="cleanQuiz()">
              OK
            </button>
          </div>`;
}

/**
 * Method used to clean quiz summary and display initial quiz selector
 */
function cleanQuiz() {
  document.getElementById("quiz-question").className = "container-hidden";
  document.getElementById("quiz-mode-container").className = "container-visible";
  document.getElementById("select-mode-title").innerHTML = "select mode:";
  document.getElementById("quiz-question").innerHTML = "";
}
