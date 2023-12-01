package http;


import java.io.IOException;
import java.net.URI;
import java.util.List;

public class HttpDemo {
    private static final String CREATE_USER_URL = "https://jsonplaceholder.typicode.com/users";
    private static final String UPDATE_USER_URL = "https://jsonplaceholder.typicode.com/users/%d";
    private static final String ALL_USERS_URL = "https://jsonplaceholder.typicode.com/users";

    public static void main(String[] args) throws IOException, InterruptedException {
        User user = new User();
        user.setId(11);
        user.setEmail("mail@example.com");
        user.setName("Nina");
        user.setPhone("000-000");
        user.setWebsite("hhoo");
        user.setUsername("ppo");

        final User createdUser = HttpUtil.sendPost(URI.create(CREATE_USER_URL), user);
        System.out.println("Created User: " + createdUser);

        User updatedUserData = new User();
        updatedUserData.setId(createdUser.getId());
        updatedUserData.setName("NewName");
        updatedUserData.setEmail("newmail@example.com");
        final User updatedUser = HttpUtil.sendPut(URI.create(String.format(UPDATE_USER_URL, createdUser.getId())), updatedUserData);
        System.out.println("Updated User: " + updatedUser);

        URI deleteUserUri = URI.create("https://jsonplaceholder.typicode.com/users/11");
        HttpUtil.sendDelete(deleteUserUri);

        URI allUsersUri = URI.create(ALL_USERS_URL);
        List<User> allUsers = HttpUtil.sendGetAll(allUsersUri);
        for (User retrievedUser : allUsers) {
            System.out.println(retrievedUser);
        }

        int userIdToRetrieve = 8;
        URI userByIdUri = URI.create("https://jsonplaceholder.typicode.com/users");
        User retrievedUserById = HttpUtil.getUserById(userByIdUri, userIdToRetrieve);
        if (retrievedUserById != null) {
            System.out.println("Retrieved User by ID: " + retrievedUserById);
        } else {
            System.err.println("User with ID " + userIdToRetrieve + " not found.");
        }

        String usernameToRetrieve = "Delphine";
        URI userByUsernameUri = URI.create("https://jsonplaceholder.typicode.com/users");
        User retrievedUserByUsername = HttpUtil.getUserByUsername(userByUsernameUri, usernameToRetrieve);
        if (retrievedUserByUsername != null) {
            System.out.println("Retrieved User by Username: " + retrievedUserByUsername);
        } else {
            System.err.println("User with username " + usernameToRetrieve + " not found.");
        }

            int userIdToPost = 1;

            URI userPostsUri = URI.create("https://jsonplaceholder.typicode.com/users/" + userIdToPost + "/posts");
            URI postCommentsUri = URI.create("https://jsonplaceholder.typicode.com/posts");
            HttpUtil.getCommentsForLatestPost(userPostsUri, postCommentsUri, userIdToPost);

            int userIdToTask = 2;
            URI userTasksUri = URI.create("https://jsonplaceholder.typicode.com/users/" + userIdToTask);
            List<Task> openTasks = HttpUtil.getOpenTasksForUser(userTasksUri, userIdToTask);
            for (Task task : openTasks) {
                System.out.println("Open Task: " + task);
            }
        }
    }

