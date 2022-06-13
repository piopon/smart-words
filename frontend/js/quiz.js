var quizID = undefined;
var totalQuestionsNo = undefined;
var currentQuestionNo = undefined;

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

function requestNextQuestion() {
  if (!verifyQuestionNo(++currentQuestionNo)) {
    console.log("Invalud question number value [" + number + "]");
  }
  requestQuestionNo(currentQuestionNo);
}

function requestPrevQuestion() {
  if (!verifyQuestionNo(--currentQuestionNo)) {
    console.log("Invalud question number value [" + number + "]");
  }
  requestQuestionNo(currentQuestionNo);
}

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

function verifyQuestionNo(number) {
  if (number === undefined) return false;
  if (number < 0 || number > totalQuestionsNo) {
    return false;
  }
  return true;
}

function answerQuestionNo(number, answerNo) {
  console.log(`question no: ${number} -> answer no: ${answerNo}`);
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

function getWordHtml(word) {
  return `<div id="question-word" class="question-word-div">${word}</div>`;
}

function getOptionHtml(option, optionNo) {
  buttonAction = `onclick="answerQuestionNo('${currentQuestionNo}', '${optionNo}')"`;
  return `<div class="question-option-div">
            <button id="answer-${optionNo}" class="question-option-btn" ${buttonAction}>
              ${optionNo}) ${option}
            </button>
          </div>`;
}

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
