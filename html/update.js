function update() {
  var p = document.getElementById("p").value, pattern = document
      .getElementById("pattern").value;
  var div = document.getElementById("result");
  try {
    var result = minimatch(p, pattern);
    div.innerHTML = result;
  } catch (e) {
    div.innerHTML = e;
  }
}