/**
 * Method used to show quiz modes
 */
function showQuizModes() {
  getQuizModes((err, data) => {
    if (err) {
      console.log("ERROR: " + err);
    } else {
      console.log(data);
    }
  });
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
  return `<p class="mode-section-label">settings:</p>
          <div>
            <label for="setting-1">select language:</label>
            <img src="images/languages/pl.png"/>
            <img src="images/languages/en.png"/>
            <img src="images/languages/fr.png"/>
            <img src="images/languages/de.png"/>
          </div>
          <div>
            <label for="setting-2">how many questions:</label>
            <input type="number" id="setting-2" value="10" min="5" max="25" />
          </div>`;
}

function getModeControls() {
  return `<div id="quiz-mode-tile-controls">
            <button id="quiz-mode-controls-start" class="dynamic-border" onclick="startQuiz()">start</button>
            <div id="quiz-mode-controls-info" class="hide" title="PUT EXTRA INFO HERE"></div>
          </div>`;
}

showQuizModes();
