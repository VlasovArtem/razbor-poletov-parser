package razborpoletov.reader.entity;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class Guest {
    private String twitterAccount;
    private String twitterAccountUrl;
    private String name;
    private String twitterImgUrl;
    private long episodeNumberOfTheFirstAppearance;
    private String location;
    private String bio;

    public Guest(String twitterAccount, String twitterAccountUrl) {
        this.twitterAccount = twitterAccount;
        this.twitterAccountUrl = twitterAccountUrl;
    }

    public Guest(String twitterAccount, String twitterAccountUrl, String name, String twitterImgUrl) {
        this.twitterAccount = twitterAccount;
        this.twitterAccountUrl = twitterAccountUrl;
        this.name = name;
        this.twitterImgUrl = twitterImgUrl;
    }

    public String getTwitterAccount() {
        return twitterAccount;
    }

    public void setTwitterAccount(String twitterAccount) {
        this.twitterAccount = twitterAccount;
    }

    public String getTwitterAccountUrl() {
        return twitterAccountUrl;
    }

    public void setTwitterAccountUrl(String twitterAccountUrl) {
        this.twitterAccountUrl = twitterAccountUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTwitterImgUrl() {
        return twitterImgUrl;
    }

    public void setTwitterImgUrl(String twitterImgUrl) {
        this.twitterImgUrl = twitterImgUrl;
    }

    public long getEpisodeNumberOfTheFirstAppearance() {
        return episodeNumberOfTheFirstAppearance;
    }

    public void setEpisodeNumberOfTheFirstAppearance(long episodeNumberOfTheFirstAppearance) {
        this.episodeNumberOfTheFirstAppearance = episodeNumberOfTheFirstAppearance;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest)) return false;

        Guest guest = (Guest) o;

        return twitterAccountUrl != null ? twitterAccountUrl.equals(guest.twitterAccountUrl) : guest.twitterAccountUrl == null;

    }

    @Override
    public int hashCode() {
        return twitterAccountUrl != null ? twitterAccountUrl.hashCode() : 0;
    }
}
