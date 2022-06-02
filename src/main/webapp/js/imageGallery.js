/**
 * Home management
 */
(function() { // avoid variables ending up in the global scope
	let pageOrchestrator = new PageOrchestrator();

	window.addEventListener("load", () => {
		if (window.sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {
			pageOrchestrator.start(); // initialize the components
			pageOrchestrator.refresh();
		}
	});

	function PageOrchestrator() {
		var errorMsg = document.getElementById("errorMsg");
		var successMsg = document.getElementById("successMsg");
		document.querySelector("a[href='Logout']").addEventListener('click', () => {
			window.sessionStorage.removeItem('username');
		})
		
		var albumList = new AlbumList(
			errorMsg, successMsg,
			document.getElementById("yourAlbums"),
			document.getElementById("otherAlbums"));
			
		this.start = function() {
			document.getElementById("username").textContent = sessionStorage.getItem('username');
		};

		this.refresh = function() {
			errorMsg.textContent = "";
			successMsg.textContent = "";
			albumList.reset();
			missionDetails.reset();
			missionsList.show(function() {
				missionsList.autoclick(currentMission);
			}); // closure preserves visibility of this
			wizard.reset();
		};
	}

	makeCall("POST", "GetAlbums", null,
		function success(message) {

		},
		function error(message) {
			errorMsg.textContent = message;
		});
})();