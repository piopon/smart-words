/**
 * Method used to show quiz modes
 */
function showQuizModes() {
  getQuizModes((err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      let quizModeContainer = document.getElementById("quiz-modes-container");
      if (quizModeContainer === null) return;
      quizModeContainer.innerHTML = Object.values(data)
        .map((item) => getModeHtml(item))
        .join("");
    }
  });
}

function getModeHtml(mode) {
  return `<div id="quiz-mode-tile" class="tile-size-big tile-color-primary">
            ${getModeTitle(mode.name)}
            <div id="quiz-mode-tile-content">
              ${getModeDescription(mode.description)}
              <div id="quiz-mode-content-settings">
                ${getModeSettings(mode.settings)}
              </div>
            </div>
            ${getModeControls()}
          </div>`;
}

function getModeTitle(title) {
  return `<div id="quiz-mode-tile-title">${title}</div>`;
}

function getModeDescription(description) {
  return `<div id="quiz-mode-content-description">
            <p class="mode-section-label">decription:</p>
            ${description}
          </div>`;
}

function getModeSettings(settings) {
  return `<p class="mode-section-label">settings:</p>`;
}

function getLanguagesHtml(label, details) {
  return `<div>
            <label for="setting-1">${label}</label>
            <img src="images/languages/pl.png"/>
            <img src="images/languages/en.png"/>
            <img src="images/languages/fr.png"/>
            <img src="images/languages/de.png"/>
          </div>`;
}

function getOptionsHtml(label, details) {
  return `<div>
            <label for="setting-2">${label}</label>
            <input type="number" id="setting-2" ${details} />
          </div>`;
}

function getModeControls() {
  return `<div id="quiz-mode-tile-controls">
            <button id="quiz-mode-controls-start" class="dynamic-border" onclick="startQuiz()">start</button>
            <div id="quiz-mode-controls-info" class="hide" title="PUT EXTRA INFO HERE"></div>
          </div>`;
}

showQuizModes();
