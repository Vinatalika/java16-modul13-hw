package http;


import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class HttpUtil {
    public static final HttpClient CLIENT = HttpClient.newHttpClient();
    public static final Gson GSON = new Gson();
    private static String username;

    public static User sendPost(URI uri, User user) throws IOException, InterruptedException {
        String requestBody = GSON.toJson(user);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return GSON.fromJson(response.body(), User.class);
    }
    public static User sendPut(URI uri, User user) throws IOException, InterruptedException {
        String requestBody = GSON.toJson(user);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return GSON.fromJson(response.body(), User.class);
        } else {
            System.err.println("Failed to update user. Status code: " + response.statusCode());
            return null;
        }
    }

    public static void sendDelete(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("Object deleted successfully");
        } else {
            System.err.println("Failed to delete object. Status code: " + response.statusCode());
        }
    }
    public static List<User> sendGetAll(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return Arrays.asList(GSON.fromJson(response.body(), User[].class));
        } else {
            System.err.println("Failed to retrieve users. Status code: " + response.statusCode());
            return Collections.emptyList();
        }
    }
    public static User getUserById(URI uri, int userId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri.toString() + "/" + userId))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return GSON.fromJson(response.body(), User.class);
        } else {
            System.err.println("Failed to retrieve user. Status code: " + response.statusCode());
            return null;
        }
    }
    public static User getUserByUsername(URI uri, String username) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri.toString() + "?username=" + username))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            User[] users = GSON.fromJson(response.body(), User[].class);
            return users.length > 0 ? users[0] : null;
        } else {
            System.err.println("Failed to retrieve user. Status code: " + response.statusCode());
            return null;
        }
    }
    public static List<Post> getPosts(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return Arrays.asList(GSON.fromJson(response.body(), Post[].class));
        } else {
            System.err.println("Failed to retrieve posts. Status code: " + response.statusCode());
            return Collections.emptyList();
        }
    }

    public static List<Comment> getComments(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return Arrays.asList(GSON.fromJson(response.body(), Comment[].class));
        } else {
            System.err.println("Failed to retrieve comments. Status code: " + response.statusCode());
            return Collections.emptyList();
        }
    }

    public static List<Comment> getCommentsForLatestPost(URI userPostsUri, URI postCommentsUri, int userId) throws IOException, InterruptedException {

        List<Post> userPosts = getPosts(userPostsUri);

        Optional<Post> latestPost = userPosts.stream()
                .max(Comparator.comparingInt(Post::getId));

        if (latestPost.isPresent()) {
            int latestPostId = latestPost.get().getId();

            URI latestPostCommentsUri = URI.create(postCommentsUri.toString() + "/" + latestPostId + "/comments");

            List<Comment> comments = getComments(latestPostCommentsUri);

            writeCommentsToFile(userId, latestPostId, comments);

            return comments;
        } else {
            System.err.println("No posts found for the user with ID " + userId);
            return Collections.emptyList();
        }
    }
    private static void writeCommentsToFile(int userId, int postId, List<Comment> comments) {
        String fileName = "user-" + userId + "-post-" + postId + "-comments.json";

        try (FileWriter fileWriter = new FileWriter(fileName)) {
            GSON.toJson(comments, fileWriter);
            System.out.println("Comments written to file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing comments to file: " + e.getMessage());
        }
    }
    public static List<Task> getOpenTasksForUser(URI uri, int userId) throws IOException, InterruptedException {
        URI userTasksUri = URI.create(uri.toString() + "/" + userId + "/todos");

        List<Task> userTasks = getTasks(userTasksUri);
        List<Task> openTasks = userTasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());

        return openTasks;
    }
    public static List<Task> getTasks(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return Arrays.asList(GSON.fromJson(response.body(), Task[].class));
        } else {
            System.err.println("Failed to retrieve tasks. Status code: " + response.statusCode());
            return Collections.emptyList();
        }
    }

}


