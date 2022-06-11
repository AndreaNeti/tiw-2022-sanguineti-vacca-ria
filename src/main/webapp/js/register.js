/**
 * Register management
 */
(function() { // avoid variables ending up in the global scope
	// already logged
	window.addEventListener("load", () => {
		if (window.sessionStorage.getItem("username") != null) {
			window.location.href = "home.html";
		}
	});
	var alertMessage = new Message();
	document.getElementById("register").addEventListener('click', (e) => {
		let form = e.target.closest("form"); let inputs = form.elements;
		let username = inputs["username"];
		let email = inputs["email"];
		let pwd = inputs["pwd"];
		let pwd2 = inputs["pwd2"];
		if (isBlank(username.value) || username.value.length < 4 || username.value.length > 50) {
			if (isBlank(username.value))
				alertMessage.show("Empty or missing username");
			else if (username.value.length < 4)
				alertMessage.show("Min username length is 4");
			else
				alertMessage.show("Max username length is 50");
			username.value = "";
			username.focus();
		} else if (isBlank(email.value) || !isEmailValid(email.value)) {
			if (isBlank(email.value))
				alertMessage.show("Empty or missing email");
			else
				alertMessage.show("Not a valid email");
			email.value = "";
			email.focus();
		} else if (isBlank(pwd.value) || isBlank(pwd2.value) || pwd.value.length < 4 || pwd.value != pwd2.value) {
			if (isBlank(pwd.value) || isBlank(pwd2.value))
				alertMessage.show("Empty or missing password");
			else if (pwd.value.length < 4)
				alertMessage.show("Min password length is 4");
			else
				alertMessage.show("Passwords don't match");
			pwd.value = "";
			pwd.focus();
			pwd2.value = "";
		} else if (form.checkValidity()) {
			// OK
			makeCall("POST", 'Register', form,
				function success() {
					alert("Successfully registered");
					window.location.href = "login.html";
				},
				function error(message) {
					alertMessage.show(message);
				});

		} else {
			alertMessage.show("Missing parameters");
			form.reportValidity();
		}
	});
})();

function isEmailValid(email) { // RFC 5322 Official Standard
	return (/(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/.test(email));
}