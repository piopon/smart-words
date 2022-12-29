const DEFAULT_LANGUAGE_MARK = "!";
var availableModes = undefined;
var selectedLanguage = undefined;

/**
 * Method used to show quiz modes
 */
function showQuizModes() {
  availableModes = [];
  getQuizModes((err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      let quizModeContainer = document.getElementById("quiz-modes-container");
      if (quizModeContainer === null) return;
      quizModeContainer.innerHTML = Object.values(data)
        .map((item) => {
          availableModes.push(item);
          return getModeHtml(item);
        })
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
  return `<div id="quiz-mode-tile" class="${randomTileSize()} ${randomTileColor()}">
            ${getTitleHtml(mode.name)}
            <div id="quiz-mode-tile-content">
              ${getDescriptionHtml(mode.description)}
              <div id="quiz-mode-content-settings">
                ${getSettingsHtml(mode.id, mode.settings)}
              </div>
            </div>
            ${getControlsHtml(mode.id)}
          </div>`;
}

/**
 * Method used to generate random tile size if form of a predefined style class
 *
 * @returns CSS class name representing (random) tile size
 */
function randomTileSize() {
  var availableTileSizes = ["big", "medium", "small"];
  var randomIndex = Math.floor(Math.random() * availableTileSizes.length)
  return "tile-size-" + availableTileSizes[randomIndex];
}

/**
 * Method used to generate random tile color if form of a predefined style class
 *
 * @returns CSS class name representing (random) tile color
 */
function randomTileColor() {
  var availableTileColors = ["primary", "red", "pink", "violet", "blue", "green", "yellow", "orange"];
  var randomIndex = Math.floor(Math.random() * availableTileColors.length)
  return "tile-color-" + availableTileColors[randomIndex];
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
 * @param {Integer} modeId parent quiz mode identifier of the settings to be displayed
 * @param {Object} settings list of all settings related to current mode
 * @returns HTML content with all mode settings
 */
function getSettingsHtml(modeId, settings) {
  let allSettingsHtml = Object.values(settings)
    .map((item) => getSettingHtml(modeId, item))
    .join("");
  return `<p class="mode-section-label">settings:</p>` + allSettingsHtml;
}

/**
 * Method used to receive mode single setting HTML code
 *
 * @param {Integer} modeId parent quiz mode identifier of the concrete setting to be displayed
 * @param {Object} setting data with specified type, label and details
 * @returns HTML content with single mode setting
 */
function getSettingHtml(modeId, setting) {
  switch (setting.type) {
    case "languages":
      return getLanguagesHtml(modeId, setting.label, setting.details);
    case "questions":
      return getQuestionsHtml(modeId, setting.label, setting.details);
    default:
      return "";
  }
}

/**
 * Method used to receive mode supported languages setting HTML code
 *
 * @param {Integer} modeId parent quiz mode identifier of the languages to be displayed
 * @param {String} label ontaining information about languages setting
 * @param {String} details containing short names of supported languages
 * @returns HTML content with mode languages setting
 */
function getLanguagesHtml(modeId, label, details) {
  let allLanguages = details.split(" ");
  let hasDefaultLanguage = details.indexOf(DEFAULT_LANGUAGE_MARK) > 0;
  let allLanguagesHtml = allLanguages
    .map((item) => {
      let isSelected = hasDefaultLanguage ? item.indexOf(DEFAULT_LANGUAGE_MARK) > 0 : details.startsWith(item);
      if (isSelected) {
        selectedLanguage = item;
      }
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
 * @param {Boolean} selected if current language HTML flag should have selected class added
 * @returns HTML content with specified language icon
 */
function getLanguageHtml(language, selected) {
  let languageClass = `language-flag ${selected ? "language-selected" : ""}`;
  let languageName = selected ? language.substring(0, 2) : language;
  let languageFile = `images/languages/${languageName}-24.png`;
  let languageClick = `changeLanguage('${languageName}')`;
  return `<img class="${languageClass}" src="${languageFile}" onclick="${languageClick}"/>`;
}

/**
 * Method used to change active language by updating 'language-selected' class in flags elements
 *
 * @param {String} newLanguage name of image flag which should be marked with 'language-selected' class
 */
function changeLanguage(newLanguage) {
  const searchString = `/${newLanguage}-24.png`;
  const languageFlags = Array.from(document.getElementsByClassName("language-flag"));
  if (languageFlags === null) return;
  languageFlags.map((flagElement) => {
    let selectedClass = flagElement.getAttribute("src").indexOf(searchString) > 0 ? "language-selected" : "";
    flagElement.className = `language-flag ${selectedClass}`;
  });
  selectedLanguage = newLanguage;
}

/**
 * Method used to receive mode total questions number setting HTML code
 *
 * @param {Integer} modeId parent quiz mode identifier of the questions number to be displayed
 * @param {String} label containing information about questions number setting
 * @param {String} details containing questions number value information
 * @returns HTML content with mode questions number setting
 */
function getQuestionsHtml(modeId, label, details) {
  return `<div class="quiz-mode-setting-container">
            <label for="quiz-mode-${modeId}-settings-question-no">${label}</label>
            <input type="number" id="quiz-mode-${modeId}-settings-question-no" ${details} />
          </div>`;
}

/**
 * Method used to create mode controls HTML code
 *
 * @returns HTML content with mode controls
 */
function getControlsHtml(modeId) {
  let modeClick = `startQuiz(${modeId})`;
  return `<div id="quiz-mode-tile-controls">
            <button id="quiz-mode-controls-start" class="dynamic-border" onclick="${modeClick}">start</button>
            <div id="quiz-mode-controls-info" class="hide" title="PUT EXTRA INFO HERE"></div>
          </div>`;
}

showQuizModes();
