import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbutils.QueryRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Internal representation of a file in our index
 */
public class File {

    private UUID id;
    private Path path;
    private String hash;
    private long size;

    /**
     * Create a new File object based on the file at the path location
     * @param filePath the file's location
     */
    public File(Path filePath) throws IOException {
        this.id = UUID.randomUUID();
        this.path = filePath;
        this.hash = DigestUtils.sha256Hex(new FileInputStream(this.path.toString()));
        this.size = Files.size(this.path);
    }

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
                .append("'" + id.toString() + "',")
                .append("'" + path.toString() + "',")
                .append("'" + hash + "',")
                .append(size)
                .append(")")
                .toString();
        qr.update(App.conn, query);
    }

    public String toString() {
        return id.toString() + ", " + path.toString() + ", " + hash.toString() + ", " + size;
    }

    public UUID getId() {
        return id;
    }

    public Path getPath() {
        return path;
    }

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }
}
