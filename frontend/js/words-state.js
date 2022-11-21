const LOAD_WORDS_OK = 0;
const LOAD_WORDS_LOAD = 1;
const LOAD_WORDS_ERROR = 2;

/**
 * Method used to update GUI state while loading words from service
 *
 * @param {Integer} state current loading state (from: LOAD_WORDS_OK, LOAD_WORDS_LOAD, LOAD_WORDS_ERROR)
 */
function loadWordsUpdateUiState(state) {
  let addWordBtn = document.getElementById("btn-add-word");
  let rowElement = document.getElementById("no-words-row");
  let textElement = document.getElementById("no-words-text");
  if (rowElement === null || textElement === null) return;
  if (LOAD_WORDS_OK === state) {
    addWordBtn.className = "enabled no-select";
    addWordBtn.href = "#modal";
    addWordBtn.addEventListener("click", addWord);
    rowElement.className = "row-hidden no-select";
    textElement.innerHTML = "";
    return;
  }
  if (LOAD_WORDS_LOAD === state) {
    addWordBtn.className = "disabled no-select";
    addWordBtn.removeAttribute("href");
    addWordBtn.onclick = null;
    rowElement.className = "row-loading no-select";
    textElement.innerHTML = addLoadingWidget() + "<br>loading words...";
    return;
  }
  if (LOAD_WORDS_ERROR === state) {
    addWordBtn.className = "disabled no-select";
    addWordBtn.removeAttribute("href");
    addWordBtn.onclick = null;
    rowElement.className = "row-visible no-select";
    textElement.innerHTML = addErrorWidget() + "<br>cannot receive words...";
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
 * @param {Integer} newUiState current loading state (from: LOAD_WORDS_OK, LOAD_WORDS_LOAD, LOAD_WORDS_ERROR)
 */
function changeWordUpdateUiState(newUiState) {
  if (LOAD_WORDS_OK === newUiState) {
    console.log("changeWordUpdateUiState -> OK");
    return;
  }
  if (LOAD_WORDS_LOAD === newUiState) {
    console.log("changeWordUpdateUiState -> LOAD");
    return;
  }
  if (LOAD_WORDS_ERROR === newUiState) {
    console.log("changeWordUpdateUiState -> ERROR");
    return;
  }
}
