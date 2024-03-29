const DEFAULT_LANGUAGE_MARK = "!";
var modeLanguageMap = new Map();
var availableModes = undefined;

/**
 * Method used to show quiz modes view (as a list of selectable tiles)
 */
function displayQuizModes() {
  availableModes = [];
  quizModeViewUpdateUI(STATE_QUIZ_LOAD);
  getQuizModes((err, data) => {
    if (err) {
      quizModeViewUpdateUI(STATE_QUIZ_ERROR);
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      quizModeViewUpdateUI(STATE_QUIZ_OK);
      let quizModeContainer = document.getElementById("quiz-modes-container");
      if (quizModeContainer === null) return;
      quizModeContainer.innerHTML = Object.values(data)
        .map((item, index) => {
          availableModes.push(item);
          return getModeHtml(item, index);
        })
        .join("");
    }
  });
}

/**
 * Method used to receive complete single quiz mode HTML code
 *
 * @param {Object} mode containig all data needed to display quiz mode
 * @param {Integer} index of the element which HTML code we want to receive
 * @returns HTML content with single quiz mode
 */
function getModeHtml(mode, index) {
  return `<div class="quiz-mode-tile ${getTileSize(index)} ${getTileColor(index)}">
            ${getTitleHtml(mode.name)}
            <div class="quiz-mode-tile-content">
              ${getDescriptionHtml(mode.description)}
              <div class="quiz-mode-content-settings">
                ${getSettingsHtml(mode.id, mode.settings)}
              </div>
            </div>
            ${getControlsHtml(mode.id)}
          </div>`;
}

/**
 * Method used to get tile size if form of a predefined style class
 *
 * @param {Integer} index of the element which size we want to receive
 * @returns CSS class name representing tile size ('medium' for index = 0, random for others)
 */
function getTileSize(index) {
  var availableTileSizes = ["big", "medium", "small"];
  var randomIndex = Math.floor(Math.random() * availableTileSizes.length)
  return "tile-size-" + (index === 0 ? "medium" : availableTileSizes[randomIndex]);
}

/**
 * Method used to generate tile color if form of a predefined style class
 *
 * @param {Integer} index of the element which color we want to receive
 * @returns CSS class name representing tile color ('primary' for index = 0, random for others)
 */
function getTileColor(index) {
  var availableTileColors = ["red", "pink", "violet", "blue", "green", "yellow", "orange"];
  var randomIndex = Math.floor(Math.random() * availableTileColors.length)
  return "tile-color-" + (index === 0 ? "primary" : availableTileColors[randomIndex]);
}

/**
 * Method used to receive mode title HTML code
 *
 * @param {String} title of current quiz mode
 * @returns HTML content with mode title
 */
function getTitleHtml(title) {
  return `<div class="quiz-mode-tile-title">${title}</div>`;
}

/**
 * Method used to receive mode description HTML code
 *
 * @param {String} description of current quiz mode
 * @returns HTML content with mode description
 */
function getDescriptionHtml(description) {
  return `<div class="quiz-mode-content-description">
            <p class="mode-section-label">decription:</p>
            <div class="mode-description-text">${description}</div>
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
        modeLanguageMap.set(modeId, item);
      }
      return getLanguageHtml(item, isSelected, modeId);
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
 * @param {Integer} modeId quiz mode identifier as a language identifier (grouping per mode)
 * @returns HTML content with specified language icon
 */
function getLanguageHtml(language, selected, modeId) {
  let languageId = `language-family-${modeId}`;
  let languageClass = `language-flag ${selected ? "language-selected" : ""}`;
  let languageName = selected ? language.substring(0, 2) : language;
  let languageFile = `images/language-flags/${languageName}-24.png`;
  let languageClick = `changeLanguage('${languageName}', ${modeId})`;
  return `<img id="${languageId}" class="${languageClass}" src="${languageFile}" onclick="${languageClick}"/>`;
}

/**
 * Method used to change active language by updating 'language-selected' class in flags elements
 *
 * @param {String} newLanguage name of image flag which should be marked with 'language-selected' class
 * @param {Integer} familyId unique language/mode family identifier (to swap between languages in one mode)
 */
function changeLanguage(newLanguage, familyId) {
  const searchString = `/${newLanguage}-24.png`;
  const languageFlags = Array.from(document.getElementsByClassName("language-flag"));
  if (languageFlags === null) return;
  languageFlags
    .filter((flagElement) => flagElement.getAttribute("id") === `language-family-${familyId}`)
    .map((flagElement) => {
      let selectedClass = flagElement.getAttribute("src").indexOf(searchString) > 0 ? "language-selected" : "";
      flagElement.className = `language-flag ${selectedClass}`;
    });
  modeLanguageMap.set(familyId, newLanguage);
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
  return `<div class="quiz-mode-tile-controls">
            <button data-mode="${modeId}" class="quiz-mode-controls-start dynamic-border" onclick="${modeClick}">
              start
            </button>
            <div data-mode="${modeId}" class="quiz-mode-controls-info hide" title="PUT EXTRA INFO HERE"></div>
          </div>`;
}