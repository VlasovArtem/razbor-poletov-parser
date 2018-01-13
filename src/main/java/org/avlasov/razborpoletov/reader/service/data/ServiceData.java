package org.avlasov.razborpoletov.reader.service.data;

import java.io.File;
import java.util.List;

/**
 * Created By artemvlasov on 05/01/2018
 **/
public interface ServiceData<T> {

    List<T> parse(File file);
    List<T> parse(List<File> files);
    File saveJsonData(List<T> data);
    File saveStringData(List<T> data);

}
