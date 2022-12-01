/**
 * Method used to show quiz modes
 */
function showQuizModes() {
  getQuizModes((err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      let quizModeContainer = document.getElementById("quiz-modes-container");
      if (quizModeContainer === null) return;
      quizModeContainer.innerHTML = Object.values(data)
        .map((item) => getModeHtml(item))
        .join("");
    }
  });
}

function getModeHtml(mode) {
  return `<div id="quiz-mode-tile" class="tile-size-big tile-color-primary">
            ${getTitleHtml(mode.name)}
            <div id="quiz-mode-tile-content">
              ${getDescriptionHtml(mode.description)}
              <div id="quiz-mode-content-settings">
                ${getSettingsHtml(mode.settings)}
              </div>
            </div>
            ${getControlsHtml()}
          </div>`;
}

function getTitleHtml(title) {
  return `<div id="quiz-mode-tile-title">${title}</div>`;
}

function getDescriptionHtml(description) {
  return `<div id="quiz-mode-content-description">
            <p class="mode-section-label">decription:</p>
            ${description}
          </div>`;
}

function getSettingsHtml(settings) {
  let allSettingsHtml = Object.values(settings)
    .map((item) => getSettingHtml(item))
    .join("");
  return `<p class="mode-section-label">settings:</p>` + allSettingsHtml;
}

function getSettingHtml(setting) {
  switch (setting.type) {
    case "languages":
      return getLanguagesHtml(setting.label, setting.details);
    case "questions":
      return getQuestionsHtml(setting.label, setting.details);
    default:
      return "";
  }
}

function getLanguagesHtml(label, details) {
  let supportedLanguages = details
    .split(" ")
    .map((item) => getLanguageHtml(item))
    .join("");
  return `<div class="quiz-mode-setting-container">
            <label>${label}</label>
            ${supportedLanguages}
          </div>`;
}

function getLanguageHtml(language) {
  return `<img class="language-flag" src="images/languages/${language}-24.png"/>`;
}

function getQuestionsHtml(label, details) {
  return `<div class="quiz-mode-setting-container">
            <label for="quiz-mode-settings-question-no">${label}</label>
            <input type="number" id="quiz-mode-settings-question-no" ${details} />
          </div>`;
}

function getControlsHtml() {
  return `<div id="quiz-mode-tile-controls">
            <button id="quiz-mode-controls-start" class="dynamic-border" onclick="startQuiz()">start</button>
            <div id="quiz-mode-controls-info" class="hide" title="PUT EXTRA INFO HERE"></div>
          </div>`;
}

showQuizModes();
