package chat.common;

public class Message {
    private String username;
    private String content;

    public Message(String username, String content) {
        this.username = username;
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    // Formats the message to include the username and content.
    public String format() {
        return username + ": " + content;
    }

    @Override
    public String toString() {
        return format();
    }
}