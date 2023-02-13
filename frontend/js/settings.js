var settingsQuizModes = undefined;
var currentlyEditedMode = undefined;
var currentlyExpandedState = new Map();
var currentlyDraggedElement = undefined;

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
  currentlyEditedMode = settingsQuizModes.find((mode) => mode.id === modeId);
  if (currentlyEditedMode === undefined) return;
  currentlyExpandedState.clear();
  updateQuizModesTable(modeId);
  updateQuizModesPlaceholder(currentlyEditedMode);
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
  modePlaceholder.innerHTML = createModePlaceholderContent(mode);
  initializeDragAndDropEvents();
}

function createModePlaceholderContent(mode) {
  return createGeneralSettingBox(mode.name, mode.description)
         + createDropTarget(0)
         + mode.settings.map((setting, index) => createModeSettingBox(setting) + createDropTarget(index+1)).join("");
}

function createGeneralSettingBox(modeName, modeDescription) {
  const boxTitle = "general settings";
  if (!currentlyExpandedState.has(boxTitle.trim())) {
    currentlyExpandedState.set(boxTitle.trim(), true);
  }
  return createSettingBox(false, createCollapsibleContent(boxTitle, `<p>${modeName} - ${modeDescription}</p>`, currentlyExpandedState.get(boxTitle.trim())));
}

function createModeSettingBox(modeSetting) {
  const boxTitle = modeSetting.type;
  if (!currentlyExpandedState.has(boxTitle.trim())) {
    currentlyExpandedState.set(boxTitle.trim(), false);
  }
  return createSettingBox(true, createCollapsibleContent(boxTitle, `<p>${modeSetting.details}</p>`, currentlyExpandedState.get(boxTitle.trim())));
}

function createSettingBox(draggable, boxContent) {
  return `<div draggable="${draggable}" class="setting-box">
            ${boxContent}
          </div>`;
}

function createCollapsibleContent(buttonTitle, collapsibleContent, expanded) {
  const btnExpandedClass = expanded ? "active" : "";
  const divExpandedClass = expanded ? "expanded" : "collapsed";
  return `<button type="button" class="collapsible-button ${btnExpandedClass}" onclick="toggleCollapse(event)">
            ${buttonTitle}
          </button>
          <div class="collapsible-content ${divExpandedClass}">
            ${collapsibleContent}
          </div>`;
}

function createDropTarget(dropNo) {
  return `<div id="drop-${dropNo}" class="drop-target hide">
            setting can be dropped here
          </div>`
}

function toggleCollapse(event) {
  var pressedButton = event.currentTarget;
  var buttonClasses = pressedButton.classList;
  buttonClasses.toggle("active");
  currentlyExpandedState.set(pressedButton.innerHTML.trim(), buttonClasses.contains("active"));
  var content = pressedButton.nextElementSibling;
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
  currentlyDraggedElement = this;
  updateDropTargetsState(true, this);
  e.dataTransfer.effectAllowed = 'move';
  e.dataTransfer.setData('text/html', this.innerHTML);
}

function handleBoxDragEnd(e) {
  this.style.opacity = '1';
  currentlyDraggedElement = undefined;
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

Array.prototype.swapItems = function(a, b){
  this[a] = this.splice(b, 1, this[a])[0];
  return this;
}

function handleBoxDrop(e) {
  e.stopPropagation();
  let dropPosition = parseInt(this.id.substring(this.id.indexOf("-") + 1));
  if (!isNaN(dropPosition)) {
    let oldIndex = currentlyEditedMode.settings.indexOf(
      currentlyEditedMode.settings.find(
        (setting) => setting.type === currentlyDraggedElement.firstElementChild.innerHTML.trim()
      )
    );
    let newIndex = dropPosition >= currentlyEditedMode.settings.length ? dropPosition - 1 : dropPosition;
    currentlyEditedMode.settings.swapItems(oldIndex, newIndex);
  }
  updateQuizModesPlaceholder(currentlyEditedMode);
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
