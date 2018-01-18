package org.avlasov.razborpoletov.reader.converters;

/**
 * Created by artemvlasov on 04/06/15.
 */
public class TagsBuilder {

    public static String basicAsciidocElementBuilder(long podcastId,
                                                      long mp3FileLength,
                                                      String podcastTitle,
                                                      String podcastDate,
                                                      String mp3FileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("---").append("\n");
        sb.append("comments: true").append("\n");
        sb.append("categories:").append("\n");
        sb.append(String.format("uid: %d", podcastId)).append("\n");
        sb.append(String.format("length: %d", mp3FileLength)).append("\n");
        sb.append("---").append("\n");
        sb.append(String.format("= %s", podcastTitle)).append("\n");
        sb.append(String.format("%s", podcastDate)).append("\n");
        sb.append(":layout: post").append("\n");
        sb.append(String.format(":filename: %s", mp3FileName)).append("\n");
        return sb.toString();
    }

    public static String audioTagHtmlBuilder(String mp3Url) {
        StringBuilder sb = new StringBuilder();
        sb.append("++++\n");
        sb.append("<!-- player goes here-->\n");
        sb.append("<audio preload=\"none\">\n");
        sb.append(String.format("<source src=\"%s\" type=\"audio/mp3\" />\n", mp3Url));
        sb.append("Your browser does not support the audio tag.\n");
        sb.append("</audio>\n");
        sb.append("++++\n");
        return sb.toString();
    }
    public static String audioTagAsciidocBuilder(String mp3Url) {
        return String.format("audio::%s[]", mp3Url);
    }

    public static String downloadTagHtmlBuilder(String mp3Url) {
        StringBuilder sb = new StringBuilder();
        sb.append("++++\n");
        sb.append("<!-- episode file link goes here-->\n");
        sb.append(String.format("<a href=\"%s\" imageanchor=\"1\" " +
                "style=\"clear: left; margin-bottom: 1em; margin-left: auto; margin-right: 2em;\">\n", mp3Url));
        sb.append("<img border=\"0\" height=\"64\" src=\"http://2.bp.blogspot" +
                ".com/-qkfh8Q--dks/T0gixAMzuII/AAAAAAAAHD0/O5LbF3vvBNQ/s200/1330127522_mp3.png\" width=\"64\"/>\n");
        sb.append("</a>\n");
        sb.append("++++\n");
        return sb.toString();
    }

    public static String downloadTagAsciidocBuilder(String mp3Url) {
        return String.format("image::http://2.bp.blogspot" +
                ".com/-qkfh8Q--dks/T0gixAMzuII/AAAAAAAAHD0/O5LbF3vvBNQ/s200/1330127522_mp3" +
                ".png[link=\"%s\" width=\"64\" height=\"64\"]\n", mp3Url);
    }

    public static String imageTagHtmlBuilder(String imgUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("++++\n");
        sb.append("<div class=\"separator\" style=\"clear: both; text-align: center;\">\n");
        sb.append(String.format("<a href=\"%s\" imageanchor=\"1\" " +
                "style=\"margin-left: 1em; margin-right: 1em;\">\n", imgUrl));
        sb.append(String.format("<img border=\"0\" height=\"350\" src=\"%s\" width=\"350\" />\n", imgUrl));
        sb.append("</a>\n");
        sb.append("</div>\n");
        sb.append("++++\n");
        return sb.toString();
    }

    public static String imageTagAsciidocBuilder(String imgUrl) {
        return String.format("image::%s[width=\"350\" height=\"350\"" +
                " link=\"%s\" align=\"center\"]\n", imgUrl, imgUrl);
    }
}
