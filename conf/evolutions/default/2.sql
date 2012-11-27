# --- !Ups

INSERT INTO users (email, password_hash, first_name, last_name, is_active) VALUES 
    ('user1@user.com', '', 'John', 'Doe', 1),
    ('user2@user.com', '', 'Jane', 'Doe', 1),
    ('user3@user.com', '', 'Abraham', 'Lincoln', 1),
    ('user4@user.com', '', 'George', 'Washington', 0);

SET @userIdUser1 = (SELECT users.user_id FROM users WHERE users.email = 'user1@user.com');
SET @userIdUser2 = (SELECT users.user_id FROM users WHERE users.email = 'user2@user.com');
SET @userIdUser3 = (SELECT users.user_id FROM users WHERE users.email = 'user3@user.com');
SET @userIdUser4 = (SELECT users.user_id FROM users WHERE users.email = 'user4@user.com');

INSERT INTO rights (name) VALUES
    ('User'),
    ('Admin');

SET @rightIdUser = (SELECT rights.right_id FROM rights WHERE rights.name = 'User');
SET @rightIdAdmin = (SELECT rights.right_id FROM rights WHERE rights.name = 'Admin');

INSERT INTO user_right_map (user_id, right_id) VALUES
    (@userIdUser2, @rightIdUser),
    (@userIdUser3, @rightIdAdmin),
    (@userIdUser3, @rightIdUser),
    (@userIdUser4, @rightIdUser);

# --- !Downs

DELETE FROM user_right_map;

DELETE FROM rights WHERE name IN ('User', 'Admin');

DELETE FROM users WHERE email IN ('user1@user.com', 'user2@user.com', 'user3@user.com', 'user4@user.com');
