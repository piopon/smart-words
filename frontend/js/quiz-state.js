const STATE_QUIZ_OK = 0;
const STATE_QUIZ_OFF = 1;
const STATE_QUIZ_LOAD = 2;
const STATE_QUIZ_ERROR = 3;
var questionsStatus = undefined;

/**
 * Method used to update GUI state while starting quiz from service
 *
 * @param {Integer} newUiState current loading state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} detailedMessage containing detailed information about current state (undefined by default)
 */
function startQuizUpdateUI(newUiState, detailedMessage = undefined) {
  let startQuizBtn = document.getElementById("quiz-mode-controls-start");
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
    startQuizInfo.title = getQuizErrorMessage(detailedMessage);
    questionsStatus = undefined;
    return;
  }
}

/**
 * Method used to receive concrete error message for user depending on source message
 *
 * @param {String} sourceMessage containing error message from API
 * @returns quiz error message string
 */
function getQuizErrorMessage(sourceMessage) {
  let message = "Cannot connect to a quiz backend service!\n" +
                "Please verify its running and connection status and refresh this page.";
  if (sourceMessage) {
    let findCodeValue = /\[([^\]]+)]/.exec(sourceMessage);
    if (findCodeValue && !isNaN(findCodeValue[1])) {
      let errorCode = parseInt(findCodeValue[1]);
      if (503 === errorCode) {
        message = "Quiz backend service cannot connect to word service!\n" +
                  "Please verify word service running status and refresh this page.";
      }
    } else {
      message = sourceMessage;
    }
  }
  return message;
}

/**
 * Method used to update UI state when changing quiz view and communicating with quiz service
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} pressedButtonId which button was pressed (and triggered update UI, undefined by default)
 * @param {String} displayMessage containing information about current state (undefined by default)
 */
function questionViewUpdateUI(newUiState, pressedButtonId = undefined, displayMessage = undefined) {
  questionControlUpdateUI(newUiState, pressedButtonId, displayMessage);
  questionAnswersUpdateUI(newUiState, pressedButtonId, displayMessage);
  questionStatusUpdateUI(newUiState, pressedButtonId, displayMessage);
}

/**
 * Method used to update UI state of question control buttons
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} pressedButtonId which button was pressed (next or previous, undefined by default)
 * @param {String} displayMessage containing information about current state (undefined by default)
 */
function questionControlUpdateUI(newUiState, pressedButtonId = undefined, displayMessage = undefined) {
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
  if (STATE_QUIZ_ERROR === newUiState) {
    if (activeBtnInfo && activeBtnInfo !== null) {
      activeBtnInfo.classList.remove("service-ok");
      activeBtnInfo.classList.remove("service-wait");
      activeBtnInfo.classList.add("service-error");
      activeBtnInfo.title = getQuizErrorMessage(displayMessage);
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
 * @param {String} displayMessage containing information about current state (undefined by default)
 */
function questionAnswersUpdateUI(newUiState, pressedButtonId = undefined, displayMessage = undefined) {
  let okGroupId = "question-option-";
  let answerDiv = pressedButtonId && pressedButtonId.startsWith(okGroupId)
      ? document.getElementById(pressedButtonId)
      : undefined;
  let answerButton = answerDiv ? answerDiv.lastChild : undefined;
  let answerHeader = answerDiv
      ? document.getElementById("question-info-" + pressedButtonId.split(okGroupId)[1])
      : undefined;
  if (STATE_QUIZ_OK === newUiState) {
    return;
  }
  if (STATE_QUIZ_LOAD === newUiState) {
    return;
  }
  if (STATE_QUIZ_ERROR === newUiState) {
    return;
  }
}

/**
 * Method used to update question navigation depending on current questionStatus and new UI state variable
 *
 * @param {Integer} newUiState current view state (from: STATE_QUIZ_OK, STATE_QUIZ_LOAD, STATE_QUIZ_ERROR)
 * @param {String} buttonId which button was pressed (next or previous, undefined by default)
 * @param {String} displayMessage containing information about current state (undefined by default)
 */
function questionStatusUpdateUI(newUiState, buttonId = undefined, displayMessage = undefined) {
  let idPrefix = "nav-";
  let questionStatusHtml = `quiz questions:`;
  for (let i = 0; i < questionsStatus.length; i++) {
    let clickId = idPrefix + i;
    let clickAction = STATE_QUIZ_OK === newUiState ? `onclick="requestQuestionNo(${i}, '${clickId}')"` : ``;
    let clickClass = STATE_QUIZ_ERROR === newUiState
        ? "nav-disabled"
        : STATE_QUIZ_LOAD === newUiState || STATE_QUIZ_OFF === newUiState ? "nav-off" : "nav-on"
    let infoMessage = STATE_QUIZ_ERROR === newUiState ? getQuizErrorMessage(displayMessage) : "";
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
