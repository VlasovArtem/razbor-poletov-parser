package org.avlasov.razborpoletov.reader.entity.statistic;

import org.avlasov.razborpoletov.reader.entity.User;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created By artemvlasov on 17/01/2018
 **/
public class UserStatisticTest {

    @Test
    public void constructor() {
        new UserStatistic(getUsers());
    }

    @Test(expected = NullPointerException.class)
    public void constructor_WithNullUsers_ThrownException() {
        new UserStatistic(null);
    }

    @Test
    public void getTotalUser() {
        assertEquals(1, new UserStatistic(getUsers()).getTotalUser());
    }

    @Test
    public void top5User() {
        assertThat(new UserStatistic(getUsers()).top5User(), IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void getAllUsers() {
        assertThat(new UserStatistic(getUsers()).getAllUsers(), IsCollectionWithSize.hasSize(1));
    }

    private List<User> getUsers() {
        return Collections.singletonList(User.builder().bio("test").build());
    }

}