package org.avlasov.razborpoletov.reader.entity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class User implements Comparable {

    private long twitterId;
    private String twitterAccount;
    private String twitterAccountUrl;
    private String name;
    private String twitterImgUrl;
    private long episodeNumberOfTheFirstAppearance;
    private String location;
    private String bio;
    private int totalAppearance;
    private Set<Integer> appearanceEpisodeNumbers;

    public User() {
    }

    private User(long twitterId, String twitterAccount, String twitterAccountUrl, String name, String twitterImgUrl, long episodeNumberOfTheFirstAppearance, String location, String bio, int totalAppearance, Set<Integer> appearanceEpisodeNumbers) {
        this.twitterId = twitterId;
        this.twitterAccount = twitterAccount;
        this.twitterAccountUrl = twitterAccountUrl;
        this.name = name;
        this.twitterImgUrl = twitterImgUrl;
        this.episodeNumberOfTheFirstAppearance = episodeNumberOfTheFirstAppearance;
        this.location = location;
        this.bio = bio;
        this.totalAppearance = totalAppearance;
        this.appearanceEpisodeNumbers = appearanceEpisodeNumbers;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static UserBuilder builder(User source) {
        return new UserBuilder(source);
    }

    public Set<Integer> getAppearanceEpisodeNumbers() {
        return appearanceEpisodeNumbers;
    }

    public long getTwitterId() {
        return twitterId;
    }

    public String getTwitterAccount() {
        return twitterAccount;
    }

    public String getTwitterAccountUrl() {
        return twitterAccountUrl;
    }

    public String getName() {
        return name;
    }

    public String getTwitterImgUrl() {
        return twitterImgUrl;
    }

    public long getEpisodeNumberOfTheFirstAppearance() {
        return episodeNumberOfTheFirstAppearance;
    }

    public String getLocation() {
        return location;
    }

    public String getBio() {
        return bio;
    }

    public int getTotalAppearance() {
        return totalAppearance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return twitterId == user.twitterId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(twitterId);
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

    public static class UserBuilder {

        private String twitterAccount;
        private String twitterAccountUrl;
        private String name;
        private String twitterImgUrl;
        private String location;
        private String bio;
        private Set<Integer> appearanceEpisodeNumbers;
        private long twitterId;

        public UserBuilder() {
        }

        public UserBuilder(User source) {
            twitterAccount = source.twitterAccount;
            twitterAccountUrl = source.twitterAccountUrl;
            name = source.name;
            twitterImgUrl = source.twitterImgUrl;
            location = source.location;
            bio = source.bio;
            appearanceEpisodeNumbers = new HashSet<>(source.appearanceEpisodeNumbers);
            twitterId = source.twitterId;
        }

        public UserBuilder twitterId(long twitterId) {
            this.twitterId = twitterId;
            return this;
        }

        public UserBuilder twitterAccount(String twitterAccount) {
            this.twitterAccount = twitterAccount;
            return this;
        }

        public UserBuilder twitterAccountUrl(String twitterAccountUrl) {
            this.twitterAccountUrl = twitterAccountUrl;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder twitterImgUrl(String twitterImgUrl) {
            this.twitterImgUrl = twitterImgUrl;
            return this;
        }

        public UserBuilder location(String location) {
            this.location = location;
            return this;
        }

        public UserBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public UserBuilder episodes(Set<Integer> episodes) {
            this.appearanceEpisodeNumbers = episodes;
            return this;
        }

        public UserBuilder addEpisode(int episode) {
            if (Objects.isNull(appearanceEpisodeNumbers)) {
                appearanceEpisodeNumbers = new HashSet<>();
            }
            appearanceEpisodeNumbers.add(episode);
            return this;
        }

        public User build() {
            Set<Integer> episodes = Optional.ofNullable(appearanceEpisodeNumbers)
                    .orElseGet(Collections::emptySet)
                    .stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            int firstEpisode = episodes
                    .stream()
                    .mapToInt(Integer::intValue)
                    .min()
                    .getAsInt();
            return new User(twitterId, twitterAccount, twitterAccountUrl, name, twitterImgUrl, firstEpisode, location, bio, episodes.size(), episodes);
        }
    }

}
