const SETTINGS_TOAST_INFO = 0;
const SETTINGS_TOAST_WARNING = 1;
const SETTINGS_TOAST_ERROR = 2;
// variables used in settings page
var toastTimeout = 3000;

/**
 * Method used to initialize settings page (main entry point after loading settings.html)
 */
function initializeSettings() {
  initializeTabQuizModes();
  initializeSettingsContent();
}

/**
 * Method used to update settings view after clicking on tabs navigation button
 *
 * @param {Object} event containing the currently pressed target
 * @param {Integer} tabId the ID of the tab content which should be visible after selecting tab navigation button
 */
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

function settingsChangeShowToast(type, message) {
  console.log(message);
  var wordToast = document.getElementById("settings-changed-toast");
  if (wordToast === null) return;
  if (SETTINGS_TOAST_INFO === type) {
    wordToast.className = "information show";
  } else if (SETTINGS_TOAST_WARNING === type) {
    wordToast.className = "warning show";
  } else if (SETTINGS_TOAST_ERROR === type) {
    wordToast.className = "error show";
  } else {
    wordToast.className = "fatal show";
  }
  wordToast.innerHTML = message;
  setTimeout(() => (wordToast.className = wordToast.className.replace("show", "")), toastTimeout);
}