var selectedGame = "quiz";
var selectedMode = "0";
var selectedLanguage = "pl";

var availableDictionaries = {};

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
  selectedGame = gameSelector.value;
  fillSelector("mode", Object.keys(availableDictionaries[selectedGame]));
  let modeSelector = document.getElementById(`dictionary-selector-mode`);
  modeSelector.disabled = selectedGame === "";
}

function modeChanged() {
  let modeSelector = document.getElementById(`dictionary-selector-mode`);
  selectedMode = modeSelector.value;
  fillSelector("language", availableDictionaries[selectedGame][selectedMode]);
  let langSelector = document.getElementById(`dictionary-selector-language`);
  langSelector.disabled = selectedMode === "";
}

function languageChanged() {
  let langSelector = document.getElementById(`dictionary-selector-language`);
  selectedLanguage = langSelector.value;
  loadWords();
}

fillDictionarySelectors();
