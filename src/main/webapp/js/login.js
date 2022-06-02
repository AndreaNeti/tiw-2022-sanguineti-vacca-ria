/**
 * Login management
 */
(function() { // avoid variables ending up in the global scope
	document.getElementById("login").addEventListener('click', (e) => {
		var form = e.target.closest("form");
		if (form.checkValidity()) {
			makeCall("POST", "Login", form,
				function success(message) {
					window.sessionStorage.setItem('username', message);
					window.location.href = "home.html";
				},
				function error(message) {
					document.getElementById("errorMsg").textContent = message;
				}
			);
		} else {
			form.reportValidity();
		}
	});

})();