package it.polimi.tiw.utils;

public enum Messages {
	
	//errorMsg
	EMPTY_CREDENTIALS("Missing or empty credential value"),
	WRONG_CREDENTIALS("Incorrect username or password"),
	ALPHANUMERIC_USERNAME("Username must be alphanumeric"),
	INVALID_PASSWORD_SPACES("Password can't contain spaces"),
	INVALID_PASSWORD_LENGTH("Min password length is 4"),
	INVALID_MIN_USERNAME("Min username length is 4"),
	INVALID_MAX_USERNAME("Max username length is 50"),
	INVALID_EMAIL("Not a valid email"),
	PASSWORDS_NO_MATCH("Passwords don't match"),
	USERNAME_TAKEN("Username or email already taken"),
	INVALID_USERNAME("The specified username contains invalid characters."),
	WRONG_USER("test"),
	NOT_LOGGED("You're not logged in"),
	EMPTY_IMAGES("No images found"),
	EMPTY_ALBUM("Cannot create an empty album"),
	EMPTY_ALBUMNAME("Missing or blank album name"),
	INVALID_ALBUM("Invalid album"),
	INVALID_ALBUMPAGE("Invalid album page"),
	INVALID_ID("Invalid ID"),
	ALPHANUMERIC_ALBUMNAME("The album name must be alphanumeric"),
	EMPTY_COMMENT("Missing or blank comment"),
	MIN_ALBUMNAME("Min album name length is 4"),
	MAX_ALBUMNAME("Max album name length is 50"),
	
	
	//successMsg
	
	COMMENT_INSERTED("Comment inserted successfully"),
	ALBUM_CREATED("Album created successfully"),
	USER_REGISTERED("Successfully registered"),
	IMAGE_UPLOADED("Image uploaded successfully");
	
	private final String error;
	
	Messages(String error) {
		this.error = error;
	}
	
	@Override
	public String toString() {
		return error;
	}
}
