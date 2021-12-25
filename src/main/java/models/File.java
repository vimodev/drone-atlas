package models;

import app.App;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.json.simple.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Internal representation of a file in our index
 */
public class File {

    private String id;
    private String path;
    private String hash;
    private long size;

    /**
     * Find a File in the database with the given id
     * @param id of the file to search
     * @return the file
     */
    public static File find(String id) {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<File> resultHandler = new BeanHandler<>(File.class);
        File file = null;
        try {
            file = qr.query(App.conn, "SELECT * FROM files WHERE id=?", resultHandler, id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return file;
    }

    /**
     * Retrieve a list of all Files in the database
     * @return the list of File objects
     */
    public static List<File> findAll() {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<List<File>> resultHandler = new BeanListHandler<>(File.class);
        List<File> files = null;
        try {
            files = qr.query(App.conn, "SELECT * FROM files", resultHandler);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return files;
    }

    /**
     * Create a new File object based on the file at the path location
     * @param filePath the file's location
     */
    public File(Path filePath) throws IOException {
        this.id = UUID.randomUUID().toString();
        this.path = filePath.toString();
        this.hash = "none"; //DigestUtils.sha256Hex(new FileInputStream(this.path));
        this.size = Files.size(Paths.get(this.path));
    }

    /**
     * Empty constructor for Query Runner Bean Handler
     */
    public File() {}

    /**
     * Save this File object to the database
     * by inserting it as a new row
     * @throws SQLException if it fails
     */
    public void insert() throws SQLException {
        QueryRunner qr = new QueryRunner();
        String query = new StringBuilder()
                .append("INSERT INTO files ")
                .append("VALUES (")
                .append("'" + id + "',")
                .append("'" + path.replace("\\", "\\\\") + "',")
                .append("'" + hash + "',")
                .append(size)
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

    /**
     * Delete this File in the database
     * @throws SQLException if something goes wrong
     */
    public void delete() throws SQLException {
        QueryRunner qr = new QueryRunner();
        qr.update(App.conn, "DELETE FROM files WHERE id=?", this.id);
    }

    public String toString() {
        return id + ", " + path + ", " + hash + ", " + size;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("path", path);
        json.put("hash", hash);
        json.put("size", size);
        return json;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
