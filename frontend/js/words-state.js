const STATE_WORDS_OK = 0;
const STATE_WORDS_LOAD = 1;
const STATE_WORDS_ERROR = 2;

/**
 * Method used to update GUI state while loading words from service
 *
 * @param {Integer} state current loading state (from: STATE_WORDS_OK, STATE_WORDS_LOAD, STATE_WORDS_ERROR)
 */
function loadWordsUpdateUI(state) {
  let rowElement = document.getElementById("no-words-row");
  let textElement = document.getElementById("no-words-text");
  if (rowElement === null || textElement === null) return;
  addWordUpdateUI(state);
  if (STATE_WORDS_OK === state) {
    rowElement.className = "row-hidden no-select";
    textElement.innerHTML = "";
    return;
  }
  if (STATE_WORDS_LOAD === state) {
    rowElement.className = "row-loading no-select";
    textElement.innerHTML = addLoadingWidget() + "<br>loading words...";
    return;
  }
  if (STATE_WORDS_ERROR === state) {
    rowElement.className = "row-visible no-select";
    textElement.innerHTML = addErrorWidget() + "<br>cannot receive words...";
    return;
  }
}

/**
 * Method used to update UI state of add word button
 *
 * @param {Integer} state current loading state (from: STATE_WORDS_OK, STATE_WORDS_LOAD, STATE_WORDS_ERROR)
 */
function addWordUpdateUI(state) {
  let addWordBtn = document.getElementById("btn-add-word");
  if (STATE_WORDS_OK === state) {
    addWordBtn.className = "enabled no-select";
    addWordBtn.href = "#modal";
    addWordBtn.addEventListener("click", addWord);
    return;
  }
  if (STATE_WORDS_LOAD === state) {
    addWordBtn.className = "disabled no-select";
    addWordBtn.removeAttribute("href");
    addWordBtn.onclick = null;
    return;
  }
  if (STATE_WORDS_ERROR === state) {
    addWordBtn.className = "disabled no-select";
    addWordBtn.removeAttribute("href");
    addWordBtn.onclick = null;
    return;
  }
}

/**
 * Method used to generate HTML code responsible for creating a loader
 *
 * @returns HTML code with loader section
 */
function addLoadingWidget() {
  return `<div id="loader-wrapper">
            <div class="loader no-select">
            <div class="line"></div>
            <div class="line"></div>
            <div class="line"></div>
            </div>
          </div>`;
}

/**
 * Method used to generate HTML code with empty loader placeholder
 *
 * @returns HTML code with loader section placeholder
 */
function addErrorWidget() {
  return `<div id="loader-wrapper"></div>`;
}

/**
 * Method used to update word dictionary UI state after changing (adding/updating) word
 *
 * @param {Integer} newUiState current loading state (from: STATE_WORDS_OK, STATE_WORDS_LOAD, STATE_WORDS_ERROR)
 */
function changeWordUpdateUI(newUiState) {
  let wordsTableOverlay = document.getElementById("table-overlay");
  let overlayTextContainer = document.getElementById("table-overlay-text");
  let overlayTextValue = document.getElementById("overlay-text-content");
  if (wordsTableOverlay === null || overlayTextContainer === null) return;
  addWordUpdateUI(newUiState);
  if (STATE_WORDS_OK === newUiState) {
    wordsTableOverlay.className = "overlay-hide";
    overlayTextContainer.className = "overlay-hide";
    return;
  }
  if (STATE_WORDS_LOAD === newUiState) {
    wordsTableOverlay.className = "overlay-show";
    overlayTextContainer.className = "overlay-show";
    overlayTextValue.className = "text-load";
    overlayTextValue.innerHTML = "connecting...";
    return;
  }
  if (STATE_WORDS_ERROR === newUiState) {
    wordsTableOverlay.className = "overlay-show";
    overlayTextContainer.className = "overlay-show";
    overlayTextValue.className = "text-error";
    overlayTextValue.innerHTML = "connection error!";
    return;
  }
}
