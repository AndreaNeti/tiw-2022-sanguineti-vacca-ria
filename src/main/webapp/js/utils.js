/**
 * AJAX call management
 */

function makeCall(method, url, formElement, successCback, errorCback) {
	var req = new XMLHttpRequest(); // visible by closure
	req.onreadystatechange = function() {
		if (req.readyState == XMLHttpRequest.DONE) {
			var message = req.responseText;
			switch (req.status) {
				case 200:
					successCback(message);
					break;
				case 401: //unauthorized
				case 403: // forbidden
					window.location.href = req.getResponseHeader("Location");
					break;
				case 400: // bad request
				case 500: // server error
					errorCback(message);
					break;
			}
		}
	}; // closure
	req.open(method, url);
	if (formElement == null) {
		req.send();
	} else {
		req.send(new FormData(formElement));
	}
	if (formElement !== null) {
		formElement.reset();
	}
}
function isBlank(str) {
	// check if null or empty or only spaces
	return (!str || /^\s*$/.test(str));
}
function decodeHtml(html) {
	// unescape html
	var txt = document.createElement("textarea");
	txt.innerHTML = html;
	return txt.value;
}
function Message() {
	let errorMsg = document.getElementById("errorMsg");
	let successMsg = document.getElementById("successMsg");
	this.show = function(message, error = true) {
		if (error) {			
			errorMsg.classList.add("fade");
			errorMsg.textContent = message;
			setTimeout(function() {errorMsg.classList.remove("fade");}, 5000);
		}
		else {
			successMsg.classList.add("fade");
			successMsg.textContent = message;
			setTimeout(function() {successMsg.classList.remove("fade");}, 5000);
		}
	}
}