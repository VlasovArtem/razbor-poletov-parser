package org.avlasov.razborpoletov.reader.entity.statistic;

import org.avlasov.razborpoletov.reader.entity.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 25/06/2017.
 */
public class UserStatistic {

    private final List<User> allUsers;

    public UserStatistic(List<User> allUsers) {
        Objects.requireNonNull(allUsers);
        this.allUsers = allUsers;
    }

    public int getTotalUser() {
        return allUsers.size();
    }

    public List<User> top5User() {
        return allUsers.stream()
                .sorted()
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return allUsers;
    }
}
