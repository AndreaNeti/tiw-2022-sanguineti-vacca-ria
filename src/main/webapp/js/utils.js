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
					window.location.href = "login.html";
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
	return (!str || /^\s*$/.test(str));
}