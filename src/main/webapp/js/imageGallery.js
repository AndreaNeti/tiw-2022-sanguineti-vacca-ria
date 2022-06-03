/**
 * Home management
 */
{ // avoid variables ending up in the global scope
	let errorMsg, successMsg, content;

	window.addEventListener("load", () => {
		if (window.sessionStorage.getItem("username") == null) {
			window.location.href = "login.html";
		} else {
			let pageOrchestrator = new PageOrchestrator();
			pageOrchestrator.start();
			pageOrchestrator.changePage(new AlbumList(pageOrchestrator.changePage));
		}
	});

	function PageOrchestrator() {
		var self = this;

		this.start = function() {
			errorMsg = document.getElementById("errorMsg");
			successMsg = document.getElementById("successMsg");
			content = document.getElementById("content");
			document.getElementById("username").textContent = sessionStorage.getItem('username');
			document.querySelector("a[href='Logout']").addEventListener('click', () => {
				window.sessionStorage.removeItem('username');
			})
		};

		this.reset = function() {
			errorMsg.textContent = "";
			successMsg.textContent = "";
			content.textContent = "";
		}

		this.changePage = function(newPage) {
			self.reset();
			newPage.start();
		}
	}

	function AlbumList(_changePage) {
		this.changePage = _changePage; // orchestrator changePage function

		this.start = function() {
			var self = this;
			makeCall("GET", "GetAlbums", null,
				function success(message) {
					self.update(JSON.parse(message));
				},
				function error(message) {
					errorMsg.textContent = message;
				});
		}

		this.update = function(albums) {
			var self = this;
			var myAlbumList = albums.myAlbums;
			var otherAlbumList = albums.otherAlbums;
			var appendAlbum = function(album, container) {
				// album tab
				let albumTab = document.createElement("div");
				albumTab.classList.add("albumTab");
				// album title
				let albumTitle = document.createElement("div");
				albumTitle.classList.add("albumTitle");
				albumTitle.textContent = album.title;
				// album date
				let albumDate = document.createElement("div");
				albumDate.classList.add("albumDate");
				albumDate.textContent = album.date;

				albumTab.appendChild(albumTitle);
				albumTab.appendChild(albumDate);

				albumTab.addEventListener("click", (event) => {
					self.changePage(new AlbumDetails(self.changePage, album.id));
				});

				container.appendChild(albumTab);
			}
			// create my album containter
			var section = document.createElement("div");
			section.classList.add("section");
			// set title
			var sectionTitle = document.createElement("div");
			sectionTitle.classList.add("sectionTitle");
			sectionTitle.textContent = "Your Albums";
			section.appendChild(sectionTitle);
			// add my albums to container
			myAlbumList.forEach(function(album) {
				appendAlbum(album, section);
			});
			// append my albums container to content
			content.appendChild(section);

			// create other albums container
			section = document.createElement("div");
			section.classList.add("section");
			// set title
			sectionTitle = document.createElement("div");
			sectionTitle.classList.add("sectionTitle");
			sectionTitle.textContent = "Other People's Albums";
			section.appendChild(sectionTitle);
			// add other albums to container
			otherAlbumList.forEach(function(album) {
				appendAlbum(album, section);
			});
			// append other albums container to content
			content.appendChild(section);
		}

		this.registerEvents = function(orchestrator) {
			// add listeners to albums
		}
	}

	function AlbumDetails(_changePage, _idAlbum) {
		this.changePage = _changePage;
		this.idAlbum = _idAlbum;
		var page;
		var images;
		var htmlThumbnails = [];
		var leftButton, rightButton;
		var self = this;

		this.start = function() {
			var self = this;
			makeCall("GET", "AlbumPage?album=" + self.idAlbum, null,
				function success(message) {
					images = JSON.parse(message);
					self.update();
				},
				function error(message) {
					errorMsg.textContent = message;
				});
		}

		this.update = function() {
			if (!images || images.length === 0) {
				errorMsg.textContent = "No images found";
				return;
			}
			// called for first time, create html structure
			if (htmlThumbnails.length === 0) {
				page = 0;
				// create images container
				var imagesContainer = document.createElement("div");
				imagesContainer.classList.add("thumbnails");
				// create left button and set its listener (only if album has more than 5 images)
				if (images.length > 5) {
					leftButton = document.createElement("div");
					leftButton.id = "left";
					leftButton.textContent = "‹";
					leftButton.addEventListener("click", (event) => {
						changePage(page - 1);
					});
					// in page 0 left button is always invisible
					leftButton.style.visibility = "hidden";
					imagesContainer.appendChild(leftButton);
				}
				// create images 
				for (let i = 0; i < images.length && i < 5; i++) {
					// create image container
					var thumbnail = document.createElement("div");
					thumbnail.classList.add("thumbnail");
					// create image and set attributes
					var img = document.createElement("img");
					img.setAttribute("alt", images[i].title);
					img.setAttribute("src", "ImageStreamer?image=" + images[i].path);
					// create image title div
					var imageTitle = document.createElement("div");
					imageTitle.classList.add("imageTitle");
					imageTitle.textContent = images[i].title;

					thumbnail.appendChild(img);
					thumbnail.appendChild(imageTitle);
					htmlThumbnails[i] = thumbnail;
					imagesContainer.appendChild(thumbnail);
				}
				// append images container to content
				content.appendChild(imagesContainer);
				// create right button and set its listener (only if album has more than 5 images)
				if (images.length > 5) {
					rightButton = document.createElement("div");
					rightButton.id = "right";
					rightButton.textContent = "›";
					rightButton.addEventListener("click", (event) => {
						changePage(page + 1);
					});
					imagesContainer.appendChild(rightButton);
					// if album has more than 5 images, right button is visible (in page 0)
				}
			} else {
				if (leftButton) {
					if (page > 0) // there is a previous page
						leftButton.style.visibility = "visible";
					else
						leftButton.style.visibility = "hidden";
				}
				if (rightButton) {
					if (page * 5 + 5 < images.length) // there is a next page
						rightButton.style.visibility = "visible";
					else
						rightButton.style.visibility = "hidden";
				}
				// if html tags are already created, update attributes
				var thumbnailIndex = 0;
				for (let i = page * 5; i < images.length && thumbnailIndex < 5; i++, thumbnailIndex++) {
					htmlThumbnails[thumbnailIndex].style.visibility = "visible";
					htmlThumbnails[thumbnailIndex].querySelectorAll(':scope > img')[0].setAttribute("alt", images[i].title);
					htmlThumbnails[thumbnailIndex].querySelectorAll(':scope > img')[0].setAttribute("src", "ImageStreamer?image=" + images[i].path);
					htmlThumbnails[thumbnailIndex].querySelectorAll(':scope > .imageTitle')[0].textContent = images[i].title;
				}
				// hide old images if this page has less than 5 images
				for (; thumbnailIndex < 5; thumbnailIndex++) {
					htmlThumbnails[thumbnailIndex].style.visibility = "hidden";
				}
			}
		}
		var changePage = function(newPage) {
			if (newPage >= 0) {
				page = newPage;
				self.update();
			}
		}
	}
};