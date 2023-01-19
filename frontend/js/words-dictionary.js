var availableDictionaries = {};

// variables holding the currently selected dictionary game, mode, and language
// also they serve a purpose to initialize selector values with starting values
var selectedGame = "quiz";
var selectedMode = "0";
var selectedLanguage = "pl";

/**
 * Method used to fill dictionary selectors (especially the starting one: game selector)
 */
function fillDictionarySelectors() {
  loadDictionariesUpdateUI(STATE_WORDS_LOAD);
  getDictionaries((err, data) => {
    if (err) {
      loadDictionariesUpdateUI(STATE_WORDS_ERROR);
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      loadDictionariesUpdateUI(STATE_WORDS_OK);
      initAvailableDictionaries(Object.values(data));
      fillSelector("game", Object.keys(availableDictionaries));
      initSelectorsValues();
    }
  });
}

/**
 * Method used to initialize all selectors with starting values
 */
function initSelectorsValues() {
  let gameSelector = document.getElementById(`dictionary-selector-game`);
  gameSelector.value = selectedGame;
  gameSelector.dispatchEvent(new Event('change'))
  let modeSelector = document.getElementById(`dictionary-selector-mode`);
  modeSelector.value = selectedMode;
  modeSelector.dispatchEvent(new Event('change'))
  let langSelector = document.getElementById(`dictionary-selector-language`);
  langSelector.value = selectedLanguage;
  langSelector.dispatchEvent(new Event('change'))
}

/**
 * Method used to initialize available dictionaries container with received data
 *
 * @param {Object} data received from word service with needed dictionaries information
 */
function initAvailableDictionaries(data) {
  data.forEach((dict) => {
    if (!availableDictionaries[dict.game]) {
      availableDictionaries[dict.game] = {};
    }
    if (!availableDictionaries[dict.game][dict.mode]) {
      availableDictionaries[dict.game][dict.mode] = [];
    }
    availableDictionaries[dict.game][dict.mode].push(dict.language);
  });
}

/**
 * Method used to fill specified selector with input data values
 *
 * @param {String} type of specificied select element (supported values are: 'game', 'mode', and 'language')
 * @param {Array} values to be displayed as options is specified select element
 */
function fillSelector(type, values, disabled) {
  let selector = document.getElementById(`dictionary-selector-${type}`);
  if (selector === null) {
    console.log(`Cannot find a dictionary selector element for ${type}...`);
    return;
  }
  const defaultSelectorOption = `<option value="" default selected hidden>select ${type}</option>`;
  let optionsHtml = defaultSelectorOption
  values.forEach((value) => optionsHtml += `<option value="${value}">${value}</option>`);
  selector.innerHTML = optionsHtml;
  selector.disabled = disabled;
}

/**
 * Method triggered when user changes the value of game select element
 */
function gameChanged() {
  let gameSelector = document.getElementById(`dictionary-selector-game`);
  let modeSelector = document.getElementById(`dictionary-selector-mode`);
  if (gameSelector === null || modeSelector === null) {
    return;
  }
  selectedGame = gameSelector.value;
  fillSelector("mode", Object.keys(availableDictionaries[selectedGame]));
  modeSelector.disabled = selectedGame === "";
}

/**
 * Method triggered when user changes the value of mode select element
 */
function modeChanged() {
  let modeSelector = document.getElementById(`dictionary-selector-mode`);
  let langSelector = document.getElementById(`dictionary-selector-language`);
  if (modeSelector === null || langSelector === null) {
    return;
  }
  selectedMode = modeSelector.value;
  fillSelector("language", availableDictionaries[selectedGame][selectedMode]);
  langSelector.disabled = selectedMode === "";
}

/**
 * Method triggered when user changes the value of language select element
 */
function languageChanged() {
  let langSelector = document.getElementById(`dictionary-selector-language`);
  if (langSelector === null) {
    return;
  }
  selectedLanguage = langSelector.value;
  loadWords(selectedGame, selectedMode, selectedLanguage);
}

// called on words.html site load
fillDictionarySelectors();
