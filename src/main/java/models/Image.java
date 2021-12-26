package models;

import app.App;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class Image {

    private String id;
    private String fileId;
    private File file;
    private int width;
    private int height;
    private double longitude;
    private double latitude;
    private double altitude;
    private double fStop;
    private double focalLength;
    private double exposureTime;
    private int iso;
    private double aperture;
    private double digitalZoom;
    private double ev;
    private double shutterSpeed;
    private String creationTime;

    /**
     * Find an image in the database by its id
     * @param id of the image
     * @return the Image or null
     */
    public static Image find(String id) {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<Image> resultHandler = new BeanHandler<>(Image.class);
        Image image = null;
        try {
            image = qr.query(App.conn, "SELECT * FROM images WHERE id=?", resultHandler, id);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return image;
    }

    /**
     * Retrieve a list of all images in the database
     * @return List of Image objects
     */
    public static List<Image> findAll() {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<List<Image>> resultHandler = new BeanListHandler<>(Image.class);
        List<Image> image = null;
        try {
            image = qr.query(App.conn, "SELECT * FROM images", resultHandler);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return image;
    }

    /**
     * Given a file object, knowing it is an image, create a Image object.
     * @param file object describing the file containing the image
     */
    public Image(File file) throws ImageProcessingException, IOException, MetadataException {
        this.id = UUID.randomUUID().toString();
        this.file = file;
        this.fileId = file.getId();
        Metadata metadata = ImageMetadataReader.readMetadata(new java.io.File(file.getPath()));
        ExifSubIFDDirectory exifSubIFD = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        this.width = exifSubIFD.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
        this.height = exifSubIFD.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
        GeoLocation loc = gps.getGeoLocation();
        this.longitude = loc.getLongitude();
        this.latitude = loc.getLatitude();
        this.altitude = gps.getRational(GpsDirectory.TAG_ALTITUDE).doubleValue();
        this.fStop = exifSubIFD.getDouble(ExifSubIFDDirectory.TAG_FNUMBER);
        this.focalLength = exifSubIFD.getDouble(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
        this.exposureTime = exifSubIFD.getDouble(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
        this.iso = exifSubIFD.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
        this.aperture = exifSubIFD.getDouble(ExifSubIFDDirectory.TAG_APERTURE);
        this.digitalZoom = exifSubIFD.getDouble(ExifSubIFDDirectory.TAG_DIGITAL_ZOOM_RATIO);
        this.ev = exifSubIFD.getDouble(ExifSubIFDDirectory.TAG_EXPOSURE_BIAS);
        String shutterDescription = new ExifSubIFDDescriptor(exifSubIFD).getShutterSpeedDescription();
        this.shutterSpeed = Double.parseDouble(shutterDescription.split("/")[1].split(" ")[0]);
        this.creationTime = exifSubIFD.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL).toString();
    }

    public Image() {}

    public java.io.File thumbnail() {
        if (this.file == null) preloadFile();
        if (this.file == null) return null;
        return new java.io.File(this.file.getPath());
    }

    public void insert() throws SQLException {
        QueryRunner qr = new QueryRunner();
        qr.update(App.conn, "INSERT INTO images VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                id, fileId, width, height, longitude, latitude, altitude,
                fStop, focalLength, exposureTime, iso, aperture, digitalZoom,
                ev, shutterSpeed, creationTime);
    }

    /**
     * Load the file if it is not loaded yet
     * @return File object
     */
    public File preloadFile() {
        if (this.file != null) return this.file;
        if (this.fileId == null) return null;
        File file = File.find(this.fileId);
        this.file = file;
        return file;
    }

    public String toString() {
        return id + ", " + fileId + ", " + width + ", " + height + ", " + longitude + ", " + latitude + ", " + altitude + ", " +
                fStop + ", " + focalLength + ", " + exposureTime + ", " + iso + ", " + aperture + ", " + digitalZoom + ", " +
                ev + ", " + shutterSpeed + ", " + creationTime;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("fileId", fileId);
        json.put("width", width);
        json.put("height", height);
        json.put("longitude", longitude);
        json.put("latitude", latitude);
        json.put("altitude", altitude);
        json.put("fStop", fStop);
        json.put("focalLength", focalLength);
        json.put("exposureTime", exposureTime);
        json.put("iso", iso);
        json.put("aperture", aperture);
        json.put("digitalZoom", digitalZoom);
        json.put("ev", ev);
        json.put("shutterSpeed", shutterSpeed);
        json.put("creationTime", creationTime);
        return json;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getfStop() {
        return fStop;
    }

    public void setfStop(double fStop) {
        this.fStop = fStop;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(double focalLength) {
        this.focalLength = focalLength;
    }

    public double getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(double exposureTime) {
        this.exposureTime = exposureTime;
    }

    public int getIso() {
        return iso;
    }

    public void setIso(int iso) {
        this.iso = iso;
    }

    public double getAperture() {
        return aperture;
    }

    public void setAperture(double aperture) {
        this.aperture = aperture;
    }

    public double getDigitalZoom() {
        return digitalZoom;
    }

    public void setDigitalZoom(double digitalZoom) {
        this.digitalZoom = digitalZoom;
    }

    public double getEv() {
        return ev;
    }

    public void setEv(double ev) {
        this.ev = ev;
    }

    public double getShutterSpeed() {
        return shutterSpeed;
    }

    public void setShutterSpeed(double shutterSpeed) {
        this.shutterSpeed = shutterSpeed;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
}
