package org.avlasov.razborpoletov.reader.service.impl;

import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.entity.statistic.UserStatistic;
import org.avlasov.razborpoletov.reader.parser.statistic.UserDataParser;
import org.avlasov.razborpoletov.reader.service.UserStatisticService;
import org.avlasov.razborpoletov.reader.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * Created by artemvlasov on 29/06/2017.
 */
@Service
public class UserStatisticServiceImpl implements UserStatisticService {

    private final UserDataParser userDataParser;
    private final FileUtils fileUtils;

    @Autowired
    public UserStatisticServiceImpl(UserDataParser userDataParser, FileUtils fileUtils) {
        this.userDataParser = userDataParser;
        this.fileUtils = fileUtils;
    }

    @Override
    public UserStatistic parseUserStatistic(List<File> files) {
        return userDataParser.getUserStatistic(files);
    }

    @Override
    public boolean saveJsonData(UserStatistic userStatistic) {
        return fileUtils.saveUserStatisticToFile(userStatistic);
    }

    @Override
    public boolean saveJsonData(List<User> users) {
        return fileUtils.saveTwitterCountInJsonToFile(users);
    }

    @Override
    public File saveData(UserStatistic userStatistic) {
        return null;
    }

    @Override
    public File saveData(List<User> users) {
        return null;
    }
}
