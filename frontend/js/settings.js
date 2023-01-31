var settingsQuizModes = undefined;

function initializeSettings() {
  initializeTabQuizModes();
}

function selectTab(event, tabId) {
  let tabItems = document.getElementsByClassName("tab-item");
  for (i = 0; i < tabItems.length; i++) {
    tabItems[i].className = tabItems[i].className.replace(" active", "");
  }
  event.currentTarget.className += " active";

  let tabContent = document.getElementsByClassName("tab-content");
  for (i = 0; i < tabContent.length; i++) {
    tabContent[i].className = "tab-content" + (tabContent[i].id === tabId ? " visible" : "");
  }
}

function initializeTabQuizModes() {
  settingsQuizModes = [];
  getQuizModes((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      let quizModesTableBody = document.querySelector("table#quiz-modes-available tbody");
      if (quizModesTableBody === null) return;
      quizModesTableBody.innerHTML = Object.values(data).map((item) => "<tr><td>0</td><td>name</td></tr>").join("");
    }
  });
}

function selectMode(modeId) {
  document.querySelectorAll(`table#quiz-modes-available tbody tr`).forEach(tableRow => {
    tableRow.className = tableRow.id === `mode-${modeId}` ? "selected" : "not-selected";
  });
  let modePlaceholder = document.getElementById("mode-placeholder");
  modePlaceholder.className = "mode-selected";
  modePlaceholder.innerHTML = "";
}
