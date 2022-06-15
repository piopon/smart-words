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
      console.log(data);
      updatedAnswerClass = 'true' === data ? "question-option-btn-ok" : "question-option-btn-nok";
      document.getElementById("answer-" + answerNo).className = updatedAnswerClass;
    }
  });
}

/**
 * Method used to display a specified question number with its all four options
 *  
 * @param {Object} questionObject to be displayed (word + four options)
 */
function displayQuestion(questionObject) {
    document.getElementById("quiz-mode-form").className = "form-hidden";
    questionHtml = getWordHtml(questionObject.word);
    for (var optionNo = 0; optionNo < questionObject.options.length; optionNo++) {
      questionHtml += getOptionHtml(questionObject.options[optionNo], optionNo);
    }
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
 * @param {String} option description to be displayed 
 * @param {Integer} optionNo number of option
 * @returns HTML code with word option
 */
function getOptionHtml(option, optionNo) {
  buttonAction = `onclick="answerQuestionNo('${currentQuestionNo}', '${optionNo}')"`;
  return `<div class="question-option-div">
            <button id="answer-${optionNo}" class="question-option-btn" ${buttonAction}>
              ${optionNo}) ${option}
            </button>
          </div>`;
}

/**
 * Method used to receive control buttons HTML code
 * 
 * @returns HTML code for question control buttons (previous and next)
 */
function getControlButtonsHtml() {
  return `<div id="question-control" class="question-control-div">
            <button id="prev-question" class="prev-question-btn" onclick="requestPrevQuestion()">
              PREVIOUS
            </button>
            <button id="next-question" class="next-question-btn" onclick="requestNextQuestion()">
              NEXT
            </button>
          </div>`;
}
