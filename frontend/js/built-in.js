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
