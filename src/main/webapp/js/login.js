/**
 * Login management
 */
(function() { // avoid variables ending up in the global scope
	// already logged
	window.addEventListener("load", () => {
		if (window.sessionStorage.getItem("username") != null) {
			window.location.href = "home.html";
		}
	});
	var alertMessage = new Message();
	document.getElementById("login").addEventListener('click', (e) => {
		let form = e.target.closest("form");
		let inputs = form.elements;
		let username = inputs["username"];
		let pwd = inputs["pwd"];
		if (isBlank(username.value) || username.value.length < 4 || username.value.length > 50) {
			if (isBlank(username.value))
				alertMessage.show("Empty or missing username");
			else if (username.value.length < 4)
				alertMessage.show("Min username length is 4");
			else
				alertMessage.show("Max username length is 50");
			username.value = "";
			username.focus();
		} else if (isBlank(pwd.value) || pwd.value.length < 4) {
			if (isBlank(pwd.value))
				alertMessage.show("Empty or missing password");
			else
				alertMessage.show("Min password length is 4");
			pwd.value = "";
			pwd.focus();
		} else if (form.checkValidity()) {
			// OK
			makeCall("POST", "Login", form,
				function success(message) {
					window.sessionStorage.setItem('username', message);
					window.location.href = "home.html";
				},
				function error(message) {
					alertMessage.show(message);
				}
			);
		} else {
			alertMessage.show("Missing parameters");
			form.reportValidity();
		}
	});

})();