package org.avlasov.razborpoletov.reader.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.avlasov.razborpoletov.reader.parser.data.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created By artemvlasov on 05/01/2018
 **/
public abstract class ServiceDataAbstract<T, R extends Parser<T>> implements ServiceData<T> {

    private final static Logger LOGGER = LogManager.getLogger(ServiceDataAbstract.class);
    protected ObjectMapper objectMapper;
    protected R parser;

    public ServiceDataAbstract(R parser, ObjectMapper objectMapper) {
        this.parser = parser;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<T> parse(List<File> files) {
        return parser.parse(files);
    }

    @Override
    public List<T> parse(File file) {
        return parser.parse(file);
    }

    @Override
    public File saveJsonData(List<T> data) {
        File json = createFileToSave("json");
        if (!data.isEmpty()) {
            try {
                objectMapper.writeValue(json, data);
            } catch (IOException e) {
                LOGGER.error(e);
                throw new RuntimeException(e);
            }
        }
        return json;
    }

    @Override
    public File saveStringData(List<T> data) {
        String dataToSave = Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .parallelStream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        File file = createFileToSave("txt");
        try {
            FileUtils.writeStringToFile(file, dataToSave, Charset.defaultCharset());
        } catch (Exception e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
        return file;
    }

    protected abstract String getFileName();

    /**
     * Get File path with provided extension
     *
     * @param fileExtension file extension
     * @return filename;
     */
    protected String getFilePath(String fileExtension) {
        return String.format("./data/%s.%s",
                getFileName(),
                fileExtension.startsWith("\\.") ? fileExtension.replaceFirst("\\.", "") : fileExtension);
    }

    /**
     * Create new file with provided extension, file will be deleted and then recreated.
     *
     * @param fileExtension File extension
     * @return File
     */
    private File createFileToSave(String fileExtension) {
        try {
            File file = new File(getFilePath(fileExtension));
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            return file;
        } catch (Exception e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

}
