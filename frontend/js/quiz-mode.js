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

/**
 * Method used to receive complete single quiz mode HTML code
 *
 * @param {Object} mode containig all data needed to display quiz mode
 * @returns HTML content with single quiz mode
 */
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

/**
 * Method used to receive mode title HTML code
 *
 * @param {String} title of current quiz mode
 * @returns HTML content with mode title
 */
function getTitleHtml(title) {
  return `<div id="quiz-mode-tile-title">${title}</div>`;
}

/**
 * Method used to receive mode description HTML code
 *
 * @param {String} description of current quiz mode
 * @returns HTML content with mode description
 */
function getDescriptionHtml(description) {
  return `<div id="quiz-mode-content-description">
            <p class="mode-section-label">decription:</p>
            <div id="mode-description-text">${description}</div>
          </div>`;
}

/**
 * Method used to receive all mode settings HTML code
 *
 * @param {Object} settings list of all settings related to current mode
 * @returns HTML content with all mode settings
 */
function getSettingsHtml(settings) {
  let allSettingsHtml = Object.values(settings)
    .map((item) => getSettingHtml(item))
    .join("");
  return `<p class="mode-section-label">settings:</p>` + allSettingsHtml;
}

/**
 * Method used to receive mode single setting HTML code
 *
 * @param {Object} setting data with specified type, label and details
 * @returns HTML content with single mode setting
 */
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

/**
 * Method used to receive mode supported languages setting HTML code
 *
 * @param {String} label ontaining information about languages setting
 * @param {String} details containing short names of supported languages
 * @returns HTML content with mode languages setting
 */
function getLanguagesHtml(label, details) {
  let allLanguages = details.split(" ");
  let hasDefaultLanguage = details.indexOf("!") > 0;
  let allLanguagesHtml = allLanguages
    .map((item) => {
      let isSelected = hasDefaultLanguage ? item.indexOf("!") > 0 : details.startsWith(item);
      return getLanguageHtml(item, isSelected);
    })
    .join("");
  return `<div class="quiz-mode-setting-container">
            <label>${label}</label>
            ${allLanguagesHtml}
          </div>`;
}

/**
 * Method used to receive specified language flag HTML code
 *
 * @param {String} language flag name representing specific image in languages resources
 * @returns HTML content with specified language icon
 */
function getLanguageHtml(language, selected) {
  let languageClass = `language-flag ${selected ? "language-selected" : ""}`;
  let languageName = selected ? language.substring(0, 2) : language;
  let languageFile = `images/languages/${languageName}-24.png`;
  let languageClick = `changeLanguage('${languageName}')`;
  return `<img class="${languageClass}" src="${languageFile}" onclick="${languageClick}"/>`;
}

function changeLanguage(newLanguage) {
  console.log("change language to: " + newLanguage);
}

/**
 * Method used to receive mode total questions number setting HTML code
 *
 * @param {String} label containing information about questions number setting
 * @param {String} details containing questions number value information
 * @returns HTML content with mode questions number setting
 */
function getQuestionsHtml(label, details) {
  return `<div class="quiz-mode-setting-container">
            <label for="quiz-mode-settings-question-no">${label}</label>
            <input type="number" id="quiz-mode-settings-question-no" ${details} />
          </div>`;
}

/**
 * Method used to create mode controls HTML code
 *
 * @returns HTML content with mode controls
 */
function getControlsHtml() {
  return `<div id="quiz-mode-tile-controls">
            <button id="quiz-mode-controls-start" class="dynamic-border" onclick="startQuiz()">start</button>
            <div id="quiz-mode-controls-info" class="hide" title="PUT EXTRA INFO HERE"></div>
          </div>`;
}

showQuizModes();
