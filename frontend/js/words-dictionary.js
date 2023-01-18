var availableDictionaries = {};

// variables holding the currently selected dictionary game, mode, and language
// also they serve a purpose to initialize selector values with starting values
var selectedGame = "quiz";
var selectedMode = "0";
var selectedLanguage = "pl";

function fillDictionarySelectors() {
  getDictionaries((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      initAvailableDictionaries(Object.values(data));
      fillSelector("game", Object.keys(availableDictionaries));
    }
  });
}

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

function fillSelector(type, values) {
  let selector = document.getElementById(`dictionary-selector-${type}`);
  if (selector === null) {
    console.log(`Cannot find a dictionary selector element for ${type}...`);
    return;
  }
  const defaultSelectorOption = `<option value="" default selected hidden>select ${type}</option>`;
  let optionsHtml = defaultSelectorOption
  values.forEach((value) => optionsHtml += `<option value="${value}">${value}</option>`);
  selector.innerHTML = optionsHtml;
}

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

function languageChanged() {
  let langSelector = document.getElementById(`dictionary-selector-language`);
  if (langSelector === null) {
    return;
  }
  selectedLanguage = langSelector.value;
  loadWords();
}

fillDictionarySelectors();
