package razborpoletov.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class PropertiesSelector {
    private final String propertiesPath;
    private Properties properties;
    private FileInputStream fis;
    private FileOutputStream fos;

    public PropertiesSelector(String propertiesPath) throws IOException {
        this.propertiesPath = propertiesPath;
        try {
            fis = new FileInputStream(propertiesPath);
            properties = new Properties();
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fis.close();
        }
    }
    public String getProperty(String property) {
        return properties.getProperty(property);
    }
    public void setProperty(String key, Object value) throws IOException {
        try {
            fos = new FileOutputStream(propertiesPath);
            properties.setProperty(key, String.valueOf(value));
            properties.store(fos, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }
}
