/**
 * Home management
 */
{ // avoid variables ending up in the global scope
	var alertMessage, content, modal, pageOrchestrator;

	window.addEventListener("load", () => {
		if (window.sessionStorage.getItem("username") == null) {
			window.location.href = "login.html";
		} else {
			pageOrchestrator = new PageOrchestrator();
			pageOrchestrator.start();
			pageOrchestrator.changeView(new AlbumList());
		}
	});
	function PageOrchestrator() {
		var self = this;
		let albumList;

		this.start = function() {
			content = document.getElementById("content");
			alertMessage = new Message();
			modal = new Modal();
			albumList = new AlbumList();
			document.getElementById("Username").textContent = sessionStorage.getItem('username');
			document.getElementById("Logout").addEventListener('click', _ => {
				window.sessionStorage.removeItem('username');
			});
			document.getElementById("Home").addEventListener('click', _ => {
				modal.close();
				this.changeView(albumList);
			});
			document.getElementById("CreateAlbum").addEventListener('click', _ => {
				this.changeView(new CreateAlbum(), false);
			});
		};

		this.reset = function() {
			content.textContent = "";
		}

		this.refresh = function() {
			this.reset();
			// show album list (home) on refresh
			albumList.start();
		}

		this.changeView = function(newView, emptyContent = true) {
			if (emptyContent)
				self.reset();
			newView.start();
		}


	}

	function AlbumList() {
		// my album list, got from json
		let myAlbums;
		// html divs
		let myAlbumTabs;
		let editingOrder = false;

		this.start = function() {
			var self = this;
			makeCall("GET", "GetAlbums", null,
				function success(message) {
					self.update(JSON.parse(message));
				},
				function error(message) {
					alertMessage.show(message);
				});
		}

		this.update = function(albums) {
			myAlbums = albums.myAlbums;
			var otherAlbumList = albums.otherAlbums;
			var appendAlbum = function(album, container, reorderable = false) {
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
					pageOrchestrator.changeView(new AlbumPage(album));
				});
				// Only myAlbums are reorderable
				if (reorderable === true) {
					// start drag event
					albumTab.addEventListener("mousedown", (e) => {
						// do something only if in editing mode
						if (editingOrder === true) {
							var albumTabBefore, albumTabBeforeCenter, albumBefore, albumTabAfter, albumTabAfterCenter, albumAfter;
							var getAlbumBeforeAfter = function() {
								albumTabBefore = myAlbumTabs[album.orderValue - 1];
								if (albumTabBefore) {
									let albumTabBeforeRect = albumTabBefore.getBoundingClientRect();
									// the y coordinate of the div center
									albumTabBeforeCenter = albumTabBeforeRect.top + (albumTabBeforeRect.height / 2);
									albumBefore = myAlbums[album.orderValue - 1];
								}
								albumTabAfter = myAlbumTabs[album.orderValue + 1];
								if (albumTabAfter) {
									let albumTabAfterRect = albumTabAfter.getBoundingClientRect();
									// the y coordinate of the div center
									albumTabAfterCenter = albumTabAfterRect.top + (albumTabAfterRect.height / 2);
									albumAfter = myAlbums[album.orderValue + 1];
								}
							}
							// the rectangle containing the album div clicked
							var rect = e.currentTarget.getBoundingClientRect();
							// the click position relative to the album div
							var clickX = (e.pageX - rect.left);
							var clickY = (e.pageY - rect.top);
							var clickedTab = myAlbumTabs[album.orderValue];
							getAlbumBeforeAfter();

							// create a ghost of the clicked album
							var ghost = clickedTab.cloneNode(true);
							ghost.style.position = "fixed";
							ghost.style.width = rect.width + "px";
							ghost.style.margin = 0;
							ghost.style.left = rect.left + "px";
							ghost.style.top = rect.top + "px";
							content.appendChild(ghost);
							// make the clicked album div not visible but still taking space
							clickedTab.style.visibility = "hidden";
							// drag function
							var mouseMove = function(e) {
								// move the ghost following mouse position
								let ghostTop = (e.pageY - clickY)
								let ghostCenter = ghostTop + (rect.height / 2);
								ghost.style.left = (e.pageX - clickX) + "px";
								ghost.style.top = ghostTop + "px";
								// swap with upper album
								if (albumTabBefore && ghostCenter < albumTabBeforeCenter) {
									let oldClickedOrderValue = album.orderValue;
									// swap div positions
									clickedTab.parentNode.insertBefore(clickedTab, albumTabBefore);
									let temp = myAlbumTabs[oldClickedOrderValue];
									myAlbumTabs[oldClickedOrderValue] = myAlbumTabs[oldClickedOrderValue - 1];
									myAlbumTabs[oldClickedOrderValue - 1] = temp;
									// swap divs in the saved array
									temp = myAlbums[oldClickedOrderValue];
									myAlbums[oldClickedOrderValue] = myAlbums[oldClickedOrderValue - 1];
									myAlbums[oldClickedOrderValue - 1] = temp;
									// swap the order values
									album.orderValue = albumBefore.orderValue;
									albumBefore.orderValue = oldClickedOrderValue;
									// calculate new before and after
									getAlbumBeforeAfter();
								} else if (albumTabAfter && ghostCenter > albumTabAfterCenter) { // swap with lower album
									let oldClickedOrderValue = album.orderValue;
									// swap div positions
									clickedTab.parentNode.insertBefore(albumTabAfter, clickedTab);
									let temp = myAlbumTabs[oldClickedOrderValue];
									myAlbumTabs[oldClickedOrderValue] = myAlbumTabs[oldClickedOrderValue + 1];
									myAlbumTabs[oldClickedOrderValue + 1] = temp;
									// swap divs in the saved array
									temp = myAlbums[oldClickedOrderValue];
									myAlbums[oldClickedOrderValue] = myAlbums[oldClickedOrderValue + 1];
									myAlbums[oldClickedOrderValue + 1] = temp;
									// swap the order values
									album.orderValue = albumAfter.orderValue;
									albumAfter.orderValue = oldClickedOrderValue;
									// calculate new before and after
									getAlbumBeforeAfter();
								}

							}
							// drop function
							var mouseUp = function() {
								// make the clicked album div visible again
								clickedTab.style.visibility = "visible";
								// delete ghost
								if (ghost)
									ghost.remove();
								// mouse cick released, remove listeners drag and drop
								document.removeEventListener("mousemove", mouseMove);
								document.removeEventListener("mouseup", mouseUp);
							}
							// after the click now add drag and drop listeners
							document.addEventListener("mousemove", mouseMove);
							document.addEventListener("mouseup", mouseUp);
						}
					});
				}
				container.appendChild(albumTab);
				return albumTab;
			} // end of append album function

			// create my album containter
			var section = document.createElement("div");
			section.classList.add("section");
			// set title
			var sectionTitle = document.createElement("div");
			sectionTitle.classList.add("sectionTitle");
			sectionTitle.textContent = "Your Albums";
			section.appendChild(sectionTitle);

			// if you have at least one album
			if (myAlbums.length > 0) {
				myAlbumTabs = [];
				// add my albums to container
				myAlbums.forEach(function(album) {
					let myAlbumTab = appendAlbum(album, section, true);
					myAlbumTabs.push(myAlbumTab);
				});
			} else {
				section.appendChild(document.createTextNode("No albums"));
			}
			// append my albums container to content
			content.appendChild(section);
			// make sense to reorder only if you have at least 2 albums
			if (myAlbums.length > 1) {
				// create change order button
				let changeOrder = document.createElement("button");
				changeOrder.textContent = "Change Order";
				changeOrder.id = "changeOrder";
				content.appendChild(changeOrder);
				// create cancel order button
				let cancelOrder = document.createElement("button");
				cancelOrder.textContent = "Cancel";
				cancelOrder.id = "cancelOrder";
				content.appendChild(cancelOrder);

				changeOrder.addEventListener("click", _ => {
					if (editingOrder === false) {
						// if first reordering orderValue is null
						myAlbums.forEach(function(album, index) {
							album.orderValue = index;
						});
						changeOrder.textContent = "Save Order";
						cancelOrder.style.display = "inline-block";
						editingOrder = true;
					} else {
						editingOrder = false;
						// send the reordered albums
						makeCall("POST", "GetAlbums", JSON.stringify(myAlbums),
							function success(message) {
								alertMessage.show(message, false);
								changeOrder.textContent = "Change Order";
								cancelOrder.style.display = "none";
							},
							function error(message) {
								alertMessage.show(message);
								// some wrong orderValue in albums, refresh and get the old ones
								pageOrchestrator.refresh();
							},
							false);
					}
				});
				// cancel reordering, refresh to get back old albums order
				cancelOrder.addEventListener("click", _ => {
					editingOrder = false;
					pageOrchestrator.refresh();
				});
			}

			// create other albums container
			section = document.createElement("div");
			section.classList.add("section");
			// set title
			sectionTitle = document.createElement("div");
			sectionTitle.classList.add("sectionTitle");
			sectionTitle.textContent = "Other People's Albums";
			section.appendChild(sectionTitle);
			// if others have at least one album
			if (otherAlbumList.length > 0) {
				// add other albums to container
				otherAlbumList.forEach(function(album) {
					appendAlbum(album, section);
				});
			} else {
				section.appendChild(document.createTextNode("No albums"));
			}
			// append other albums container to content
			content.appendChild(section);
		}
	}

	function AlbumPage(_album) {
		let album = _album;
		let page;
		let images;
		let htmlThumbnails = [];
		let imagesContainer, leftButton, rightButton, imageDetails;

		this.start = function() {
			let self = this;
			makeCall("GET", "AlbumPage?album=" + album.id, null,
				function success(message) {
					images = JSON.parse(message);
					self.initialize();
					// show page 0
					self.update(0);
				},
				function error(message) {
					alertMessage.show(message);
				});
		}

		this.update = function(newPage) {
			// null or empty image list
			if (!images || images.length === 0) {
				alertMessage.show("No images found");
				return;
			}
			if (newPage === page) return;
			if (newPage < 0) {
				alertMessage.show("Invalid page ID");
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
				imageDetails = new ImageDetails();
				// create album title
				let albumTitle = document.createElement("div");
				albumTitle.classList.add("sectionTitle");
				albumTitle.textContent = album.title;
				content.appendChild(albumTitle);
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
					// create right button and set its listener (only if album has more than 5 images)
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
			thumbnailTitle.textContent = decodeHtml(images[imageIndex].title);

			thumbnail.appendChild(img);
			thumbnail.appendChild(thumbnailTitle);
			thumbnail.style.display = "none";
			thumbnail.addEventListener("click", _ => {
				imageDetails.show(images[imageIndex]);
			})
			htmlThumbnails[imageIndex] = thumbnail;
			imagesContainer.appendChild(thumbnail);
		}
	}
	function Modal() {
		let modal, modalContent;

		this.initialize = function() {
			var self = this;
			// create modal div
			modal = document.createElement("div");
			modal.classList.add("modal");
			modal.style.display = "none";
			// create modal conent container
			modalContent = document.createElement("div");
			modalContent.classList.add("modalContent");
			// click out listener
			modal.addEventListener('click', function(e) {
				if (!modalContent.contains(e.target)) {
					self.close();
				}
			});
			// create close button
			let close = document.createElement("div");
			close.classList.add("closeModal");
			close.textContent = "✕";
			close.addEventListener("click", _ => {
				this.close();
			})
			// append to modal
			modal.appendChild(close);
			modal.appendChild(modalContent);
			document.body.appendChild(modal);
		}

		this.show = function(pageRoot) {
			if (!modal) this.initialize();
			modalContent.textContent = "";
			modalContent.appendChild(pageRoot);
			modal.style.display = "block";
		}

		this.close = function() {
			if (modal) {
				modal.style.display = "none";
				modalContent.textContent = "";
			}
		}
	}
	function ImageDetails() {
		let imageDetails, imageTitle, img, imageDate, imageDescription, commentsContainer, imageId;

		this.initialize = function() {
			// create imageDetails container
			imageDetails = document.createElement("div");
			imageDetails.classList.add("imageDetails");

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
			// add comment event
			submit.addEventListener("click", (e) => {
				let form = e.target.closest("form");
				let textArea = form.elements["comment"];
				let commentText = textArea.value;
				if (form.checkValidity()) {
					if (isBlank(commentText)) {
						alertMessage.show("Missing or blank comment");
						textArea.value = "";
						textArea.focus();
					} else {
						// request add comment
						makeCall("POST", "ImageDetails", form,
							function success(message) {
								let comment = {
									nickname: sessionStorage.getItem('username'),
									date: new Date().toLocaleDateString('en-GB'),
									comment: commentText
								};
								addComment(comment);
								textArea.scrollIntoView();
								alertMessage.show(message, false);
							}, function error(message) {
								alertMessage.show(message);
							});
					}
				}
				else {
					alertMessage.show("Missing parameters");
					form.reportValidity();
				}
			})
			form.appendChild(submit);

			subContent.appendChild(form);

			imageDetails.appendChild(subContent);
		}

		this.show = function(image) {
			if (!imageDetails) this.initialize();
			imageTitle.textContent = decodeHtml(image.title);
			img.alt = image.title;
			img.src = "ImageStreamer?image=" + image.path;
			imageDate.textContent = image.date;
			imageDescription.textContent = decodeHtml(image.description);
			imageId.value = image.id;
			commentsContainer.textContent = "";
			imageDetails.scrollTop = 0;
			makeCall("GET", "ImageDetails?image=" + image.id, null,
				function success(message) {
					comments = JSON.parse(message);
					comments.forEach(function(comment) {
						addComment(comment);
					});
				},
				function error(message) {
					alertMessage.show(message);
				});
			modal.show(imageDetails);
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

	function CreateAlbum() {
		this.start = function() {
			var self = this;
			makeCall("GET", "CreateAlbum", null,
				function success(message) {
					self.update(JSON.parse(message));
				},
				function error(message) {
					alertMessage.show(message);
				});
		}

		this.update = function(images) {
			// create form container
			let formContainer = document.createElement("form");
			formContainer.action = "#";
			formContainer.style.textAlign = "center";
			// create title
			let title = document.createElement("div");
			title.classList.add("createAlbumTitle");
			title.textContent = "Create Album";
			formContainer.appendChild(title);
			// album title input
			let albumTitleInput = document.createElement("input");
			albumTitleInput.type = "text";
			albumTitleInput.name = "AlbumTitle";
			albumTitleInput.placeholder = "Album title";
			albumTitleInput.minLength = "4";
			albumTitleInput.maxLength = "50";
			formContainer.appendChild(albumTitleInput);
			// submit input
			let submit = document.createElement("input");
			submit.type = "button";
			submit.value = "Create Album";
			submit.addEventListener("click", (e) => {
				var form = e.target.closest("form");
				makeCall("POST", "CreateAlbum", form,
					function success(message) {
						alertMessage.show(message, false);
						modal.close();
						pageOrchestrator.refresh();
					},
					function error(message) {
						alertMessage.show(message);
					});
			})
			formContainer.appendChild(submit);
			if (images.length > 0) {
				// images div container
				let imagesContainer = document.createElement("div");
				imagesContainer.classList.add("images");
				formContainer.appendChild(imagesContainer);
				images.forEach(function(image) {
					let imageSelection = document.createElement("div");
					imageSelection.classList.add("imageSelection");
					let checkBox = document.createElement("input");
					checkBox.type = "checkbox";
					checkBox.name = "image";
					checkBox.value = image.id;
					imageSelection.appendChild(checkBox);
					// img div
					let img = document.createElement("img");
					img.alt = img.title;
					img.src = "ThumbnailStreamer?image=" + image.path;
					imageSelection.appendChild(img);
					// image title
					let imgTitle = document.createElement("div");
					imgTitle.classList.add("createAlbumImageTitle");
					imgTitle.textContent = decodeHtml(image.title);
					imageSelection.appendChild(imgTitle);

					imagesContainer.appendChild(imageSelection);
				});
				formContainer.appendChild(imagesContainer);
			}
			else {
				formContainer.appendChild(document.createElement("br"));
				formContainer.appendChild(document.createTextNode("No images"));
			}
			// open form container into a modal
			modal.show(formContainer);
		}
	}
};