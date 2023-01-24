function selectTab(event, tabName) {
  // This is to clear the previous clicked content.
  let tabContent = document.getElementsByClassName("tab-content");
  for (i = 0; i < tabContent.length; i++) {
    tabContent[i].style.display = "none";
  }
  // Set the tab to be "active".
  let tabItems = document.getElementsByClassName("tab-item");
  for (i = 0; i < tabItems.length; i++) {
    tabItems[i].className = tabItems[i].className.replace(" active", "");
  }
  // Display the clicked tab and set it to active.
  document.getElementById(tabName).style.display = "flex";
  event.currentTarget.className += " active";
}
