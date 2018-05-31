package org.ds.api.controller;

import org.ds.api.JsonWrapper;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@RestController
public class UserController {
    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/api/addUser",
            method = RequestMethod.POST)
    public HashMap<String, Object> addUser(@RequestHeader("auth-token") String authToken,
                                           @RequestBody String userName){
        String query = "INSERT INTO user_account(authtoken, username) VALUES (?, ?);";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, authToken);
            statement.setString(2, userName);
            statement.execute();
        } catch (SQLException e) {
            JsonWrapper.generateError(e.getMessage());
        }
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/getShareBook",
            method = RequestMethod.POST)
    public HashMap<String, Object> getShareBook(@RequestHeader("auth-token") String authToken,
                                           @RequestBody String link){
        String query = "INSERT INTO user_books VALUES " +
                "(?, (SELECT booktable FROM books WHERE link = ? LIMIT 1), 'share') " +
                "RETURNING booktable";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, authToken);
            statement.setString(2, link);
            ResultSet rs = statement.executeQuery();
            if (rs.next()){
                String bookId = rs.getString(1);
                template.convertAndSend(BookController.getCallbackPath(), "");
            } else {
                JsonWrapper.generateError("Shared book can not found!");
            }
        } catch (SQLException e) {
            JsonWrapper.generateError(e.getMessage());
        }
        return JsonWrapper.generateJson(null);
    }
}
