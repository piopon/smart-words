function selectTab(event, tabId) {
  let tabContent = document.getElementsByClassName("tab-content");
  for (i = 0; i < tabContent.length; i++) {
    tabContent[i].classList.remove("visible");
  }
  document.getElementById(tabId).classList.add("visible");

  let tabItems = document.getElementsByClassName("tab-item");
  for (i = 0; i < tabItems.length; i++) {
      tabItems[i].className = tabItems[i].className.replace(" active", "");
  }
  event.currentTarget.className += " active";
}
