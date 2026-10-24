package com.artformgames.plugin.residencelist.storage.sql;

import cc.carm.lib.easyplugin.EasyPlugin;
import cc.carm.lib.easysql.EasySQL;
import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLTable;
import cc.carm.lib.easysql.api.table.NamedSQLTable;
import com.artformgames.plugin.residencelist.api.residence.ResidenceRate;
import com.artformgames.plugin.residencelist.api.sort.SortFunctions;
import com.artformgames.plugin.residencelist.conf.PluginConfig;
import com.artformgames.plugin.residencelist.storage.AbstractStorage;
import com.artformgames.plugin.residencelist.storage.data.CachedUserData;
import com.artformgames.plugin.residencelist.storage.data.ResidenceSnapshot;
import com.artformgames.plugin.residencelist.storage.data.UserSnapshot;
import org.bukkit.Material;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MySQL persistence. Every hook is invoked by AbstractStorage's ordered I/O executor.
 */
public class MySQLStorage extends AbstractStorage {

    private enum DataTable {
        USERS("users", new String[]{
                "`uuid` VARCHAR(36) NOT NULL PRIMARY KEY",
                "`pinned` LONGTEXT NOT NULL",
                "`sort` INT NOT NULL",
                "`reversed` BOOLEAN NOT NULL"
        }),
        RESIDENCES("residences", new String[]{
                "`server_id` VARCHAR(64) NOT NULL",
                "`name` VARCHAR(255) NOT NULL",
                "`icon` VARCHAR(255) NULL",
                "`custom_model_data` INT NOT NULL",
                "`alias_name` TEXT NULL",
                "`description` LONGTEXT NOT NULL",
                "`rates` LONGTEXT NOT NULL",
                "PRIMARY KEY (`server_id`, `name`)"
        });

        private final String name;
        private final String[] columns;

        DataTable(String name, String[] columns) {
            this.name = name;
            this.columns = columns;
        }
    }

    private final EnumMap<DataTable, NamedSQLTable> tables = new EnumMap<>(DataTable.class);
    private SQLManager manager;
    private String serverId;

    public MySQLStorage(EasyPlugin plugin) {
        super(plugin);
    }

    MySQLStorage(EasyPlugin plugin, SQLManager manager, String serverId, String prefix) throws SQLException {
        super(plugin);
        this.manager = manager;
        this.serverId = serverId;
        createTables(prefix);
    }

    @Override
    protected void openStorage() throws Exception {
        String prefix = PluginConfig.STORAGE.MYSQL.TABLE_PREFIX.getNotNull();
        if (!prefix.matches("[A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("MySQL table prefix must contain only letters, digits and underscores");
        }
        serverId = PluginConfig.STORAGE.MYSQL.SERVER_ID.get();
        if (serverId == null || serverId.isBlank()) {
            serverId = Integer.toString(plugin.getServer().getPort());
        }
        if (serverId.length() > 64) {
            throw new IllegalArgumentException("MySQL server ID must be 1 to 64 characters");
        }

        String database = PluginConfig.STORAGE.MYSQL.DATABASE.getNotNull();
        if (!database.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("MySQL database name must contain only letters, digits and underscores");
        }
        String url = "jdbc:mysql://" + PluginConfig.STORAGE.MYSQL.HOST.getNotNull()
                + ":" + PluginConfig.STORAGE.MYSQL.PORT.getNotNull()
                + "/" + database
                + "?useUnicode=true&characterEncoding=UTF-8&connectTimeout=5000&socketTimeout=10000"
                + "&rewriteBatchedStatements=true";
        manager = EasySQL.createManager("com.mysql.cj.jdbc.Driver", url,
                PluginConfig.STORAGE.MYSQL.USERNAME.getNotNull(),
                PluginConfig.STORAGE.MYSQL.PASSWORD.getNotNull());
        try {
            createTables(prefix);
        } catch (Exception exception) {
            EasySQL.shutdownManager(manager);
            manager = null;
            throw exception;
        }
    }

    private void createTables(String prefix) throws SQLException {
        for (DataTable value : DataTable.values()) {
            NamedSQLTable table = SQLTable.of(value.name, value.columns,
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            table.create(manager, prefix);
            tables.put(value, table);
        }
    }

    private NamedSQLTable table(DataTable value) {
        return tables.get(value);
    }

    @Override
    protected Map<String, ResidenceSnapshot> readResidences() throws Exception {
        return table(DataTable.RESIDENCES).createQuery()
                .addCondition("server_id", serverId)
                .build().executeFunction(query -> {
                    Map<String, ResidenceSnapshot> result = new LinkedHashMap<>();
                    ResultSet rows = query.getResultSet();
                    while (rows.next()) {
                        String iconName = rows.getString("icon");
                        Material icon = iconName == null ? null : Material.matchMaterial(iconName);
                        String name = rows.getString("name");
                        result.put(name, new ResidenceSnapshot(
                                icon,
                                rows.getInt("custom_model_data"),
                                rows.getString("alias_name"),
                                strings(rows.getString("description")),
                                rates(rows.getString("rates"))
                        ));
                    }
                    return result;
                }, Map.of());
    }

    @Override
    protected CachedUserData readUser(UUID key) throws Exception {
        return table(DataTable.USERS).createQuery()
                .addCondition("uuid", key.toString())
                .build().executeFunction(query -> {
                    ResultSet rows = query.getResultSet();
                    if (!rows.next()) return null;
                    return new CachedUserData(new UserSnapshot(
                            key,
                            strings(rows.getString("pinned")),
                            SortFunctions.parse(rows.getInt("sort")),
                            rows.getBoolean("reversed")
                    ));
                }, null);
    }

    @Override
    protected void writeUsers(List<UserSnapshot> users) throws Exception {
        if (users.isEmpty()) return;
        var action = table(DataTable.USERS).createReplaceBatch()
                .setColumnNames("uuid", "pinned", "sort", "reversed");
        for (UserSnapshot user : users) {
            action.addParamsBatch(user.key().toString(), json(user.pinned()),
                    user.sortFunction().ordinal(), user.reversed());
        }
        action.execute();
    }

    @Override
    protected void writeResidences(Map<String, ResidenceSnapshot> residences) throws Exception {
        if (residences.isEmpty()) return;
        var action = table(DataTable.RESIDENCES).createReplaceBatch()
                .setColumnNames("server_id", "name", "icon", "custom_model_data",
                        "alias_name", "description", "rates");
        for (Map.Entry<String, ResidenceSnapshot> entry : residences.entrySet()) {
            ResidenceSnapshot value = entry.getValue();
            action.addParamsBatch(serverId, entry.getKey(),
                    value.icon() == null ? null : value.icon().name(), value.customModelData(),
                    value.aliasName(), json(value.description()), jsonRates(value.rates()));
        }
        action.execute();
    }

    @Override
    protected void deleteResidence(String name) throws Exception {
        table(DataTable.RESIDENCES).createDelete()
                .addCondition(new String[]{"server_id", "name"}, new Object[]{serverId, name})
                .build().execute();
    }

    @Override
    protected void closeStorage() {
        if (manager != null) {
            EasySQL.shutdownManager(manager);
            manager = null;
        }
    }

    private static String json(List<String> values) {
        return new JSONArray(values).toString();
    }

    private static List<String> strings(String json) {
        List<String> values = new ArrayList<>();
        if (json == null || json.isBlank()) return values;
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) values.add(array.getString(i));
        return values;
    }

    private static String jsonRates(Map<UUID, ResidenceRate> rates) {
        JSONArray array = new JSONArray();
        rates.values().forEach(rate -> array.put(new JSONObject()
                .put("author", rate.author().toString())
                .put("content", rate.content())
                .put("recommend", rate.recommend())
                .put("time", rate.time().toString())));
        return array.toString();
    }

    private static Map<UUID, ResidenceRate> rates(String json) throws SQLException {
        Map<UUID, ResidenceRate> result = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return result;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                UUID author = UUID.fromString(object.getString("author"));
                result.put(author, new ResidenceRate(author,
                        object.getString("content"), object.getBoolean("recommend"),
                        LocalDateTime.parse(object.getString("time"))));
            }
            return result;
        } catch (RuntimeException exception) {
            throw new SQLException("Invalid residence rates JSON", exception);
        }
    }
}
