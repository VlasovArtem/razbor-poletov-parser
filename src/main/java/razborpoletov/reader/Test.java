package razborpoletov.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by artemvlasov on 24/04/15.
 */
public class Test {
    //    public static void main(String[] args) throws IOException {
//        Location location = new Location();
//        location.setCity("Odessa");
//        location.setCountry("Ukraine");
//        location.setNameOfConferenceHall("Hall");
//        location.setStreet("Viliamsa 67");
//        File file = new File("podcast.json");
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.writeValue(file, location);
//    }
//    public static void main(String[] args) {
//        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
//        File folder = new File("/Users/artemvlasov/git/razbor-poletov.github.com/source/_posts/");
//        List<Content> contents = new ArrayList<>();
//        Document document = null;
//        for(File f : folder.listFiles()) {
//            FilenameUtils.getExtension(f.getAbsolutePath());
//            switch (FilenameUtils.getExtension(f.getAbsolutePath())) {
//                case "adoc":
//                    StructuredDocument doc = asciidoctor.readDocumentStructure(f, new HashMap<>());
//                    Content content = new Content();
//                    content.setContentPart(doc.getPartById("_Полезняшки"));
//                    String docname = (String) doc.getHeader().getAttributes().get("docname");
//                    String[] splited = docname.split("-");
//                    content.setDate(LocalDateTime.of(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]),
//                            Integer.valueOf(splited[2]), 0, 0));
//                    content.setEpisodeNumber(Integer.valueOf(splited[splited.length-1]));
//                    contents.add(content);
//                    break;
//            }
//        }
//        List<Podcast> podcasts = new ArrayList<>();
//        List<List<UsefulStuff>> usefulStuffsLists = new ArrayList<>();
//        for(Content content : contents) {
//            Podcast podcast = new Podcast();
//            List<UsefulStuff> usefulStuffs = new ArrayList<>();
//            ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
//            podcast.setId(content.getEpisodeNumber());
//            podcast.setDate(content.getDate());
//            if(content.getContentPart() != null) {
//                document = Jsoup.parse(content.getContentPart().getContent());
//                Elements elements = document.getElementsByTag("a");
//                for(Element element : elements) {
//                    UsefulStuff usefulStuff = new UsefulStuff();
//                    String name = element.textNodes().stream().findFirst().get().getWholeText();
//                    String url = element.attributes().get("href");
//                    if(name.equals(url)) {
//                        if(name.contains("http") || name.contains("https") || name.contains("www")) {
//                            if(name.contains("www.")) {
//                                name = name.substring(name.indexOf("www.") + "www." .length())
//                                        .split("\\.")[0];
//                            } else {
//                                name = name.substring(name.indexOf("://") + "://".length())
//                                        .split("\\.")[0];
//                            }
//                        }
//                    }
//                    if(!url.contains("razbor-poletov")) {
//                        usefulStuff.setProvider(name);
//                        usefulStuff.setLink(url);
//                        usefulStuffs.add(usefulStuff);
//                    }
//                }
//            }
//            podcast.setUsefulStuffs(usefulStuffs);
//            podcasts.add(podcast);
//            usefulStuffsLists.add(usefulStuffs);
//        }
//
//        try(FileOutputStream fos = new FileOutputStream(new File("useful-things.json"), true)) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.writeValue(fos, usefulStuffsLists);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try(FileOutputStream fos = new FileOutputStream(new File("podcast.json"), true)) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.writeValue(fos, podcasts);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//    public static class Content {
//        private int episodeNumber;
//        private LocalDateTime date;
//        private ContentPart contentPart;
//
//        public int getEpisodeNumber() {
//            return episodeNumber;
//        }
//
//        public void setEpisodeNumber(int episodeNumber) {
//            this.episodeNumber = episodeNumber;
//        }
//
//        public LocalDateTime getDate() {
//            return date;
//        }
//
//        public void setDate(LocalDateTime date) {
//            this.date = date;
//        }
//
//        public ContentPart getContentPart() {
//            return contentPart;
//        }
//
//        public void setContentPart(ContentPart contentPart) {
//            this.contentPart = contentPart;
//        }
//    }
//    public static void main(String[] args) {
//        final Logger logger = LoggerFactory.getLogger(Test.class);
//        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
//        File folder = new File("/Users/artemvlasov/git/razbor-poletov.github.com/source/_posts/");
//        List<Content> contents = new ArrayList<>();
//        Document document = null;
//        for(File f : folder.listFiles()) {
//            if(!f.getName().contains("episode")) {
//                continue;
//            }
//            FilenameUtils.getExtension(f.getName());
//            switch (FilenameUtils.getExtension(f.getAbsolutePath())) {
//                case "adoc":
//                    StructuredDocument doc = asciidoctor.readDocumentStructure(f, new HashMap<>());
//                    Content content = new Content();
//                    content.setContentPart(doc.getPartById("_Конференции"));
//                    String docname = (String) doc.getHeader().getAttributes().get("docname");
//                    String[] splited = docname.split("-");
//                    content.setDate(LocalDateTime.of(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]),
//                            Integer.valueOf(splited[2]), 0, 0));
//                    content.setEpisodeNumber(Integer.valueOf(splited[splited.length-1]));
//                    contents.add(content);
//                    break;
//            }
//        }
//        List<Podcast> podcasts = new ArrayList<>();
//        List<List<Conference>> conferencesLists = new ArrayList<>();
//        Set<String> uniqueConferenceWebsite = new HashSet<>();
//        List<String> ignoredUrls = Arrays.asList("instagram", "youtube");
//        for(Content content : contents) {
//            Podcast podcast = new Podcast();
//            List<Conference> conferences = new ArrayList<>();
//            podcast.setId(content.getEpisodeNumber());
//            podcast.setDate(content.getDate());
//            if(content.getContentPart() != null) {
//                document = Jsoup.parse(content.getContentPart().getContent());
//                Elements elements = document.getElementsByTag("a");
//                for(Element element : elements) {
//                    Conference conference = new Conference();
//                    String name = element.textNodes().stream().findFirst().get().getWholeText();
//                    String url = element.attributes().get("href");
//                    if(!uniqueConferenceWebsite.contains(url) && !ignoredUrls.contains(url)) {
//                        uniqueConferenceWebsite.add(url);
//                        if (name.equals(url)) {
//                            if (name.contains("http") || name.contains("https") || name.contains("www")) {
//                                if (name.contains("www.")) {
//                                    name = name.substring(name.indexOf("www.") + "www." .length())
//                                            .split("\\.")[0];
//                                } else {
//                                    name = name.substring(name.indexOf("://") + "://" .length())
//                                            .split("\\.")[0];
//                                }
//                            }
//                        }
//                        if (!url.contains("razbor-poletov")) {
//                            conference.setName(name);
//                            conference.setWebsite(url);
//                            conferences.add(conference);
//                        }
//                    }
//                }
//            }
//            podcast.setConferences(conferences);
//            podcasts.add(podcast);
//            conferencesLists.add(conferences);
//        }
//
//        try(FileOutputStream fos = new FileOutputStream(new File("conferences.json"))) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.writeValue(fos, conferencesLists);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try(FileOutputStream fos = new FileOutputStream(new File("podcast.json"))) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.writeValue(fos, podcasts);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//    public static class Content {
//        private int episodeNumber;
//        private LocalDateTime date;
//        private ContentPart contentPart;
//
//        public int getEpisodeNumber() {
//            return episodeNumber;
//        }
//
//        public void setEpisodeNumber(int episodeNumber) {
//            this.episodeNumber = episodeNumber;
//        }
//
//        public LocalDateTime getDate() {
//            return date;
//        }
//
//        public void setDate(LocalDateTime date) {
//            this.date = date;
//        }
//
//        public ContentPart getContentPart() {
//            return contentPart;
//        }
//
//        public void setContentPart(ContentPart contentPart) {
//            this.contentPart = contentPart;
//        }
//    }
    public static void main(String[] args) throws IOException, URISyntaxException {
        Logger logger = LoggerFactory.getLogger(Test.class);
//        final Logger logger = LoggerFactory.getLogger(Test.class);
//        File folder = new File("/Users/artemvlasov/git/razbor-poletov.github.com/source/_posts/");
//        Arrays.asList(folder.listFiles()).stream().forEach(System.out::println);
//        System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(folder.lastModified()), ZoneId.systemDefault()));
//        for(File file : folder.listFiles()) {
//            if(file.getName().contains("markdown")) {
//                String html = new Markdown4jProcessor().process(file);
//                if(html.contains("Полезняшка") || html.contains("Полезняшки")) {
//                    logger.info("Podcast {} contains usefulstuff", file.getName());
//                    Document document = Jsoup.parse(html);
//                    for(Element element : document.getElementsByTag("li")) {
//                        System.out.println(element.text());
//                    }
//                }
//            }
//        }
//        String outerUrl = "queues.io";
//        List<String> protocols = Arrays.asList("http://", "https://");
//        String formattedUrl = protocols.stream().anyMatch(outerUrl::contains) ? outerUrl :
//                String.format("http://%s", outerUrl);
//        List<String> tags = Arrays.asList("Java", "Web", "Framework", "WebSocket", "Gradle", "Maven", "Android",
//                "JSON", "JVM", "SQL", "Git", "Open source", "Apache");
//        URI uri = new URI(formattedUrl);
//        if(uri.isAbsolute()) {
//            URL innerUrl = uri.toURL();
//            URLConnection huc = (URLConnection) innerUrl.openConnection();
//            BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
//            StringBuilder builder = new StringBuilder();
//            String htmlLine;
//
//            while((htmlLine = br.readLine()) != null) {
//                builder.append(br.readLine());
//            }
//            LocalDateTime start = LocalDateTime.now();
//
//            tags.stream().filter(tag -> (builder.toString().contains(tag) || builder.toString().contains(tag
//                    .toLowerCase()) || builder.toString().contains(tag.toUpperCase()))).collect
//                    (Collectors
//                    .toList()).forEach(System.out::println);
//            LocalDateTime stop = LocalDateTime.now();
//            System.out.println("Parsing html page = " + Duration.between(start, stop).getNano());
//            System.out.println(builder.toString());
//
//        } else {
//            System.out.println(uri + " is not Absolute");
//        }
//        Scanner s = new Scanner(new File("tags.txt"));
//        List<String> tags = new ArrayList<>();
//        while (s.hasNext()) {
//            tags.add(s.next());
//        }
//        s.close();
//        tags.stream().forEach(System.out::println);
//        JsonParser parser = new JsonFactory().createParser(new File("duplicateTags.json"));
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, List<String>> duplicate = mapper.readValue(parser, new TypeReference<Map<String, List<String>>>() {});
//        System.out.println(duplicate);
//        List<String> tags = mapper.readValue(new File("tags.json"), new TypeReference<List<String>>() {});
//        System.out.println(tags);
//        String github = "https://github.com/";
//        System.out.println(github.lastIndexOf("/") + " " + github.length());
//        URL url = new URL("https://github.com/vladmihalcea/flexy-pool");
//        URLConnection connection = url.openConnection();
//        Document document = Jsoup.parse(connection.getInputStream(), null, "https://github.com/vladmihalcea/flexy-pool");
//        Elements elements = document.getElementsByClass("repository-description");
//        elements.stream().findFirst().toString();
//        System.out.println();

//        Pattern pattern = Pattern.compile("https?://github.com(/([^/])+){2}");
//        Matcher matcher = pattern.matcher("http://github.com/oblac/jodd");
//        System.out.println(matcher.find() ? matcher.group(0) : null);
//        Pattern pattern = Pattern.compile("(?:https?://github.com(/([^/])+){2})");
//        Matcher matcher = pattern.matcher("http://github.com/oblac/jodd/hello/man");
//        System.out.println(matcher.find() ? matcher.group(0) : null);
//        URL url = new URL("http://bitwiseshiftleft.github.com/sjcl/");
//        URLConnection huc = url.openConnection();
//        Document document = Jsoup.parse(huc.getInputStream(), null, url.toString());
//        Pattern pattern = Pattern.compile("(?:https?://github.com(/([^/])+){2})");
//        document.getElementsByTag("a").stream()
//                .filter(element -> element.attr("href").matches("https?://github.com.+"))
//                .map(el -> el.attr("href"))
//                .collect(Collectors.toSet())
//                .stream()
//                .map(test -> {
//                    final Matcher m =  pattern.matcher(test);
//                    return m.find() ? m.group(0) : null;})
//                .collect(Collectors.toSet()).stream().forEach(System.out::println);
//        List<String>
        Connection connection = Jsoup.connect("http://msgpack.org");
        Document document = connection.get();
        ObjectMapper mapper = new ObjectMapper();
        List<String> tags = mapper.readValue(new File("tags.json"), new TypeReference<List<String>>() {});
        Element body = document.body();
        tags.stream()
                .filter(tag -> body.getElementsMatchingOwnText(Pattern.compile(tag, Pattern.CASE_INSENSITIVE)).size() > 0)
                .distinct()
                .sorted(Comparator.comparing(tag -> -body.getElementsMatchingOwnText(tag).size()))
                .limit(5)
                .forEach(tag -> System.out.println(tag + ": " + body.getElementsMatchingOwnText(tag).size()));
        System.out.println("Matching text");
        System.out.println(body.getElementsMatchingText("Ruby").size());
        System.out.println("Matching own text");
        System.out.println(body.getElementsMatchingOwnText("Ruby").size());
        System.out.println("Containing own text");
        System.out.println(body.getElementsContainingOwnText("Ruby").size());
        System.out.println("Containing text");
        System.out.println(body.getElementsContainingText("Ruby").size());


//        BufferedReader br = new BufferedReader(new InputStreamReader(huc.getInputStream()));
//        StringBuilder builder = new StringBuilder();
//        String htmlLine;
//        while((htmlLine = br.readLine()) != null) {
//            builder.append(br.readLine());
//        }
//

//        Pattern pattern = Pattern.compile("\\b(" + StringUtils.join(tags, "|") + ")\\b");
//        Matcher m = pattern.matcher(builder.toString());
//        Set<String> uniqueTags = new HashSet<>();
//        while (m.find()) {
//            uniqueTags.add(m.group());
//        }
//        uniqueTags.stream().forEach(System.out::println);
    }

}
