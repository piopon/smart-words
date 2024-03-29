/**
 * Method adding element swap logic to Array prototype
 *
 * @param {Integer} a the index of the first element to be swapped
 * @param {Integer} b the index of the second element to be swapped
 * @returns array with swapped elements
 */
Array.prototype.swapItems = function (a, b) {
  this[a] = this.splice(b, 1, this[a])[0];
  return this;
};

/**
 * Method used to force min and max values constraints in input of type number
 * Currently user is able to bypass these settings by simply typing value outside the range
 *
 * @param {Object} inputElement the input element of type number
 */
function forceMinMaxConstraints(inputElement) {
  if (inputElement.type != "number" || inputElement.value == "") {
    return;
  }
  if (parseInt(inputElement.value) < parseInt(inputElement.min)) {
    inputElement.value = inputElement.min;
  }
  if (parseInt(inputElement.value) > parseInt(inputElement.max)) {
    inputElement.value = inputElement.max;
  }
}
