/**
 * Login management
 */
(function() { // avoid variables ending up in the global scope
	var alertMessage = new Message();
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
					alertMessage.show(message);
				}
			);
		} else {
			alertMessage.show("Missing parameters");
			form.reportValidity();
		}
	});

})();