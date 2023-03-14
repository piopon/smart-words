// the list of currently supported languages
const SUPPORTED_LANGUAGES = ["de", "en", "es", "fr", "pl", "pt"];
// variables used by quiz modes tab in settings page
var availableQuizModes = undefined;
var availableModeSettings = undefined;
var currentlyEditedMode = undefined;
var currentlyExpandedState = new Map();
var currentlyDraggedElement = undefined;

/**
 * Method used to read all defined quiz modes from backend service and display them in the UI tab
 */
function initializeTabQuizModes() {
  availableQuizModes = [];
  getQuizModes((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      let quizModesTableBody = document.querySelector("table#quiz-modes-available tbody");
      if (quizModesTableBody === null) return;
      quizModesTableBody.innerHTML = Object.values(data)
        .map((item) => {
          availableQuizModes.push(item);
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
 * Method used to initialize supported settings content (right-most column in quiz modes tab)
 *
 * @returns HTML code for supported settings content column
 */
function initializeSettingsContent() {
  const deletableModeSettings = false;
  let settingsPlaceholder = document.getElementById("settings-placeholder");
  if (settingsPlaceholder === null) return;
  getModeSettings((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      settingsPlaceholder.innerHTML = Object.values(data)
        .map((setting) => {
          availableModeSettings.push(setting);
          return createModeSettingBox(setting, deletableModeSettings);
        })
        .join("");
      updateSupportedSettingsBoxes();
    }
  });
}

/**
 * Method used to display details of the selected mode ID in the settings view
 *
 * @param {Integer} modeId identifier of the mode which details we want to display in settings view
 */
function selectMode(modeId) {
  currentlyEditedMode = availableQuizModes.find((mode) => mode.id === modeId);
  if (currentlyEditedMode === undefined) return;
  currentlyExpandedState.clear();
  updateQuizModesTable(modeId);
  updateQuizModesPlaceholder(currentlyEditedMode);
  updateSupportedSettingsBoxes();
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
 * Method used to update supported setting boxes user interface
 */
function updateSupportedSettingsBoxes() {
  let supportedBoxes = document.querySelectorAll("div#settings-placeholder .setting-box");
  supportedBoxes.forEach(box => {
    var boxButton = box.children[0];
    var boxContent = box.children[1];
    if (currentlyEditedMode === undefined) {
      box.draggable = false;
      boxButton.disabled = true;
      return;
    }
    boxButton.disabled = undefined !== currentlyEditedMode.settings.find((s) => s.type === boxButton.innerText);
    box.draggable = !boxButton.disabled;
    if (boxButton.disabled) {
      boxButton.classList.remove("active");
      boxContent.classList.add("collapsed");
      boxContent.classList.remove("expanded");
    }
  });
}

/**
 * Method used to create the quiz modes placeholder divider HTML content
 *
 * @param {Object} mode which data will be used in a placeholder
 * @returns HTML code of the mode placeholder with input mode data values
 */
function createModePlaceholderContent(mode) {
  const deletableModeSettings = true;
  return createGeneralSettingBox(mode.name, mode.description) +
         createDropTarget(0) +
         mode.settings.map((setting, index) => createModeSettingBox(setting, deletableModeSettings) +
                                               createDropTarget(index + 1)).join("");
}

/**
 * Method used to create HTML code for setting box of type "general"
 *
 * @param {String} modeName the name of mode to be displayed in general setting box
 * @param {String} modeDescription the description of mode to be displayed in general setting box
 * @returns HTML code of the general setting box with specified name and description
 */
function createGeneralSettingBox(modeName, modeDescription) {
  // cannot drag and delete general setting box since it's a const element of all quiz modes
  const draggable = false;
  const deleteable = false;
  const contentTitle = "general";
  const contentValue = createCollapsibleContent("general", { name: modeName, description: modeDescription });
  if (!currentlyExpandedState.has(contentTitle.trim())) {
    currentlyExpandedState.set(contentTitle.trim(), true);
  }
  const expandedState = currentlyExpandedState.get(contentTitle.trim());
  return createSettingBox(draggable, createCollapsibleComponent(contentTitle, contentValue, expandedState, deleteable));
}

/**
 * Method used to create HTML code for setting box used to display specified mode setting
 *
 * @param {Object} modeSetting to be displayed in the setting box
 * @param {Boolean} deleteable flag indicating if setting box should have a delete button (true), or not (false)
 * @returns HTML code of the mode setting box
 */
function createModeSettingBox(modeSetting, deleteable) {
  const draggable = true;
  const contentTitle = modeSetting.type;
  const contentValue = createCollapsibleContent(modeSetting.type, modeSetting);
  if (!currentlyExpandedState.has(contentTitle.trim())) {
    currentlyExpandedState.set(contentTitle.trim(), false);
  }
  const expandedState = currentlyExpandedState.get(contentTitle.trim());
  return createSettingBox(draggable, createCollapsibleComponent(contentTitle, contentValue, expandedState, deleteable));
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
 * @param {Boolean} deleteable flag defining if component should have a delete button (true), or not (false)
 * @returns HTML code for collapsible component (button + content)
 */
function createCollapsibleComponent(buttonTitle, collapsibleContent, expanded, deleteable) {
  const btnExpandedClass = expanded ? "active" : "";
  const divExpandedClass = expanded ? "expanded" : "collapsed";
  return `<button type="button" class="collapsible-button ${btnExpandedClass}" onclick="toggleCollapse(event)">
            ${buttonTitle}
          </button>
          <div class="collapsible-content ${divExpandedClass}">
            ${collapsibleContent}
            ${deleteable ? createDeleteBoxButton() : ""}
          </div>`;
}

/**
 * Method used to create collapsible setting box delete button responsible for removing setting from mode
 *
 * @returns HTML code for setting box delete button
 */
function createDeleteBoxButton() {
  const parentContent = "event.target.parentNode";
  const parentSettingBox = `${parentContent}.parentNode`;
  return `<button class="collapsible-delete" onclick="deleteSettingBox(${parentSettingBox})">
            ❌
          </button>`;
}

/**
 * Method used to create HTML code for collapsible content of specified type
 *
 * @param {String} settingType the type of content which we want to create
 * @param {Object} settingValue data which we want to display as a content
 * @returns HTML code for collapsible content of specified type
 */
function createCollapsibleContent(settingType, settingValue) {
  switch (settingType) {
    case "general":
      return createContentGeneral(settingValue);
    case "questions":
      return createContentQuestions(settingValue);
    case "languages":
      return createContentLanguages(settingValue);
    default:
      return `unknown setting type: ${settingType}`;
  }
}

/**
 * Method used to create HTML code for general settings collapsible content
 *
 * @param {String} setting details of general settings
 * @returns HTML code for collapsible general content
 */
function createContentGeneral(setting) {
  return createSettingInputText("mode name", setting.name) +
         createSettingInputText("mode description", setting.description)
}

/**
 * Method used to create HTML code for questions setting collapsible content
 *
 * @param {String} setting details of question setting
 * @returns HTML code for collapsible quiz questions content
 */
function createContentQuestions(setting) {
  const questionRegex = /value='(?<default>\d+)' min='(?<min>\d+)' max='(?<max>\d+)'/;
  const questionValues = setting.details.match(questionRegex);
  return createSettingInputText("specify setting label", setting.label) +
         createSettingInputNumber("minimum value", questionValues === null ? "": questionValues.groups.min, 1, 5) +
         createSettingInputNumber("default value", questionValues === null ? "": questionValues.groups.default, 1, 50) +
         createSettingInputNumber("maximum value", questionValues === null ? "" :questionValues.groups.max, 25, 50);
}

/**
 * Method used to create HTML code for languages setting collapsible content
 *
 * @param {String} setting details of languages setting
 * @returns HTML code for collapsible quiz languages content
 */
function createContentLanguages(setting) {
  return createSettingInputText("specify setting label", setting.label) +
         createSettingInputLanguage("select supported languages", setting.details);
}

/**
 * Method used to create a single setting input of type text with an appropriate label
 *
 * @param {String} labelText the text displayed in a label
 * @param {String} inputValue the initial value of a text
 * @returns HTML code for input text with a label contained in a divider element
 */
function createSettingInputText(labelText, inputValue) {
  return `<div class="mode-setting-text-edit">
            <label class="mode-setting-label">${labelText}</label>
            <input type="text" class="mode-setting-value" placeholder="specify value" value="${inputValue}" />
          </div>`;
}

/**
 * Method used to create a single setting input of type number with an appropriate label
 *
 * @param {String} labelText the text displayed in a label
 * @param {Integer} inputValue the initial value of a number input
 * @param {Integer} minValue the minimal value of a number input
 * @param {Integer} maxValue the maximym value of a number input
 * @returns HTML code for input number with a label contained in a divider element
 */
function createSettingInputNumber(labelText, initValue, minValue, maxValue) {
  return `<div class="mode-setting-number-edit">
            <label class="mode-setting-label">${labelText}</label>
            <input type="number" class="mode-setting-value" placeholder="specify value" value="${initValue}"
                                                            min="${minValue}" max="${maxValue}"
                                                            onfocusout=forceMinMaxConstraints(this) />
          </div>`;
}

/**
 * Method used to create a single setting input of type language (flag checkboxes) with an appropriate label
 *
 * @param {String} labelText the text displayed in a label
 * @param {String} languages short codes of used languages
 * @returns HTML code for input language with a label contained in a divider element
 */
function createSettingInputLanguage(labelText, languages) {
  var allFlagsCheckboxes = Object.values(SUPPORTED_LANGUAGES)
    .map((lang) => createSettingFlagCheckbox(`${lang}-32-box.png`, languages.includes(lang)))
    .join("");
  return `<div class="mode-setting-languages-edit">
            <label class="mode-setting-label">${labelText}</label>
            <div class="mode-setting-flag-container">${allFlagsCheckboxes}</div>
          </div>`;
}

/**
 * Method used to create a single setting input of type checkbox with an appropriate flag image (as label)
 *
 * @param {String} flag file name which should be displayed as label
 * @param {Boolean} checked if the checkbox should be intially selected (true), or not (false)
 * @returns HTML code for input checkbox with a flag image label contained in a divider element
 */
function createSettingFlagCheckbox(flag, checked) {
  const imageSrc = `images/language-flags/${flag}`;
  const labelClass = `mode-setting-flag-img ${checked ? "flag-checked" : ""}`;
  return `<div class="mode-setting-flag-checkbox">
            <input type="checkbox" id="check-flag-${flag}" ${checked ? "checked" : ""}/>
            <label class="${labelClass}" onclick="toggleFlagCheckbox(event.target)">
              <img src="${imageSrc}" onclick="toggleFlagCheckbox(event.target.parentNode)"/>
            </label>
          </div>`;
}

/**
 * Method used to toggle flag checkbox
 *
 * @param {Element} flagItem for which we want to update the check state
 */
function toggleFlagCheckbox(flagItem) {
  flagItem.classList.toggle("flag-checked");
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
 * Method used to delete the specified setting box from the UI
 *
 * @param {Element} settingBox which should be deleted
 */
function deleteSettingBox(settingBox) {
  if (!isCurrentModeSetting(settingBox)) {
    return;
  }
  let toDeleteModeSetting = currentlyEditedMode.settings.find(
    (setting) => setting.type === getSettingBoxName(settingBox)
  );
  let oldIndex = currentlyEditedMode.settings.indexOf(toDeleteModeSetting);
  currentlyEditedMode.settings.splice(oldIndex, 1);
  currentlyExpandedState.delete(getSettingBoxName(settingBox));
  updateQuizModesPlaceholder(currentlyEditedMode);
  updateSupportedSettingsBoxes();
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
    if (isCurrentModeSetting(currentlyDraggedElement)) {
      // dragged element is from currently edited mode (exists in mode placeholder)
      let draggedModeSetting = currentlyEditedMode.settings.find(
        (setting) => setting.type === getSettingBoxName(currentlyDraggedElement)
      );
      let oldIndex = currentlyEditedMode.settings.indexOf(draggedModeSetting);
      // add the dragged setting to the new position
      if (dropPosition >= currentlyEditedMode.settings.length) {
        currentlyEditedMode.settings.push(draggedModeSetting);
      } else {
        currentlyEditedMode.settings.splice(dropPosition, 0, draggedModeSetting);
        // we've added new element inside array = check if oldIndex should be updated
        if (dropPosition <= oldIndex) {
          oldIndex++;
        }
      }
      // remove the dragged setting from the previous position
      currentlyEditedMode.settings.splice(oldIndex, 1);
    } else {
      // dragged element is new for currently edited mode (dragged to mode placeholder)
      let newSetting = availableModeSettings.find(
        (setting) => setting.type === getSettingBoxName(currentlyDraggedElement)
      );
      currentlyEditedMode.settings.splice(dropPosition, 0, newSetting);
    }
  } else {
    // we are dropping setting box to delete drop target
    deleteSettingBox(currentlyDraggedElement);
    this.classList.remove('over');
  }
  updateQuizModesPlaceholder(currentlyEditedMode);
  updateSupportedSettingsBoxes();
  return false;
}

/**
 * Method used to update the drop target state and event listeners
 *
 * @param {Boolean} visible if the drop target should be visible and contain appropriate listeners
 * @param {Object} draggedElement candidate for drop (used to correctly display non-sibling targets)
 */
function updateDropTargetsState(visible, draggedElement) {
  const dropDeleteId = "drop-delete";
  let dropTargets = document.querySelectorAll(".drop-target");
  dropTargets.forEach((target) => {
    let showTarget = visible && isCurrentModeSetting(draggedElement);
    if (dropDeleteId !== target.id) {
      // in current mode we must display only those drop targets which are not adjacent to dragged element
      const checkPrev = compareSettingBoxes(draggedElement, target.previousSibling);
      const checkNext = compareSettingBoxes(draggedElement, target.nextSibling);
      showTarget = verifySourcePlaceholder(draggedElement) && (checkPrev || checkNext ? false : visible);
    }
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

/**
 * Method used to verify the source placeholder of the dragged element
 *
 * @param {Object} draggedElement for which we want to determine the source placeholder
 * @returns true if the source placeholder can accept dragged element, false otherwise
 */
function verifySourcePlaceholder(draggedElement) {
  if (isCurrentModeSetting(draggedElement)) {
    // we can always accept dragged element from mode placeholder (position update)
    return true;
  } else {
    // we can accept dragged element if mode placeholder does not have the same setting type
    let draggedBoxName = getSettingBoxName(draggedElement);
    let settingTypeSelector = "div#mode-placeholder button.collapsible-button";
    let currentModeSettings = document.querySelectorAll(settingTypeSelector);
    return !Array.from(currentModeSettings)
      .map((element) => element.innerText)
      .includes(draggedBoxName);
  }
}

/**
 * Method used to compare setting boxes objects
 *
 * @param {Object} firstBox first setting box to compare
 * @param {Object} secondBox second setting box to compare
 * @returns true if the setting boxes are the same object or the same type, false otherwise
 */
function compareSettingBoxes(firstBox, secondBox) {
  if (firstBox === secondBox) {
    return true;
  }
  if (secondBox === null) {
    return false;
  }
  const firstBoxName = getSettingBoxName(firstBox);
  const secondBoxName = getSettingBoxName(secondBox);
  return firstBoxName === secondBoxName;
}

/**
 * Method used to check if specified setting box is currently used in edited quiz mode
 *
 * @param {Element} settingBox to check if its in the currently edited quiz mode
 * @returns true if setting box is in the mode placeholder (currently edited quiz mode)
 */
function isCurrentModeSetting(settingBox) {
  const modePlaceholder = document.getElementById("mode-placeholder");
  return settingBox.parentNode === modePlaceholder;
}

/**
 * Method used to retrieve setting box name based on collapsible button text
 *
 * @param {Element} settingBox which name (type) we want to receive
 * @returns setting box name in a form of a String
 */
function getSettingBoxName(settingBox) {
  return settingBox.children[0].innerText;
}
