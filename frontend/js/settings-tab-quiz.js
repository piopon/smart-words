var settingsQuizModes = undefined;
var currentlyEditedMode = undefined;
var currentlyExpandedState = new Map();
var currentlyDraggedElement = undefined;

/**
 * Method used to read all defined quiz modes from backend service and display them in the UI tab
 */
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

/**
 * Method used to display details of the selected mode ID in the settings view
 *
 * @param {Integer} modeId identifier of the mode which details we want to display in settings view
 */
function selectMode(modeId) {
  currentlyEditedMode = settingsQuizModes.find((mode) => mode.id === modeId);
  if (currentlyEditedMode === undefined) return;
  currentlyExpandedState.clear();
  updateQuizModesTable(modeId);
  updateQuizModesPlaceholder(currentlyEditedMode);
}

/**
 * Method used to update the select table state of the mode with specified ID
 *
 * @param {Integer} modeId identifier of the mode which should be marked as selected in the modes table
 */
function updateQuizModesTable(modeId) {
  document.querySelectorAll(`table#quiz-modes-available tbody tr`).forEach((tableRow) => {
    tableRow.className = tableRow.id === `mode-${modeId}` ? "selected" : "not-selected";
  });
}

/**
 * Method used to update the quiz modes placeholder divider content and style using the selected mode data
 *
 * @param {Object} mode which data will to be displayed in a placeholder
 */
function updateQuizModesPlaceholder(mode) {
  let modePlaceholder = document.getElementById("mode-placeholder");
  if (modePlaceholder === null) return;
  modePlaceholder.className = "mode-selected";
  modePlaceholder.innerHTML = createModePlaceholderContent(mode);
  initializeDragAndDropEvents();
}

/**
 * Method used to create the quiz modes placeholder divider HTML content
 *
 * @param {Object} mode which data will be used in a placeholder
 * @returns HTML code of the mode placeholder with input mode data values
 */
function createModePlaceholderContent(mode) {
  return createGeneralSettingBox(mode.name, mode.description) +
         createDropTarget(0) +
         mode.settings.map((setting, index) => createModeSettingBox(setting) + createDropTarget(index+1)).join("");
}

/**
 * Method used to create HTML code for setting box of type "general"
 *
 * @param {String} modeName the name of mode to be displayed in general setting box
 * @param {String} modeDescription the description of mode to be displayed in general setting box
 * @returns HTML code of the general setting box with specified name and description
 */
function createGeneralSettingBox(modeName, modeDescription) {
  const draggable = false;
  const contentTitle = "general settings";
  const contentValue = `<p>${modeName} - ${modeDescription}</p>`;
  if (!currentlyExpandedState.has(contentTitle.trim())) {
    currentlyExpandedState.set(contentTitle.trim(), true);
  }
  const expandedState = currentlyExpandedState.get(contentTitle.trim());
  return createSettingBox(draggable, createCollapsibleComponent(contentTitle, contentValue, expandedState));
}

/**
 * Method used to create HTML code for setting box used to display specified mode setting
 *
 * @param {Object} modeSetting to be displayed in the setting box
 * @returns HTML code of the mode setting box
 */
function createModeSettingBox(modeSetting) {
  const draggable = true;
  const contentTitle = modeSetting.type;
  const contentValue = `<p>${modeSetting.details}</p>`;
  if (!currentlyExpandedState.has(contentTitle.trim())) {
    currentlyExpandedState.set(contentTitle.trim(), false);
  }
  const expandedState = currentlyExpandedState.get(contentTitle.trim());
  return createSettingBox(draggable, createCollapsibleComponent(contentTitle, contentValue, expandedState));
}

/**
 * Method used to create a container for setting box
 *
 * @param {Boolean} draggable specifies if the container should be draggable (true), or not (false)
 * @param {String} boxContent setting box content
 * @returns HTML code of the setting box container with specified content and draggable flag
 */
function createSettingBox(draggable, boxContent) {
  return `<div draggable="${draggable}" class="setting-box">
            ${boxContent}
          </div>`;
}

/**
 * Method used to create collapsible component with the button responsible for collapsing/expanding and the content
 *
 * @param {String} buttonTitle collapsible component label (as a text of a button for collapsing/expanding)
 * @param {String} collapsibleContent the content which should be displayed when component is expanded
 * @param {Boolean} expanded flag defining if the initial state of component should be expanded (true), or collapsed (false)
 * @returns HTML code for collapsible component (button + content)
 */
function createCollapsibleComponent(buttonTitle, collapsibleContent, expanded) {
  const btnExpandedClass = expanded ? "active" : "";
  const divExpandedClass = expanded ? "expanded" : "collapsed";
  return `<button type="button" class="collapsible-button ${btnExpandedClass}" onclick="toggleCollapse(event)">
            ${buttonTitle}
          </button>
          <div class="collapsible-content ${divExpandedClass}">
            ${collapsibleContent}
          </div>`;
}

function createCollapsibleContent(settingType, settingValue) {
  switch (settingType) {
    case "general":
      return `<p>${settingValue.name} - ${settingValue.description}</p>`;
    default:
      return `<p>${settingValue.details}</p>`
  }
}

/**
 * Method used to create drop target placeholder
 *
 * @param {Integer} dropNo number of drop target (for unique identifying)
 * @returns HTML code for drop target divider
 */
function createDropTarget(dropNo) {
  return `<div id="drop-${dropNo}" class="drop-target hide">
            setting can be dropped here
          </div>`
}

/**
 * Method used to toggle collapse/expand state of a collapsible component (passed via event)
 *
 * @param {Object} event containing data defining the source target which should be collapsed/expanded
 */
function toggleCollapse(event) {
  var pressedButton = event.currentTarget;
  var buttonClasses = pressedButton.classList;
  buttonClasses.toggle("active");
  currentlyExpandedState.set(pressedButton.innerHTML.trim(), buttonClasses.contains("active"));
  var content = pressedButton.nextElementSibling;
  content.classList.toggle("collapsed");
  content.classList.toggle("expanded");
}

/**
 * Method used to initialize drag and drop event listeners for all applicable setting boxes
 */
function initializeDragAndDropEvents() {
  let modeSettingBoxes = document.querySelectorAll('.setting-box');
  modeSettingBoxes.forEach(box => {
    box.addEventListener('dragstart', handleBoxDragStart);
    box.addEventListener('dragend', handleBoxDragEnd);
  });
}

/**
 * Method used to handle the drag start event of a setting box
 *
 * @param {Object} e drag start event data
 */
function handleBoxDragStart(e) {
  this.style.opacity = '0.5';
  currentlyDraggedElement = this;
  updateDropTargetsState(true, this);
  e.dataTransfer.effectAllowed = 'move';
  e.dataTransfer.setData('text/html', this.innerHTML);
}

/**
 * Method used to handle the drag end event of a setting box
 *
 * @param {Object} e drag end event data
 */
function handleBoxDragEnd(e) {
  this.style.opacity = '1';
  currentlyDraggedElement = undefined;
  updateDropTargetsState(false, this);
}

/**
 * Method used to handle the drag enter event of a setting box
 *
 * @param {Object} e drag enter event data
 */
function handleBoxDragEnter(e) {
  this.classList.add('over');
}

/**
 * Method used to handle the drag leave event of a setting box
 *
 * @param {Object} e drag leave event data
 */
function handleBoxDragLeave(e) {
  this.classList.remove('over');
}

/**
 * Method used to handle the drag over event of a setting box
 * This method prevents the default behavior and returns false (good coding practice)
 *
 * @param {Object} e drag over event data
 */
function handleBoxDragOver(e) {
  e.preventDefault();
  return false;
}

/**
 * Method used to handle the drop event of a setting box
 * It stops events propagation, handles drop logic and returns false (good coding practice)
 *
 * @param {Object} e drop event data
 */
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

/**
 * Method used to update the drop target state and event listeners
 *
 * @param {Boolean} visible if the drop target should be visible and contain appropriate listeners
 * @param {Object} draggedElement candidate for drop (used to correctly display non-sibling targets)
 */
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
