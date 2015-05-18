package razborpoletov.reader.entity;

/**
 * Created by artemvlasov on 20/04/15.
 */
public class Twitter {
    private String account;
    private String accountUrl;

    public Twitter(String account, String accountUrl) {
        this.account = account;
        this.accountUrl = accountUrl;
    }

    public String getAccount() {
        return account;
    }

    public String getAccountUrl() {
        return accountUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Twitter)) return false;

        Twitter twitter = (Twitter) o;

        return !(account != null ? !account.equals(twitter.account) : twitter.account != null) && !(accountUrl != null ? !accountUrl.equals(twitter.accountUrl) : twitter.accountUrl != null);

    }

    @Override
    public int hashCode() {
        int result = account != null ? account.hashCode() : 0;
        result = 31 * result + (accountUrl != null ? accountUrl.hashCode() : 0);
        return result;
    }
}
