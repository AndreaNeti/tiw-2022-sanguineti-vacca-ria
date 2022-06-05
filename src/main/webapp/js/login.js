/**
 * Login management
 */
(function() { // avoid variables ending up in the global scope
	document.getElementById("login").addEventListener('click', (e) => {
		var form = e.target.closest("form");
		var errorMsg = document.getElementById("errorMsg");
		if (form.checkValidity()) {
			makeCall("POST", "Login", form,
				function success(message) {
					window.sessionStorage.setItem('username', message);
					window.location.href = "home.html";
				},
				function error(message) {
					errorMsg.textContent = message;
				}
			);
		} else {
			errorMsg.textContent = "Missing parameters";
			form.reportValidity();
		}
	});

})();