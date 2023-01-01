const STATE_QUIZ_OK = 0;
const STATE_QUIZ_OFF = 1;
const STATE_QUIZ_LOAD = 2;
const STATE_QUIZ_ERROR = 3;
var questionsStatus = undefined;

/**
 * Method used to update GUI state while starting quiz from service
 *
 * @param {Integer} newUiState current loading state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {Object} detailedState containing detailed information about current state (undefined by default)
 */
function startQuizUpdateUI(quizModeId, newUiState, detailedState = undefined) {
  let startQuizBtn = document.getElementById(`quiz-mode-${quizModeId}-controls-start`);
  let startQuizInfo = document.getElementById("quiz-mode-controls-info");
  if (startQuizBtn === null || startQuizInfo === null) return;
  if (STATE_QUIZ_OK === newUiState) {
    startQuizBtn.addEventListener("click", startQuiz);
    startQuizBtn.className = "dynamic-border";
    startQuizBtn.disabled = false;
    startQuizBtn.innerHTML = "start";
    startQuizInfo.className = "hide";
    return;
  }
  if (STATE_QUIZ_LOAD === newUiState) {
    startQuizBtn.onclick = null;
    startQuizBtn.className = "loading";
    startQuizBtn.disabled = false;
    startQuizBtn.innerHTML = "connecting...";
    startQuizInfo.className = "hide";
    questionsStatus = Array(parseInt(totalQuestionsNo)).fill(STATUS_NO_ANSWER);
    return;
  }
  if (STATE_QUIZ_ERROR === newUiState) {
    startQuizBtn.onclick = null;
    startQuizBtn.disabled = true;
    startQuizBtn.innerHTML = "service unavailable";
    startQuizInfo.className = "";
    startQuizInfo.title = getQuizErrorMessage(detailedState);
    questionsStatus = undefined;
    return;
  }
  if (STATE_QUIZ_OFF === newUiState) {
    startQuizBtn.onclick = null;
    startQuizBtn.disabled = true;
    startQuizBtn.innerHTML = "service disabled";
    startQuizInfo.className = "";
    startQuizInfo.title = "Logic entered quiz off state. Reload page and try again.";
    questionsStatus = undefined;
    return;
  }
}

/**
 * Method used to receive concrete error message for user depending on source message
 *
 * @param {Object} sourceState containing error state information (message and error code) from API
 * @returns quiz error message string
 */
function getQuizErrorMessage(sourceState) {
  let message = "Cannot connect to a quiz backend service!\n" +
                "Please verify its running and connection status and refresh this page.";
  if (sourceState) {
    if (503 === sourceState.status) {
      message = "Quiz backend service cannot connect to word service!\n" +
                "Please verify word service running status and refresh this page.";
    } else if (400 === sourceState.status) {
      message = sourceState.message;
    }
  }
  return message;
}

/**
 * Method used to update UI state when changing quiz view and communicating with quiz service
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} pressedButtonId which button was pressed (and triggered update UI, undefined by default)
 * @param {Object} detailedState containing information about current state (undefined by default)
 */
function questionViewUpdateUI(newUiState, pressedButtonId = undefined, detailedState = undefined) {
  questionControlUpdateUI(newUiState, pressedButtonId, detailedState);
  questionAnswersUpdateUI(newUiState, pressedButtonId, detailedState);
  questionStatusUpdateUI(newUiState, pressedButtonId, detailedState);
  questionLabelsUpdateUI(newUiState);
}

/**
 * Method used to update UI state of quiz label when changing quiz view and communicating with quiz service
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 */
function questionLabelsUpdateUI(newUiState) {
  let modeTitleLabel = document.getElementById("quiz-title-container-label");
  let wordNameLabel = document.getElementById("question-word");
  let navStatusLabel = document.getElementById("status-title-label");
  if (modeTitleLabel === null || wordNameLabel === null || navStatusLabel === null) return;
  if (STATE_QUIZ_OK === newUiState) {
    modeTitleLabel.classList.remove("label-disabled");
    navStatusLabel.classList.remove("label-disabled");
    wordNameLabel.classList.remove("label-disabled");
    modeTitleLabel.classList.add("label-enabled");
    navStatusLabel.classList.add("label-enabled");
    wordNameLabel.classList.add("label-enabled");
    return;
  }
  if (STATE_QUIZ_LOAD === newUiState) {
    modeTitleLabel.classList.remove("label-disabled");
    navStatusLabel.classList.remove("label-disabled");
    wordNameLabel.classList.remove("label-disabled");
    modeTitleLabel.classList.add("label-enabled");
    navStatusLabel.classList.add("label-enabled");
    wordNameLabel.classList.add("label-enabled");
    return;
  }
  if (STATE_QUIZ_ERROR === newUiState) {
    modeTitleLabel.classList.remove("label-enabled");
    navStatusLabel.classList.remove("label-enabled");
    wordNameLabel.classList.remove("label-enabled");
    modeTitleLabel.classList.add("label-disabled");
    navStatusLabel.classList.add("label-disabled");
    wordNameLabel.classList.add("label-disabled");
    return;
  }
  if (STATE_QUIZ_OFF === newUiState) {
    modeTitleLabel.classList.remove("label-disabled");
    navStatusLabel.classList.remove("label-disabled");
    wordNameLabel.classList.remove("label-disabled");
    modeTitleLabel.classList.add("label-enabled");
    navStatusLabel.classList.add("label-enabled");
    wordNameLabel.classList.add("label-enabled");
    return;
  }
}

/**
 * Method used to update UI state of question control buttons
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} pressedButtonId which button was pressed (next or previous, undefined by default)
 * @param {Object} detailedState containing information about current state (undefined by default)
 */
function questionControlUpdateUI(newUiState, pressedButtonId = undefined, detailedState = undefined) {
  let stopBtn = document.getElementById("stop-quiz");
  let prevBtn = document.getElementById("prev-question");
  let nextBtn = document.getElementById("next-question") !== null
      ? document.getElementById("next-question")
      : document.getElementById("finish-quiz");
  let activeBtnInfo = pressedButtonId ? document.getElementById(`${pressedButtonId}-info`) : undefined;
  if (stopBtn === null || prevBtn === null || nextBtn === null) return;
  if (STATE_QUIZ_OK === newUiState) {
    if (activeBtnInfo && activeBtnInfo !== null) {
      activeBtnInfo.classList.add("service-ok");
      activeBtnInfo.classList.remove("service-wait");
      activeBtnInfo.classList.remove("service-error");
    }
    return;
  }
  if (STATE_QUIZ_LOAD === newUiState) {
    if (activeBtnInfo && activeBtnInfo !== null) {
      activeBtnInfo.classList.remove("service-ok");
      activeBtnInfo.classList.add("service-wait");
      activeBtnInfo.classList.remove("service-error");
    }
    return;
  }
  if (STATE_QUIZ_ERROR === newUiState || STATE_QUIZ_OFF === newUiState) {
    if (activeBtnInfo && activeBtnInfo !== null) {
      activeBtnInfo.classList.remove("service-ok");
      activeBtnInfo.classList.remove("service-wait");
      activeBtnInfo.classList.add("service-error");
      activeBtnInfo.title = getQuizErrorMessage(detailedState);
    }
    stopBtn.disabled = true;
    prevBtn.disabled = true;
    nextBtn.disabled = true;
    return;
  }
}

/**
 * Method used to update UI state of question answer buttons
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} pressedButtonId which button was pressed (next or previous, undefined by default)
 * @param {Object} detailedState containing information about current state (undefined by default)
 */
function questionAnswersUpdateUI(newUiState, pressedButtonId = undefined, detailedState = undefined) {
  let okGroupId = "question-option-";
  let pressedAnswerNo = pressedButtonId && pressedButtonId.startsWith(okGroupId)
      ? pressedButtonId.split(okGroupId)[1]
      : undefined;
  for (let answerNo in [0, 1, 2, 3]) {
    let answerButton = document.getElementById("answer-" + answerNo);
    let answerHeader = document.getElementById("question-header-" + answerNo);
    let answerInfo = pressedAnswerNo && pressedAnswerNo === answerNo
        ? document.getElementById("question-info-" + pressedAnswerNo)
        : undefined;
    if (answerButton === null || answerHeader === null) return;
    if (STATE_QUIZ_OK === newUiState) {
      answerButton.disabled = false;
      answerHeader.className = "answer-header-enabled";
      if (answerInfo) {
        answerInfo.className = "service-ok";
      }
      continue;
    }
    if (STATE_QUIZ_LOAD === newUiState) {
      answerButton.disabled = false;
      answerHeader.className = "answer-header-enabled";
      if (answerInfo) {
        answerInfo.className = "service-wait";
      }
      continue;
    }
    if (STATE_QUIZ_ERROR === newUiState || STATE_QUIZ_OFF === newUiState) {
      answerButton.disabled = true;
      answerHeader.className = "answer-header-disabled";
      if (answerInfo) {
        answerInfo.className = "service-error";
        answerInfo.title = getQuizErrorMessage(detailedState);
      }
      continue;
    }
  }
}

/**
 * Method used to update question navigation depending on current questionStatus and new UI state variable
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} buttonId which button was pressed (next or previous, undefined by default)
 * @param {Object} detailedState containing information about current state (undefined by default)
 */
function questionStatusUpdateUI(newUiState, buttonId = undefined, detailedState = undefined) {
  let idPrefix = "nav-";
  let questionStatusHtml = `<span id="status-title-label" class="label-enabled">quiz questions:</span>`;
  for (let i = 0; i < questionsStatus.length; i++) {
    let clickId = idPrefix + i;
    let clickAction = STATE_QUIZ_OK === newUiState ? `onclick="requestQuestionNo(${i}, '${clickId}')"` : ``;
    let clickClass = STATE_QUIZ_ERROR === newUiState
        ? "nav-disabled"
        : STATE_QUIZ_LOAD === newUiState || STATE_QUIZ_OFF === newUiState ? "nav-off" : "nav-on"
    let infoMessage = STATE_QUIZ_ERROR === newUiState ? getQuizErrorMessage(detailedState) : "";
    let infoClass = buttonId && buttonId.startsWith(idPrefix) && i === parseInt(buttonId.substring(idPrefix.length))
        ? STATE_QUIZ_ERROR === newUiState ? "nav-error" : STATE_QUIZ_LOAD === newUiState ? "nav-wait" : "nav-ok"
        : "nav-ok";
    questionStatusHtml += `<div id="${clickId}" class="question-status${questionsStatus[i]} ${clickClass}" ${clickAction}>
                             <div id="nav-info" class="${infoClass}" title="${infoMessage}"></div>
                             ${i + 1}
                           </div>`;
  }
  document.getElementById("quiz-title-container-status").innerHTML = questionStatusHtml;
}

/**
 * Method used to check if current quiz has unanswered questions
 *
 * @returns true if there are question without posted answer, false otherwise
 */
function quizHasUnansweredQuestions() {
  return questionsStatus.includes(STATUS_NO_ANSWER);
}
