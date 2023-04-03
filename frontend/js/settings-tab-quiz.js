// the list of currently supported languages
const SUPPORTED_LANGUAGES = ["de", "en", "es", "fr", "pl", "pt"];
// variables used by quiz modes tab in settings page
var availableQuizModes = [];
var availableModeSettings = [];
var dirtyQuizModes = new Set();
var currentlyEditedMode = undefined;
var currentlyExpandedState = new Map();
var currentlyDraggedElement = undefined;

/**
 * Method used to read all defined quiz modes from backend service and display them in the UI tab
 */
function initializeTabQuizModes() {
  getQuizModes((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      availableQuizModes = data;
      updateQuizModesTable();
    }
  });
}

/**
 * Method used to initialize supported settings content (right-most column in quiz modes tab)
 *
 * @returns HTML code for supported settings content column
 */
function initializeSettingsContent() {
  const watch = false;
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
          return createModeSettingBox(setting, deletableModeSettings, watch);
        })
        .join("");
      updateSupportedSettingsBoxes();
    }
  });
}

/**
 * Method used to create new quiz mode and handle UI update
 */
function createQuizMode() {
  postQuizMode((err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      availableQuizModes.push(data);
      updateQuizModesTable();
    }
  });
}

/**
 * Method used to save quiz mode changes and handle UI update
 */
function updateQuizMode() {
  if (!updateCurrentlyEditedMode()) {
    return;
  }
  putQuizMode(currentlyEditedMode.id, currentlyEditedMode, (err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      console.log(data);
      dirtyQuizModes.delete(currentlyEditedMode.id);
      updateQuizModesTable();
    }
  });
}

/**
 * Method used to delete specified quiz mode and update table (if success)
 *
 * @param {Integer} modeId identifier of the mode which should be deleted
 */
function removeQuizMode(modeId) {
  deleteQuizMode(modeId, (err, data) => {
    if (err) {
      console.log("ERROR " + err.status + ": " + err.message);
    } else {
      console.log(data);
      availableQuizModes = availableQuizModes.filter(mode => mode.id !== modeId);
      currentlyEditedMode = undefined;
      updateQuizModesTable();
      updateQuizModesPlaceholder(currentlyEditedMode);
      updateSupportedSettingsBoxes();
    }
  });
  window.event.stopPropagation();
}

/**
 * Method used to display details of the selected mode ID in the settings view
 *
 * @param {Integer} modeId identifier of the mode which details we want to display in settings view
 */
function selectMode(modeId) {
  // we need to check if a mode was opened and update model changes due to possible focus lost changes (from number inputs)
  if (currentlyEditedMode !== undefined) {
    storeCurrentValuesInQuizMode(currentlyEditedMode);
  }
  currentlyEditedMode = availableQuizModes.find((mode) => mode.id === modeId);
  if (currentlyEditedMode === undefined) return;
  currentlyExpandedState.clear();
  updateQuizModesTableSelection(modeId);
  updateQuizModesPlaceholder(currentlyEditedMode);
  updateSupportedSettingsBoxes();
}

/**
 * Method used to update the quiz modes table content
 */
function updateQuizModesTable() {
  let quizModesTableBody = document.querySelector("table#quiz-modes-available tbody");
  if (quizModesTableBody === null) return;
  quizModesTableBody.innerHTML = createModesTableContent(availableQuizModes);
}

/**
 * Method used to update the table select state of the mode with specified ID
 *
 * @param {Integer} modeId identifier of the mode which should be marked as selected in the modes table
 */
function updateQuizModesTableSelection(modeId) {
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
  if (mode == undefined) {
    modePlaceholder.className = "mode-deselected";
    modePlaceholder.innerHTML = "select mode from available quiz modes table";
  } else {
    modePlaceholder.className = "mode-selected";
    modePlaceholder.innerHTML = createModePlaceholderContent(mode);
    initializeDragAndDropEvents();
  }
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
 * Method used to update currently edited mode including: storing current values and marking mode as dirty
 *
 * @returns true if currently edited mode was updated successfully, false otherwise
 */
function updateCurrentlyEditedMode() {
  if (storeCurrentValuesInQuizMode(currentlyEditedMode)) {
    markCurrentlyEditedModeAsDirty();
    return true;
  }
  return false;
}

/**
 * Method used to store current UI values in the specified quiz modeo object
 *
 * @param {Object} mode in which we want to store current values from UI
 * @returns true if values were stored successfully, false otherwise
 */
function storeCurrentValuesInQuizMode(mode) {
  try {
    const newModeName = getEditedModeInputValue("general-name");
    const updateTable = newModeName !== mode.name;
    mode.name = newModeName;
    if (updateTable) {
      updateQuizModesTable();
    }
    mode.description = getEditedModeInputValue("general-desc");
    mode.settings.forEach(setting => {
      switch (setting.type) {
        case "questions":
          setting.label = getEditedModeInputValue("questions-label");
          setting.details = `value='${getEditedModeInputValue("questions-def")}' `
                          + `min='${getEditedModeInputValue("questions-min")}' `
                          + `max='${getEditedModeInputValue("questions-max")}'`;
          break;
        case "languages":
          setting.label = getEditedModeInputValue("languages-label");
          setting.details = Object.values(SUPPORTED_LANGUAGES)
            .map((lang) => getEditedModeInputCheckState(`check-flag-${lang}`) ? lang + " " : "")
            .join("").trim();
          break;
        default:
          throw `Unknown type: ${setting.type}`;
      }
    });
    return true;
  } catch (e) {
    console.error(`Cannot update mode. ${e}`);
    return false;
  }
}

/**
 * Method used to mark currently edited mode as dirty: add it to set and update quiz modes table
 */
function markCurrentlyEditedModeAsDirty() {
  if (!dirtyQuizModes.has(currentlyEditedMode.id)) {
    dirtyQuizModes.add(currentlyEditedMode.id);
    updateQuizModesTable();
  }
}

/**
 * Method used to create the quiz modes table HTML content
 *
 * @param {Array} modes list of quiz modes which we want to put in a table row/cell
 * @returns HTML code of the quiz modes table data (rows/cells)
 */
function createModesTableContent(modes) {
  return Object.values(modes)
    .map((mode) => {
      return `<tr id="mode-${mode.id}" class="not-selected" onclick="selectMode(${mode.id})">
                <td>${mode.id}</td>
                <td>
                  ${mode.name}
                  ${dirtyQuizModes.has(mode.id) ? "*" : ""}
                  ${mode.deletable ? createDeleteModeButton(mode.id) : ""}
                </td>
              </tr>`;
    })
    .join("");
}

/**
 * Method used to create the quiz modes placeholder divider HTML content
 *
 * @param {Object} mode which data will be used in a placeholder
 * @returns HTML code of the mode placeholder with input mode data values
 */
function createModePlaceholderContent(mode) {
  const deletableModeSettings = mode.deletable;
  return createGeneralSettingBox(mode.name, mode.description) +
         createDropTarget(0) +
         mode.settings.map((setting, index) => createModeSettingBox(setting, deletableModeSettings, true) +
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
  const watch = true;
  const draggable = false;
  const deletable = false;
  const contentTitle = "general";
  const contentValue = createCollapsibleContent("general", { name: modeName, description: modeDescription }, watch);
  if (!currentlyExpandedState.has(contentTitle.trim())) {
    currentlyExpandedState.set(contentTitle.trim(), true);
  }
  const expandedState = currentlyExpandedState.get(contentTitle.trim());
  return createSettingBox(draggable, createCollapsibleComponent(contentTitle, contentValue, expandedState, deletable));
}

/**
 * Method used to create HTML code for setting box used to display specified mode setting
 *
 * @param {Object} modeSetting to be displayed in the setting box
 * @param {Boolean} deletable flag indicating if setting box should have a delete button (true), or not (false)
 * @param {Boolean} watch flag used to indetify if we should watch this mode setting box to update mode dirty state
 * @returns HTML code of the mode setting box
 */
function createModeSettingBox(modeSetting, deletable, watch) {
  const draggable = true;
  const contentTitle = modeSetting.type;
  const contentValue = createCollapsibleContent(modeSetting.type, modeSetting, watch);
  if (!currentlyExpandedState.has(contentTitle.trim())) {
    currentlyExpandedState.set(contentTitle.trim(), false);
  }
  const expandedState = currentlyExpandedState.get(contentTitle.trim());
  return createSettingBox(draggable, createCollapsibleComponent(contentTitle, contentValue, expandedState, deletable));
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
 * @param {Boolean} deletable flag defining if component should have a delete button (true), or not (false)
 * @returns HTML code for collapsible component (button + content)
 */
function createCollapsibleComponent(buttonTitle, collapsibleContent, expanded, deletable) {
  const btnExpandedClass = expanded ? "active" : "";
  const divExpandedClass = expanded ? "expanded" : "collapsed";
  return `<button type="button" class="collapsible-button ${btnExpandedClass}" onclick="toggleCollapse(event)">
            ${buttonTitle}
          </button>
          <div class="collapsible-content ${divExpandedClass}">
            ${collapsibleContent}
            ${deletable ? createDeleteBoxButton() : ""}
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
 * Method used to create quiz mode delete button responsible for removing specified mode
 *
 * @param {Integer} id identified of the mode to be deleted
 * @returns HTML code for quiz mode delete button
 */
function createDeleteModeButton(id) {
  return `<button class="delete-mode" onclick="removeQuizMode(${id})">
            ❌
          </button>`;
}

function createDirtyModeMarker() {
  return `<div class="dirty-mode-marker">💾❗</div>`;
}

/**
 * Method used to create HTML code for collapsible content of specified type
 *
 * @param {String} settingType the type of content which we want to create
 * @param {Object} settingValue data which we want to display as a content
 * @param {Boolean} watch flag used to indetify if we should watch this content to update mode dirty state
 * @returns HTML code for collapsible content of specified type
 */
function createCollapsibleContent(settingType, settingValue, watch) {
  switch (settingType) {
    case "general":
      return createContentGeneral(settingValue, watch);
    case "questions":
      return createContentQuestions(settingValue, watch);
    case "languages":
      return createContentLanguages(settingValue, watch);
    default:
      return `unknown setting type: ${settingType}`;
  }
}

/**
 * Method used to create HTML code for general settings collapsible content
 *
 * @param {Object} setting data to be displayed in general settings
 * @param {Boolean} watch flag used to indetify if we should watch this content to update mode dirty state
 * @returns HTML code for collapsible general content
 */
function createContentGeneral(setting, watch) {
  return createSettingInputText("general-name", watch, "mode name", setting.name) +
         createSettingInputText("general-desc", watch, "mode description", setting.description)
}

/**
 * Method used to create HTML code for questions setting collapsible content
 *
 * @param {Object} setting data to be displayed in question setting
 * @param {Boolean} watch flag used to indetify if we should watch this content to update mode dirty state
 * @returns HTML code for collapsible quiz questions content
 */
function createContentQuestions(setting, watch) {
  const questionRegex = /value='(?<default>\d*)' min='(?<min>\d*)' max='(?<max>\d*)'/;
  const questionValues = setting.details.match(questionRegex);
  const initMinimumValue = questionValues === null ? "": questionValues.groups.min;
  const initDefaultValue = questionValues === null ? "": questionValues.groups.default;
  const initMaximumValue = questionValues === null ? "" :questionValues.groups.max;
  return createSettingInputText("questions-label", watch, "specify setting label", setting.label) +
         createSettingInputNumber("questions-min", watch, "minimum value", initMinimumValue, 1, 5) +
         createSettingInputNumber("questions-def", watch, "default value", initDefaultValue, 1, 50) +
         createSettingInputNumber("questions-max", watch, "maximum value", initMaximumValue, 25, 50);
}

/**
 * Method used to create HTML code for languages setting collapsible content
 *
 * @param {Object} setting data to be displayed in languages setting
 * @param {Boolean} watch flag used to indetify if we should watch this content to update mode dirty state
 * @returns HTML code for collapsible quiz languages content
 */
function createContentLanguages(setting, watch) {
  return createSettingInputText("languages-label", watch, "specify setting label", setting.label) +
         createSettingInputLanguage("languages-used", watch, "select supported languages", setting.details);
}

/**
 * Method used to create a single setting input of type text with an appropriate label
 *
 * @param {Integer} id unique identifier of the created input
 * @param {Boolean} watch flag used to indetify if we should watch input change and update mode dirty state
 * @param {String} labelText the text displayed in a label
 * @param {String} inputValue the initial value of a text
 * @returns HTML code for input text with a label contained in a divider element
 */
function createSettingInputText(id, watch, labelText, inputValue) {
  return `<div class="mode-setting-text-edit">
            <label class="mode-setting-label" for="${id}">${labelText}</label>
            <input type="text" id="${id}" class="mode-setting-value"
                                          placeholder="specify value" value="${inputValue}"
                                          ${watch ? `oninput="updateCurrentlyEditedMode()"` : ``}/>
          </div>`;
}

/**
 * Method used to create a single setting input of type number with an appropriate label
 *
 * @param {Integer} id unique identifier of the created input
 * @param {Boolean} watch flag used to indetify if we should watch input change and update mode dirty state
 * @param {String} labelText the text displayed in a label
 * @param {Integer} inputValue the initial value of a number input
 * @param {Integer} minValue the minimal value of a number input
 * @param {Integer} maxValue the maximym value of a number input
 * @returns HTML code for input number with a label contained in a divider element
 */
function createSettingInputNumber(id, watch, labelText, initValue, minValue, maxValue) {
  return `<div class="mode-setting-number-edit">
            <label class="mode-setting-label" for="${id}">${labelText}</label>
            <input type="number" id="${id}" class="mode-setting-value"
                                            placeholder="specify value" value="${initValue}"
                                            min="${minValue}" max="${maxValue}"
                                            ${watch ? `oninput="updateCurrentlyEditedMode()"` : ``}
                                            onfocusout="forceMinMaxConstraints(this)"/>
          </div>`;
}

/**
 * Method used to create a single setting input of type language (flag checkboxes) with an appropriate label
 *
 * @param {Integer} id unique identifier of the created input
 * @param {Boolean} watch flag used to indetify if we should watch input change and update mode dirty state
 * @param {String} labelText the text displayed in a label
 * @param {String} languages short codes of used languages
 * @returns HTML code for input language with a label contained in a divider element
 */
function createSettingInputLanguage(id, watch, labelText, languages) {
  var allFlagsCheckboxes = Object.values(SUPPORTED_LANGUAGES)
    .map((lang) => createSettingFlagCheckbox(watch, lang, languages.includes(lang)))
    .join("");
  return `<div class="mode-setting-languages-edit">
            <label class="mode-setting-label" for="${id}">${labelText}</label>
            <div id="${id}" class="mode-setting-flag-container">${allFlagsCheckboxes}</div>
          </div>`;
}

/**
 * Method used to create a single setting input of type checkbox with an appropriate flag image (as label)
 *
 * @param {Boolean} watch flag used to indetify if we should watch checkbox and update mode dirty state
 * @param {String} flag file name which should be displayed as label
 * @param {Boolean} checked if the checkbox should be intially selected (true), or not (false)
 * @returns HTML code for input checkbox with a flag image label contained in a divider element
 */
function createSettingFlagCheckbox(watch, flag, checked) {
  const imageSrc = `images/language-flags/${flag}-32-box.png`;
  const labelClass = `mode-setting-flag-img ${checked ? "flag-checked" : ""}`;
  return `<div class="mode-setting-flag-checkbox">
            <input type="checkbox" id="check-flag-${flag}" ${checked ? "checked" : ""}/>
            <label class="${labelClass}" onclick="toggleFlagCheckbox(event.target, ${watch})">
              <img src="${imageSrc}" onclick="toggleFlagCheckbox(event.target.parentNode, ${watch})"/>
            </label>
          </div>`;
}

/**
 * Method used to toggle flag checkbox
 *
 * @param {Element} flagItem for which we want to update the check state
 * @param {Boolean} watch flag used to indetify if we should update mode dirty state
 */
function toggleFlagCheckbox(flagItem, watch) {
  const linkedCheckbox = flagItem.previousElementSibling;
  if (linkedCheckbox !== null) {
    linkedCheckbox.checked = !linkedCheckbox.checked;
    if (watch) {
      updateCurrentlyEditedMode();
    }
  }
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
 * Method used to receive edited mode input value from an element determined by ID
 *
 * @param {String} inputId unique identifier of the input element which value to receive
 * @returns a String value from the input element
 */
function getEditedModeInputValue(inputId) {
  const inputElement = document.querySelector(`div#mode-placeholder input#${inputId}`);
  if (inputElement === null) {
    throw `Element with ID "${inputId}" could not be found.`;
  }
  if (inputElement.type != "text" && inputElement.type != "number") {
    throw `Element with ID "${inputId}" is not of type "text" nor "number".`;
  }
  return inputElement.value;
}

/**
 * Method used to receive edited mode checked state from an element determined by ID
 *
 * @param {String} inputId unique identifier of the input element which value to receive
 * @returns a Boolean value representing the checked state of input element
 */
function getEditedModeInputCheckState(inputId) {
  const inputElement = document.querySelector(`div#mode-placeholder input#${inputId}`);
  if (inputElement === null) {
    throw `Element with ID "${inputId}" could not be found.`;
  }
  if (inputElement.type != "checkbox") {
    throw `Element with ID "${inputId}" is not of type "checkbox".`;
  }
  return inputElement.checked;
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
  let deleteIndex = currentlyEditedMode.settings.indexOf(toDeleteModeSetting);
  currentlyEditedMode.settings.splice(deleteIndex, 1);
  // we have to clean all fields (except type) since Array.find() returns object by reference
  toDeleteModeSetting.label = '';
  toDeleteModeSetting.details = '';
  // update model after deleting setting box to correctly update quiz modes placeholder in UI
  updateCurrentlyEditedMode();
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
    // update model before possible mode change to correctly update quiz modes placeholder in UI
    updateCurrentlyEditedMode();
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
    if (dropDeleteId === target.id) {
      // for delete drop target we must check if currently edited mode is deletable
      showTarget = showTarget && currentlyEditedMode.deletable == true;
    } else {
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
