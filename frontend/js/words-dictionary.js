var selectedMode = "0";
var selectedLanguage = "pl";
var availableGames = new Set();
var availableModes = new Set();
var availableLangs = new Set();

function fillDictionarySelectors() {
  getDictionaries((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      Object.values(data).forEach(dictionary => {
        availableGames.add(dictionary.game);
        availableModes.add(dictionary.mode);
        availableLangs.add(dictionary.language);
      });
      fillSelector("game", Array.from(availableGames.values()));
      fillSelector("mode", Array.from(availableModes.values()));
      fillSelector("language", Array.from(availableLangs.values()));
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
  console.log("Game changed");
}

function modeChanged() {
  console.log("Mode changed");
}

function languageChanged() {
  console.log("Language changed");
}

fillDictionarySelectors();