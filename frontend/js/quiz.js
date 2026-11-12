const STATUS_NO_ANSWER = 0;
const STATUS_ANSWER_OK = 1;
const STATUS_ANSWER_NOK = -1;
const END_QUIZ_FINISH = 0;
const END_QUIZ_STOP = 1;
var quizUuid = undefined;
var selectedMode = undefined;
var endQuizReason = undefined;
var totalQuestionsNo = undefined;
var currentQuestionNo = undefined;
var currentSummary = undefined;
var leaderboardEntries = [];
var leaderboardSubmitted = false;
const LEADERBOARD_GAME = "quiz";
const LEADERBOARD_LIMIT = 10;

/**
 * Method used to receive number of question, start quiz and receive UUID
 *
 * @param {Integer} modeId unique identifier of quiz mode to be started
 */
function startQuiz(modeId) {
  initializeQuizSettings(modeId);
  startQuizUpdateUI(selectedMode.id, STATE_QUIZ_LOAD);
  postQuizStart(totalQuestionsNo, selectedMode.id, selectedMode.language, (err, data) => {
    if (err) {
      startQuizUpdateUI(selectedMode.id, STATE_QUIZ_ERROR, err);
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      startQuizUpdateUI(selectedMode.id, STATE_QUIZ_OK);
      quizUuid = data;
      currentQuestionNo = 0;
      requestQuestionNo(currentQuestionNo);
    }
  });
}

/**
 * Method used to initialize all variables after successful quiz start and before updating UI
 *
 * @param {Integer} modeId unique identifier of quiz mode for which we want to initialize settings
 */
function initializeQuizSettings(modeId) {
  selectedMode = availableModes.find(mode => mode.id === modeId);
  selectedMode.language = modeLanguageMap.get(modeId);
  totalQuestionsNo = document.getElementById(`quiz-mode-${modeId}-settings-question-no`).value;
}

/**
 * Method (wrapper) used request a next question (relative to currently displayed one)
 */
function requestNextQuestion() {
  if (!verifyQuestionNo(++currentQuestionNo)) {
    questionViewUpdateUI(STATE_QUIZ_ERROR, 'next-question', createErrorObject(`Invalid question number value [${number}]`, 500));
  } else {
    requestQuestionNo(currentQuestionNo, 'next-question');
  }
}

/**
 * Method (wrapper) used request a previous question (relative to currently displayed one)
 */
function requestPrevQuestion() {
  if (!verifyQuestionNo(--currentQuestionNo)) {
    questionViewUpdateUI(STATE_QUIZ_ERROR, 'prev-question', createErrorObject(`Invalid question number value [${number}]`, 500));
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
  getQuestionNo(quizUuid, number, (err, data) => {
    if (err) {
      questionViewUpdateUI(STATE_QUIZ_ERROR, buttonId);
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      questionViewUpdateUI(STATE_QUIZ_OK, buttonId);
      currentQuestionNo = number;
      displayQuizQuestion(data);
    }
  });
}

/**
 * Method used to display a specified quiz question data view (name, all four options, and control buttons)
 *
 * @param {Object} questionObject to be displayed (word + four options)
 */
function displayQuizQuestion(questionObject) {
  let questionStatus = `question ${currentQuestionNo + 1}/${totalQuestionsNo}`;
  document.getElementById("quiz-title-container-label").innerHTML = `quiz - ${selectedMode.name} - ${questionStatus}:`;
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
  return `<div id="question-word" class="question-word-div label-enabled">${word}</div>`;
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
            <div id="question-header-${optionNo}" class="answer-header-enabled">
              <span>${headersMap.get(optionNo)}</span>
              <div id="question-info-${optionNo}" class="service-ok" title="PUT EXTRA INFO HERE"></div>
            </div>
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
 * @param {Boolean} isNewQuestion flag indicating if this question is a new (unanswered) question
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
  postQuestionAnswer(quizUuid, number, answerNo, (err, data) => {
    if (err) {
      questionViewUpdateUI(STATE_QUIZ_ERROR, buttonId);
      console.log("ERROR " + err.status + ": " + err.message);
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
  getQuizStopDetails(quizUuid, (err, data) => {
    if (err) {
      // Keep compatibility with old quiz backend response shape.
      getQuizStop(quizUuid, (fallbackErr, fallbackData) => {
        if (fallbackErr) {
          questionViewUpdateUI(STATE_QUIZ_ERROR, endButtonId);
          console.log("ERROR " + fallbackErr.status + ": " + fallbackErr.message);
          return;
        }
        questionViewUpdateUI(STATE_QUIZ_OK, endButtonId);
        displayQuizSummary(normalizeSummaryDetails(fallbackData));
      });
    } else {
      questionViewUpdateUI(STATE_QUIZ_OK, endButtonId);
      displayQuizSummary(normalizeSummaryDetails(data));
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
 * Method used to display quiz summary view (hide question and show percentage correctness)
 *
 * @param {Object} summaryValue object with quiz summary details
 */
function displayQuizSummary(summaryValue) {
  questionViewUpdateUI(STATE_QUIZ_OFF);
  currentSummary = summaryValue;
  leaderboardEntries = [];
  leaderboardSubmitted = false;
  document.getElementById("quiz-question-container").className = "container-visible";
  document.getElementById("quiz-modes-container").className = "container-hidden";
  document.getElementById("quiz-title-container-label").innerHTML = "results:";
  document.getElementById("quiz-question-container").innerHTML = getSummaryHtml(summaryValue, [], false,
      "loading leaderboard...");
  loadSummaryLeaderboard();
}

/**
 * Method used to receive summary HTML code (percentage result and OK button)
 *
 * @param {Object} summaryData complete summary details object
 * @param {Array} topEntries top leaderboard entries for selected mode
 * @param {Boolean} canSubmit indicates if score can be submitted to leaderboard
 * @param {String} message leaderboard information message
 * @returns HTML code for quiz summary
 */
function getSummaryHtml(summaryData, topEntries, canSubmit, message) {
  let displayImage = getSummaryImage(summaryData.scorePercent);
  let displayTitle = getSummaryTitle(summaryData.scorePercent);
  let displayValue = summaryData.scorePercent * 100;
  displayValue = +displayValue.toFixed(2);
  let submitSection = canSubmit ? getLeaderboardSubmitHtml() : "";
  let leaderboardSection = getLeaderboardHtml(topEntries);
  return `<div id="quiz-summary">
            <img id="quiz-summary-img" src="${displayImage}">
            <p id="quiz-summary-title"><u>${displayTitle}: </u></p>
            <p><strong>${displayValue}%</strong> of correct answers.</p>
            <p id="quiz-summary-extra">${summaryData.correctAnswers}/${summaryData.totalQuestions} correct answers.</p>
            <p id="leaderboard-message">${message}</p>
            ${submitSection}
            ${leaderboardSection}
            <button id="end-summary" class="question-control-btn dynamic-border" onclick="cleanQuiz()">
              OK
            </button>
          </div>`;
}

function loadSummaryLeaderboard() {
  getLeaderboardEntries(LEADERBOARD_GAME, currentSummary.mode, LEADERBOARD_LIMIT, (err, data) => {
    if (err) {
      document.getElementById("quiz-question-container").innerHTML = getSummaryHtml(
        currentSummary,
        [],
        false,
        "leaderboard is currently unavailable"
      );
      return;
    }
    leaderboardEntries = data;
    let canSubmit = isTopLeaderboardResult(currentSummary, leaderboardEntries);
    let message = canSubmit
      ? "you reached top 10 - enter your name"
      : "you did not reach top 10 in this mode";
    if (leaderboardSubmitted) {
      message = "score saved to leaderboard";
      canSubmit = false;
    }
    document.getElementById("quiz-question-container").innerHTML = getSummaryHtml(
      currentSummary,
      leaderboardEntries,
      canSubmit,
      message
    );
  });
}

function isTopLeaderboardResult(summaryData, topEntries) {
  if (!topEntries || topEntries.length < LEADERBOARD_LIMIT) {
    return true;
  }
  const lastEntry = topEntries[topEntries.length - 1];
  if (summaryData.scorePercent > lastEntry.scorePercent) {
    return true;
  }
  if (summaryData.scorePercent === lastEntry.scorePercent && summaryData.totalQuestions > lastEntry.totalQuestions) {
    return true;
  }
  return false;
}

function submitSummaryToLeaderboard() {
  if (leaderboardSubmitted) {
    return;
  }
  const usernameInput = document.getElementById("leaderboard-username");
  if (!usernameInput) {
    return;
  }
  const username = usernameInput.value.trim();
  const submitButton = document.getElementById("leaderboard-submit");
  if (submitButton) {
    submitButton.disabled = true;
    submitButton.innerHTML = "saving...";
  }

  const payload = {
    game: LEADERBOARD_GAME,
    mode: currentSummary.mode,
    username: username,
    scorePercent: currentSummary.scorePercent,
    totalQuestions: currentSummary.totalQuestions,
    correctAnswers: currentSummary.correctAnswers
  };
  postLeaderboardEntry(payload, (err, _data) => {
    if (err) {
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.innerHTML = "SAVE SCORE";
      }
      return;
    }
    loadSummaryLeaderboard();
  });
}

/**
 * Method used to receive leaderboard submit section HTML.
 */
function getLeaderboardSubmitHtml() {
  return `<div id="leaderboard-submit-wrapper">
            <label for="leaderboard-username">name:</label>
            <input id="leaderboard-username" type="text" maxlength="24" placeholder="enter your name" />
            <button id="leaderboard-submit" class="question-control-btn static-border" onclick="submitSummaryToLeaderboard()">
              SAVE SCORE
            </button>
            <p id="leaderboard-submit-info"></p>
          </div>`;
}

/**
 * Method used to receive leaderboard section HTML.
 */
function getLeaderboardHtml(topEntries) {
  let rowsHtml = "";
  if (!topEntries || topEntries.length === 0) {
    rowsHtml = `<tr><td colspan="4">no scores yet</td></tr>`;
  } else {
    rowsHtml = topEntries.map((entry, index) => {
      const score = +(entry.scorePercent * 100).toFixed(2);
      return `<tr>
                <td>${index + 1}</td>
                <td>${entry.username}</td>
                <td>${score}%</td>
                <td>${entry.totalQuestions}</td>
              </tr>`;
    }).join("");
  }

  return `<div id="leaderboard-wrapper">
            <p id="leaderboard-title">top 10 leaderboard</p>
            <table id="leaderboard-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>name</th>
                  <th>score</th>
                  <th>questions</th>
                </tr>
              </thead>
              <tbody>
                ${rowsHtml}
              </tbody>
            </table>
          </div>`;
}

/**
 * Method used to normalize summary object in compatibility mode.
 */
function normalizeSummaryDetails(summaryValue) {
  if (typeof summaryValue === "object" && summaryValue !== null) {
    return summaryValue;
  }
  const normalizedPercent = parseFloat(summaryValue);
  const parsedQuestions = parseInt(totalQuestionsNo);
  return {
    scorePercent: isNaN(normalizedPercent) ? 0 : normalizedPercent,
    correctAnswers: isNaN(normalizedPercent) || isNaN(parsedQuestions) ? 0 : Math.round(normalizedPercent * parsedQuestions),
    totalQuestions: isNaN(parsedQuestions) ? 0 : parsedQuestions,
    mode: selectedMode ? selectedMode.id : 0
  };
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
  let summaryImage = "images/quiz-summary/050-medium.png";
  if (summaryValue === 1.0) {
    summaryImage = "images/quiz-summary/100-perfect.png";
  } else if (summaryValue >= 0.75) {
    summaryImage = "images/quiz-summary/075-good.png";
  } else if (summaryValue <= 0.25) {
    summaryImage = "images/quiz-summary/025-bad.png";
  }
  return summaryImage;
}

/**
 * Method used to clean quiz summary and display initial quiz modes selector
 */
function cleanQuiz() {
  currentSummary = undefined;
  leaderboardEntries = [];
  leaderboardSubmitted = false;
  displayQuizModes();
  document.getElementById("quiz-question-container").className = "container-hidden";
  document.getElementById("quiz-modes-container").className = "container-visible";
  document.getElementById("quiz-title-container-status").innerHTML = "";
  document.getElementById("quiz-title-container-label").innerHTML = "select mode:";
  document.getElementById("quiz-question-container").innerHTML = "";
}