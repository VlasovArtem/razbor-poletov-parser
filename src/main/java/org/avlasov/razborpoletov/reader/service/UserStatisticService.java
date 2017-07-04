package org.avlasov.razborpoletov.reader.service;

import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.entity.statistic.UserStatistic;

import java.io.File;
import java.util.List;

/**
 * Created by artemvlasov on 29/06/2017.
 */
public interface UserStatisticService {

    UserStatistic parseUserStatistic(List<File> files);
    boolean saveJsonData(UserStatistic userStatistic);
    boolean saveJsonData(List<User> users);
    File saveData(UserStatistic userStatistic);
    File saveData(List<User> users);

}
