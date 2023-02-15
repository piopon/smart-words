/**
 * Method used to initialize settings page (main entry point after loading settings.html)
 */
function initializeSettings() {
  initializeTabQuizModes();
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
