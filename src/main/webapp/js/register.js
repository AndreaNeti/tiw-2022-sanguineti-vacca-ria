/**
 * Register management
 */
(function() { // avoid variables ending up in the global scope
	document.getElementById("register").addEventListener('click', (e) => {
		var form = e.target.closest("form");
		var errorMsg = document.getElementById("errorMsg");
		if (form.checkValidity()) {
			var inputs = form.elements;
			let username = inputs["username"];
			let email = inputs["email"];
			let pwd = inputs["pwd"];
			let pwd2 = inputs["pwd2"];
			if (isBlank(username.value) || username.value.length < 4 || username.value.length > 50) {
				if (isBlank(username.value))
					errorMsg.textContent = "Empty or missing username";
				else if (username.value.length < 4)
					errorMsg.textContent = "Min username length is 4";
				else
					errorMsg.textContent = "Max username length is 50";
				username.value = "";
				username.focus();
			} else if (isBlank(email.value) || !isEmailValid(email.value)) {
				if (isBlank(email.value))
					errorMsg.textContent = "Empty or missing email";
				else
					errorMsg.textContent = "Not a valid email";
				email.value = "";
				email.focus();
			} else if (isBlank(pwd.value) || isBlank(pwd2.value) || pwd.value.length < 4 || pwd.value != pwd2.value) {
				if (isBlank(pwd.value) || isBlank(pwd2.value))
					errorMsg.textContent = "Empty or missing password";
				else if (pwd.value.length < 4)
					errorMsg.textContent = "Min password length is 4";
				else
					errorMsg.textContent = "Passwords don't match";
				pwd.value = "";
				pwd.focus();
				pwd2.value = "";
			} else { // OK
				makeCall("POST", 'Register', form,
					function success(message) {
						alert("Successfully registered");
						window.location.href = "login.html";
					},
					function error(message) {
						errorMsg.textContent = message;
					});
			}
		} else {
			errorMsg.textContent = "Missing parameters";
			form.reportValidity();
		}
	});
})();

function isEmailValid(email) { // RFC 5322 Official Standard
	return (/(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])/.test(email));
}