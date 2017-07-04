package org.avlasov.razborpoletov.reader.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class User implements Comparable {

    public final static User EMPTY_USER = new User();

    private String twitterAccount;
    private String twitterAccountUrl;
    private String name;
    private String twitterImgUrl;
    private long episodeNumberOfTheFirstAppearance;
    private String location;
    private String bio;
    private int totalAppearance;
    private List<Integer> appearanceEpisodeNumbers;

    private User() {
    }

    public User(String twitterAccount, String twitterAccountUrl, String name, String twitterImgUrl, String location, String bio, int episodeNumberOfTheFirstAppearance) {
        this.twitterAccount = twitterAccount;
        this.twitterAccountUrl = twitterAccountUrl;
        this.name = name;
        this.twitterImgUrl = twitterImgUrl;
        this.location = location;
        this.bio = bio;
        this.episodeNumberOfTheFirstAppearance = episodeNumberOfTheFirstAppearance;
        appearanceEpisodeNumbers = new ArrayList<>();
        appearanceEpisodeNumbers.add(episodeNumberOfTheFirstAppearance);
        totalAppearance = 1;
    }

    public void addAppearance() {
        totalAppearance++;
    }

    public void addEpisodeNumber(int number) {
        appearanceEpisodeNumbers.add(number);
    }

    public boolean isRequiredGuest(String accountURL) {
        return twitterAccountUrl.equals(accountURL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return twitterAccountUrl != null ? twitterAccountUrl.toLowerCase().equals(user.twitterAccountUrl.toLowerCase()) : user.twitterAccountUrl == null;

    }

    @Override
    public int hashCode() {
        return twitterAccountUrl != null ? twitterAccountUrl.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        content.append("------------------------").append("\n")
                .append("Twitter: ").append(twitterAccountUrl).append("\n")
                .append("Nickname: ").append(twitterAccount).append("\n")
                .append("Name: ").append(name).append("\n")
                .append("Img: ").append(twitterImgUrl).append("\n")
                .append("Location: ").append(location).append("\n")
                .append("Bio: ").append(bio).append("\n")
                .append("First Appearance (episode #): ").append(episodeNumberOfTheFirstAppearance).append("\n")
                .append("Appeared in: ").append(appearanceEpisodeNumbers).append("\n")
                .append("Total entries: ").append(totalAppearance).append("\n");
        return content.toString();
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(((User) o).totalAppearance, totalAppearance);
    }

    public String getTwitterAccount() {
        return twitterAccount;
    }
}
