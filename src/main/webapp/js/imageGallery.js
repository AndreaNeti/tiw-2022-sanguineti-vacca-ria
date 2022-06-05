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
			document.getElementById("Username").textContent = sessionStorage.getItem('username');
			document.getElementById("Logout").addEventListener('click', _ => {
				window.sessionStorage.removeItem('username');
			});
			document.getElementById("Home").addEventListener('click', _ => {
				this.changePage(new AlbumList(this.changePage));
			});
			document.getElementById("CreateAlbum").addEventListener('click', _ => {
				// this.changePage();
			});
			document.getElementById("UploadImage").addEventListener('click', _ => {
				// this.changePage();
			});
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

				albumTab.addEventListener("click", _ => {
					self.changePage(new AlbumDetails(album.id));
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

	function AlbumDetails(_idAlbum) {
		this.idAlbum = _idAlbum;
		let page;
		let images;
		let htmlThumbnails = [];
		let imagesContainer, leftButton, rightButton, modal;

		this.start = function() {
			let self = this;
			makeCall("GET", "AlbumPage?album=" + self.idAlbum, null,
				function success(message) {
					images = JSON.parse(message);
					self.initialize();
					self.update(0);
				},
				function error(message) {
					errorMsg.textContent = message;
				});
		}

		this.update = function(newPage) {
			// null or empty image list
			if (!images || images.length === 0) {
				errorMsg.textContent = "No images found";
				return;
			}
			if (newPage === page) return;
			if (newPage < 0) {
				errorMsg.textContent = "Invalid page ID";
				// TODO go back to home ?
				return;
			}
			if (leftButton) {
				if (newPage > 0) // there is a previous page
					leftButton.style.visibility = "visible";
				else
					leftButton.style.visibility = "hidden";
			}
			if (rightButton) {
				if ((newPage + 1) * 5 < images.length) // there is a next page
					rightButton.style.visibility = "visible";
				else
					rightButton.style.visibility = "hidden";
			}
			// prefetch new images and create html tags
			if ((newPage + 1) * 5 >= htmlThumbnails.length) {
				// create next page images but keep old ones
				for (let i = (newPage + 1) * 5; i < images.length && i < (newPage + 2) * 5; i++) {
					appendImage(i);
				}
			}
			// make old page images not visible
			for (let i = page * 5; i < images.length && i < (page + 1) * 5; i++)
				htmlThumbnails[i].style.display = "none";
			// make new page images visible
			for (let i = newPage * 5; i < images.length && i < (newPage + 1) * 5; i++)
				htmlThumbnails[i].style.display = "block";

			page = newPage;
		}
		this.initialize = function() {
			// called for first time, create html structure
			if (htmlThumbnails.length === 0) {
				modal = new ImageDetails();
				// create images container
				imagesContainer = document.createElement("div");
				imagesContainer.classList.add("thumbnails");
				// create left button and set its listener (only if album has more than 5 images)
				if (images.length > 5) {
					leftButton = document.createElement("div");
					leftButton.id = "left";
					leftButton.textContent = "‹";
					leftButton.addEventListener("click", _ => {
						this.update(page - 1);
					});
					// in page 0 left button is always invisible
					leftButton.style.visibility = "hidden";
					imagesContainer.appendChild(leftButton);
				}
				// create right button and set its listener (only if album has more than 5 images)
				if (images.length > 5) {
					rightButton = document.createElement("div");
					rightButton.id = "right";
					rightButton.textContent = "›";
					rightButton.addEventListener("click", _ => {
						this.update(page + 1);
					});
					imagesContainer.appendChild(rightButton);
					// if album has more than 5 images, right button is visible (in page 0)
				}
				// at start creates 10 images
				for (let i = 0; i < images.length && i < 10; i++) {
					appendImage(i);
				}
				// append images container to content
				content.appendChild(imagesContainer);

				// create and append modal window

			}
		}
		let appendImage = function(imageIndex) {
			if (images.length < imageIndex) return;
			// create image container
			let thumbnail = document.createElement("div");
			thumbnail.classList.add("thumbnail");
			// create image and set attributes
			let img = document.createElement("img");
			img.setAttribute("alt", images[imageIndex].title);
			img.setAttribute("src", "ThumbnailStreamer?image=" + images[imageIndex].path);
			// create image title div
			let thumbnailTitle = document.createElement("div");
			thumbnailTitle.classList.add("thumbnailTitle");
			thumbnailTitle.textContent = images[imageIndex].title;

			thumbnail.appendChild(img);
			thumbnail.appendChild(thumbnailTitle);
			thumbnail.style.display = "none";
			thumbnail.addEventListener("click", _ => {
				modal.show(images[imageIndex]);
			})
			htmlThumbnails[imageIndex] = thumbnail;
			imagesContainer.appendChild(thumbnail);
		}
	}

	function ImageDetails() {
		let imageTitle, img, imageDate, imageDescription, commentsContainer, imageId, modal;

		this.initialize = function() {
			// create modal div
			modal = document.createElement("div");
			modal.classList.add("modal");

			// create imageDetails container
			let imageDetails = document.createElement("div");
			imageDetails.classList.add("imageDetails");

			// create close button
			let close = document.createElement("div");
			close.classList.add("closeModal");
			close.textContent = "✕";
			close.addEventListener("click", _ => {
				this.close();
			})
			imageDetails.appendChild(close);
			// create image title div
			imageTitle = document.createElement("div");
			imageTitle.classList.add("imageTitle");
			imageDetails.appendChild(imageTitle);
			// create img tag
			img = document.createElement("img");
			imageDetails.appendChild(img);
			// create subContent container
			let subContent = document.createElement("div");
			subContent.classList.add("subContent");
			// create image date div
			imageDate = document.createElement("div");
			imageDate.classList.add("imageDate");
			subContent.appendChild(imageDate);
			// create image description div
			imageDescription = document.createElement("div");
			imageDescription.classList.add("imageDescription");
			subContent.appendChild(imageDescription);
			// create comments container (empty)
			commentsContainer = document.createElement("div");
			commentsContainer.classList.add("commentsContainer");
			subContent.appendChild(commentsContainer);

			// create form
			let form = document.createElement("form");
			form.action = "#";
			// create textArea
			let textArea = document.createElement("textarea");
			textArea.id = "commentBox";
			textArea.name = "comment";
			textArea.rows = "4";
			textArea.cols = "50";
			textArea.placeholder = "Leave your comment here";
			form.appendChild(textArea);
			// create image id hidden input
			imageId = document.createElement("input");
			imageId.type = "hidden";
			imageId.name = "image";
			form.appendChild(imageId);
			// br
			let br = document.createElement("br");
			form.appendChild(br);
			// create submit button
			let submit = document.createElement("input");
			submit.type = "button";
			submit.value = "Submit";
			submit.addEventListener("click", (e) => {
				let form = e.target.closest("form");
				let textArea = form.elements["comment"];
				let commentText = textArea.value;
				if (form.checkValidity()) {
					if (isBlank(commentText)) {
						errorMsg.textContent = "Missing or blank comment";
					} else {
						// 
						makeCall("POST", "ImageDetails", form,
							function success() {
								let comment = {
									nickname: sessionStorage.getItem('username'),
									date: new Date().toLocaleDateString('en-GB'),
									comment: commentText
								};
								addComment(comment);
								textArea.scrollIntoView();
							}, function error() {
								errorMsg.textContent = message;
							});
					}
				}
				else {
					errorMsg.textContent = "Missing parameters";
					form.reportValidity();
				}
			})
			form.appendChild(submit);

			subContent.appendChild(form);

			imageDetails.appendChild(subContent);
			modal.style.display = "none";
			content.appendChild(modal);

			modal.appendChild(imageDetails);
		}

		this.show = function(image) {
			// never called before, create all html
			if (!modal) this.initialize();
			modal.style.display = "block";
			imageTitle.textContent = image.title;
			img.alt = image.title;
			img.src = "ImageStreamer?image=" + image.path;
			imageDate.textContent = image.date;
			imageDescription.textContent = image.description;
			imageId.value = image.id;
			makeCall("GET", "ImageDetails?image=" + image.id, null,
				function success(message) {
					comments = JSON.parse(message);
					comments.forEach(function(comment) {
						addComment(comment);
					});
				},
				function error(message) {
					errorMsg.textContent = message;
				});
		}

		this.close = function() {
			modal.style.display = "none";
			imageTitle.textContent = "";
			img.src = "";
			img.alt = "";
			imageDate.textContent = "";
			imageDescription.textContent = "";
			imageId.value = "";
			commentsContainer.textContent = "";
		}

		let addComment = function(comment) {
			// create comment container
			let commentDiv = document.createElement("div");
			commentDiv.classList.add("comment");
			// create comment date div
			let commentDate = document.createElement("div");
			commentDate.classList.add("commentDate");
			commentDate.textContent = comment.date;
			commentDiv.appendChild(commentDate);
			// create comment nickName div
			let commentNickname = document.createElement("div");
			commentNickname.classList.add("commentNickname");
			commentNickname.textContent = comment.nickname;
			commentDiv.appendChild(commentNickname);
			// create comment text div
			let commentText = document.createElement("div");
			commentText.classList.add("commentText");
			commentText.textContent = decodeHtml(comment.comment);
			commentDiv.appendChild(commentText);
			// append to comments container
			commentsContainer.appendChild(commentDiv);
		}
	}
};