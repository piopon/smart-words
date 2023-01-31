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
      quizModesTableBody.innerHTML = Object.values(data).map((item) => {
        settingsQuizModes.push(item);
        return `<tr id="mode-${item.id}" class="not-selected" onclick="selectMode(${item.id})">
                  <td>${item.id}</td>
                  <td>${item.name}</td>
                </tr>`;
      }).join("");
    }
  });
}

function selectMode(modeId) {
  document.querySelectorAll(`table#quiz-modes-available tbody tr`).forEach(tableRow => {
    tableRow.className = tableRow.id === `mode-${modeId}` ? "selected" : "not-selected";
  });
  let modePlaceholder = document.getElementById("mode-placeholder");
  modePlaceholder.className = "mode-selected";
  modePlaceholder.innerHTML = addGeneralSettingBox(mode.name, mode.description)
                            + mode.settings.map(setting => addModeSettingBox(setting)).join("");
}

function addGeneralSettingBox(modeName, modeDescription) {
  return `<div draggable="false" class="setting-box">general settings</div>`;
}

function addModeSettingBox(modeSetting) {
  return `<div draggable="true" class="setting-box">${modeSetting.type}</div>`;
}
