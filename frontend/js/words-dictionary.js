var selectedMode = "0";
var selectedLanguage = "pl";

function fillDictionarySelectors() {
  getDictionaries((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      console.log(data);
    }
  });
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