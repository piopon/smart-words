var selectedMode = "0";
var selectedLanguage = "pl";

var availableDictionaries = {};

function fillDictionarySelectors() {
  getDictionaries((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      Object.values(data).forEach((dictionary) => {
        if (!availableDictionaries[dictionary.game]) {
          availableDictionaries[dictionary.game] = {};
        }
        if (!availableDictionaries[dictionary.game][dictionary.mode]) {
          availableDictionaries[dictionary.game][dictionary.mode] = [];
        }
        availableDictionaries[dictionary.game][dictionary.mode].push(dictionary.language);
      });
      fillSelector("game", Object.keys(availableDictionaries));
    }
  });
}

function fillSelector(type, values) {
  let selector = document.getElementById(`dictionary-selector-${type}`);
  let optionsHtml = `<option value="" default selected hidden>select ${type}</option>`;
  values.forEach((value) => {
    optionsHtml += `<option value="${value}">${value}</option>`;
  });
  selector.innerHTML = optionsHtml;
}

function gameChanged() {
  let gameSelector = document.getElementById(`dictionary-selector-game`);
  fillSelector("mode", Object.keys(availableDictionaries[gameSelector.value]));
  let modeSelector = document.getElementById(`dictionary-selector-mode`);
  modeSelector.disabled = gameSelector.value === "";
}

function modeChanged() {
  let gameSelector = document.getElementById(`dictionary-selector-game`);
  let modeSelector = document.getElementById(`dictionary-selector-mode`);
  fillSelector("language", availableDictionaries[gameSelector.value][modeSelector.value]);
  let langSelector = document.getElementById(`dictionary-selector-language`);
  langSelector.disabled = modeSelector.value === "";
}

function languageChanged() {
  loadWords();
}

fillDictionarySelectors();
