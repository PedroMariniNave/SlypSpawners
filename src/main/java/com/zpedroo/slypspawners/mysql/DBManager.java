package com.zpedroo.slypspawners.mysql;

import com.zpedroo.slypspawners.managers.SpawnerManager;
import com.zpedroo.slypspawners.spawner.PlayerSpawner;
import com.zpedroo.slypspawners.spawner.Spawner;
import org.bukkit.Location;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class DBManager {

    private SpawnerManager manager;

    public DBManager() {
        this.manager = new SpawnerManager();
    }

    public void saveSpawner(PlayerSpawner spawner) {
        if (contains(getManager().serializeLocation(spawner.getLocation()), "location")) {
            String query = "UPDATE `" + DBConnection.TABLE + "` SET" +
                    "`location`='" + getManager().serializeLocation(spawner.getLocation()) + "', " +
                    "`uuid`='" + spawner.getOwnerUUID().toString() + "', " +
                    "`stack`='" + spawner.getStack().toString() + "', " +
                    "`type`='" + spawner.getSpawner().getType() + "', " +
                    "`boost`='" + spawner.getBoost().toString() + "', " +
                    "`friends`='" + serializeFriends(spawner.getFriends()) + "' " +
                    "WHERE `location`='" + getManager().serializeLocation(spawner.getLocation()) + "';";
            executeUpdate(query);
            return;
        }

        String query = "INSERT INTO `" + DBConnection.TABLE + "` (`location`, `uuid`, `stack`, `type`, `boost`, `friends`) VALUES " +
                "('" + getManager().serializeLocation(spawner.getLocation()) + "', " +
                "'" + spawner.getOwnerUUID().toString() + "', " +
                "'" + spawner.getStack().toString() + "', " +
                "'" + spawner.getSpawner().getType() + "', " +
                "'" + spawner.getBoost().toString() + "', " +
                "'" + serializeFriends(spawner.getFriends()) + "');";
        executeUpdate(query);
    }

    public void deleteSpawner(String location) {
        String query = "DELETE FROM `" + DBConnection.TABLE + "` WHERE `location`='" + location + "';";
        executeUpdate(query);
    }

    public HashMap<Location, PlayerSpawner> getPlacedSpawners() {
        HashMap<Location, PlayerSpawner> spawners = new HashMap<>(5120);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                Location location = getManager().deserializeLocation(result.getString(1));
                UUID ownerUUID = UUID.fromString(result.getString(2));
                BigInteger stack = result.getBigDecimal(3).toBigInteger();
                Spawner spawner = getManager().getSpawner(result.getString(4));
                Long boost = result.getLong(5);
                List<String> friends = deserializeFriends(result.getString(6));
                PlayerSpawner playerSpawner = new PlayerSpawner(location, ownerUUID, stack, spawner, boost, friends);

                spawners.put(location, playerSpawner);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return spawners;
    }

    private String serializeFriends(List<String> friends) {
        StringBuilder serialized = new StringBuilder(64);

        for (String friend : friends) {
            if (friend == null) continue;

            serialized.append(friend).append("#");
        }

        return serialized.toString();
    }

    private List<String> deserializeFriends(String serialized) {
        List<String> friends = new ArrayList<>(32);
        String[] split = serialized.split("#");

        friends.addAll(Arrays.asList(split));

        return friends;
    }

    private Boolean contains(String value, String column) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT `" + column + "` FROM `" + DBConnection.TABLE + "` WHERE `" + column + "`='" + value + "';";
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();
            return result.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return false;
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, null, null, statement);
        }
    }

    private void closeConnection(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + DBConnection.TABLE + "` (`location` VARCHAR(255) NOT NULL, `uuid` VARCHAR(255) NOT NULL, `stack` DECIMAL(40,0) NOT NULL, `type` VARCHAR(255) NOT NULL, `boost` LONG NOT NULL, `friends` LONGTEXT NOT NULL, PRIMARY KEY(`location`));";
        executeUpdate(query);
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    private SpawnerManager getManager() {
        return manager;
    }
}