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
      quizModesTableBody.innerHTML = Object.values(data)
        .map((item) => {
          settingsQuizModes.push(item);
          return `<tr id="mode-${item.id}" class="not-selected" onclick="selectMode(${item.id})">
                  <td>${item.id}</td>
                  <td>${item.name}</td>
                </tr>`;
        })
        .join("");
    }
  });
}

function selectMode(modeId) {
  let modeSelected = settingsQuizModes.find((mode) => mode.id === modeId);
  if (modeSelected === undefined) return;
  updateQuizModesTable(modeId);
  updateQuizModesPlaceholder(modeSelected);
}

function updateQuizModesTable(modeId) {
  document.querySelectorAll(`table#quiz-modes-available tbody tr`).forEach((tableRow) => {
    tableRow.className = tableRow.id === `mode-${modeId}` ? "selected" : "not-selected";
  });
}

function updateQuizModesPlaceholder(mode) {
  let modePlaceholder = document.getElementById("mode-placeholder");
  if (modePlaceholder === null) return;
  modePlaceholder.className = "mode-selected";
  modePlaceholder.innerHTML = addGeneralSettingBox(mode.name, mode.description)
                            + addDropTarget()
                            + mode.settings.map(setting => addModeSettingBox(setting) + addDropTarget()).join("");
  initializeDragAndDropEvents();
}

function addGeneralSettingBox(modeName, modeDescription) {
  return addCollapsibleBox(false, "general settings", `<p>${modeName} - ${modeDescription}</p>`);
}

function addModeSettingBox(modeSetting) {
  return addCollapsibleBox(true, modeSetting.type, `<p>${modeSetting.details}</p>`);
}

function addDropTarget() {
  return `<div class="drop-target hide">setting can be dropped here</div>`
}

function addCollapsibleBox(draggable, title, content) {
  return `<div draggable="${draggable}" class="setting-box">
            <button type="button" class="collapsible-button" onclick="toggleCollapse(event)">${title}</button>
            <div class="collapsible-content collapsed">
              ${content}
            </div>
          </div>`;
}

function toggleCollapse(event) {
  event.currentTarget.classList.toggle("active");
  var content = event.currentTarget.nextElementSibling;
  content.classList.toggle("collapsed");
  content.classList.toggle("expanded");
}

function initializeDragAndDropEvents() {
  let modeSettingBoxes = document.querySelectorAll('.setting-box');
  modeSettingBoxes.forEach(box => {
    box.addEventListener('dragstart', handleBoxDragStart);
    box.addEventListener('dragend', handleBoxDragEnd);
  });
}

function handleBoxDragStart(e) {
  this.style.opacity = '0.5';
  updateDropTargetsState(true, this);
  e.dataTransfer.effectAllowed = 'move';
  e.dataTransfer.setData('text/html', this.innerHTML);
}

function handleBoxDragEnd(e) {
  this.style.opacity = '1';
  updateDropTargetsState(false, this);
}

function handleBoxDragEnter(e) {
  this.classList.add('over');
}

function handleBoxDragLeave(e) {
  this.classList.remove('over');
}

function handleBoxDragOver(e) {
  e.preventDefault();
  return false;
}

function handleBoxDrop(e) {
  e.stopPropagation();
  this.insertAdjacentHTML('beforebegin', `<div draggable="true" class="setting-box">${e.dataTransfer.getData('text/html')}</div>`);
  return false;
}

function updateDropTargetsState(visible, draggedElement) {
  let dropTargets = document.querySelectorAll(".drop-target");
  dropTargets.forEach((target) => {
    let showTarget = target.nextSibling === draggedElement || target.previousSibling === draggedElement ? false : visible;
    target.classList.remove(showTarget ? "hide" : "show");
    target.classList.add(showTarget ? "show" : "hide");
    if (showTarget) {
      target.addEventListener('dragover', handleBoxDragOver);
      target.addEventListener("dragenter", handleBoxDragEnter);
      target.addEventListener("dragleave", handleBoxDragLeave);
      target.addEventListener("drop", handleBoxDrop);
    } else {
      target.removeEventListener('dragover', handleBoxDragOver);
      target.removeEventListener("dragenter", handleBoxDragEnter);
      target.removeEventListener("dragleave", handleBoxDragLeave);
      target.removeEventListener("drop", handleBoxDrop);
    }
  });
}
