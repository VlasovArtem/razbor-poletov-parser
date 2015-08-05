package razborpoletov.reader.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by artemvlasov on 04/06/15.
 */
public class AsciidocConverter {
    private final static File PODCAST_FOLDER = new File("/Users/artemvlasov/git/razbor-poletov.github.com/source/_posts/");
    private final static Logger LOG = LoggerFactory.getLogger(AsciidocConverter.class);
//    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
//        String commandPattern = "pandoc -f %s -t asciidoc -o %s %s";
//        StatisticParser parser = new StatisticParser();
//        for (File file : Preconditions.checkNotNull(PODCAST_FOLDER.listFiles(), "File list folder is empty")) {
//            if(Pattern.matches("20([0-9]{2}-){3}episode-[0-9].+|(2013-02-03-episode33-aws-event\\.markdown)", file
//                    .getName())) {
//                String from;
//                String input = file.getAbsolutePath();
//                String[] command;
//                PodcastFile podcastFile = null;
//                switch (FilenameUtils.getExtension(file.getName())) {
//                    case Constants.MARKDOWN_FORMAT:
//                        podcastFile = new PodcastFile(file);
//                        from = "markdown";
//                        command = String.format(commandPattern, from, podcastFile.getOutputFile(), input).split(" ");
//                        executePandocCommand(command);
//                        podcastFile.setAsciidocWithoutHtml(prepareAsciidocStringConvertedFromMarkdown(
//                                podcastFile.getIntermediateConvertedFile(),
//                                podcastFile.getBasicElement(),
//                                podcastFile.getAsciidocAudioTag(),
//                                podcastFile.getAsciidocDonwloadTag(),
//                                podcastFile.getAsciidocImageTag()));
//                        podcastFile.setAsciidocWithHtml(prepareAsciidocStringConvertedFromMarkdown(
//                                podcastFile.getIntermediateConvertedFile(),
//                                podcastFile.getBasicElement(),
//                                podcastFile.getHtmlAudioTag(),
//                                podcastFile.getHtmlDonwloadTag(),
//                                podcastFile.getHtmlImageTag()));
//                        break;
//                    case Constants.HTML:
//                        podcastFile = new PodcastFile(file);
//                        from = "html";
//                        command = String.format(commandPattern, from, podcastFile.getOutputFile(), input).split(" ");
//                        executePandocCommand(command);
//                        podcastFile.setAsciidocWithoutHtml(prepareAsciidocStringConvertedFromHtml(
//                                podcastFile.getIntermediateConvertedFile(),
//                                podcastFile.getBasicElement(),
//                                podcastFile.getAsciidocAudioTag(),
//                                podcastFile.getAsciidocDonwloadTag(),
//                                podcastFile.getAsciidocImageTag()));
//                        podcastFile.setAsciidocWithHtml(prepareAsciidocStringConvertedFromHtml(
//                                podcastFile.getIntermediateConvertedFile(),
//                                podcastFile.getBasicElement(),
//                                podcastFile.getHtmlAudioTag(),
//                                podcastFile.getHtmlDonwloadTag(),
//                                podcastFile.getHtmlImageTag()));
//                        break;
//                    default:
//                        break;
//                }
//                if(podcastFile != null) {
//                    saveContentToFile(podcastFile.getAsciidocWithHtml(), podcastFile.getConvertedFileWithHtml());
//                    saveContentToFile(podcastFile.getAsciidocWithoutHtml(), podcastFile.getConvertedFileWithoutHtml());
//                }
//            }
//        }
//    }
//
//    private static String prepareAsciidocStringConvertedFromHtml(
//            File file,
//            String basicElement,
//            String audioTag,
//            String downloadTag,
//            String imageTag) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
//            int countBasicElement = 0;
//            for(String line; (line = br.readLine()) != null; ) {
//                if(line.contains("---") && countBasicElement < 2) {
//                    countBasicElement++;
//                } else if(countBasicElement == 2) {
//                    if(checkTagNotNull(basicElement, "basic element", file)) {
//                        sb.append(basicElement).append("\n");
//                    }
//                    if(checkTagNotNull(imageTag, "image", file)) {
//                        sb.append(imageTag).append("\n");
//                    }
//                    countBasicElement++;
//                } else if(countBasicElement >= 2) {
//                    if (line.matches("(\\[cols=\"\",\\])")) {
//                        if(checkTagNotNull(downloadTag, "download", file)) {
//                            sb.append(downloadTag).append("\n");
//                        }
//                    } else if (line.matches("(Your browser does not support the audio tag\\.)")) {
//                        if(checkTagNotNull(audioTag, "audioTag", file)) {
//                            sb.append(audioTag).append("\n");
//                        }
//                    } else if (!line.matches("^\\|.+")) {
//                        sb.append(line).append("\n");
//                    }
//                }
//
//            }
//        }
//        return sb.toString();
//    }
//    private static String prepareAsciidocStringConvertedFromMarkdown(
//            File file,
//            String basicElement,
//            String audioTag,
//            String downloadTag,
//            String imageTag) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        if(checkTagNotNull(basicElement, "base element", file)) {
//            sb.append(basicElement).append("\n");
//        }
//        if(checkTagNotNull(imageTag, "image", file)) {
//            sb.append(imageTag).append("\n");
//        }
//        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
//            for(String line; (line = br.readLine()) != null; ) {
//                if(line.matches("\\s?(Your browser does not support the audio tag\\.)")) {
//                    if(checkTagNotNull(audioTag, "audioTag", file)) {
//                        sb.append(audioTag).append("\n");
//                    }
//                    if(checkTagNotNull(downloadTag, "download", file)) {
//                        sb.append(downloadTag).append("\n");
//                    }
//                } else {
//                    sb.append(line).append("\n");
//                }
//
//            }
//        }
//        return sb.toString();
//    }
//    private static boolean checkTagNotNull(String tag, String tagName, File file) {
//        if(tag == null) {
//            LOG.info("File {} has no {} tag", FilenameUtils.getBaseName(file.getName()), tagName);
//            return false;
//        } else {
//            return true;
//        }
//    }
//    private static void executePandocCommand(String[] command) throws IOException, InterruptedException {
//        ProcessBuilder builder = new ProcessBuilder(command);
//        builder.redirectErrorStream(true);
//        Process p = builder.start();
//        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String line;
//        while (true) {
//            line = r.readLine();
//            if (line == null) {
//                break;
//            }
//            System.out.println(line + " " + command[command.length - 1]);
//        }
//        p.waitFor();
//    }
//    private static void saveContentToFile(String content, File file) throws IOException {
//        file.createNewFile();
//        try(FileOutputStream fos = new FileOutputStream(file)) {
//            fos.write(content.getBytes());
//        }
//    }
//    private static void saveBasicElementToFile(File podcastFile, File asciidocFile) throws IOException,
//            URISyntaxException {
//        StatisticParser parser = new StatisticParser();
//        long length = parser.getFileSize(podcastFile);
//        LocalDate ld = FileParser.getPodcastDate(podcastFile);
//        String convertedBasicElement = findBasicElementAsciidoc(asciidocFile);
//        long podcastId = FileParser.getPodcastId(podcastFile);
//        if(convertedBasicElement == null || ld == null) {
//            System.out.println(String.format("Basic element of the file %s is not exists", asciidocFile.getName()));
//        } else {
//            Pattern pattern = Pattern.compile("\".+\"");
//            Matcher matcher = pattern.matcher(convertedBasicElement);
//            String convertedDocumentTitle = null;
//            if(matcher.find()) {
//                convertedDocumentTitle = matcher.group().split("\"")[1];
//            }
//            String basicElement = String.format(
//                    basicAsciidocElementBuilder(),
//                    podcastId,
//                    length,
//                    convertedDocumentTitle,
//                    ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
//                    parser.getMp3Filename(podcastFile));
//            File file = new File(String.format("basic-elements/%s-basic.txt",
//                    FilenameUtils.getBaseName(podcastFile.getName())));
//            if(file.createNewFile()) {
//                try(FileOutputStream fos = new FileOutputStream(file)) {
//                    fos.write(basicElement.getBytes());
//                }
//            }
//        }
//    }
//private static String findBasicElementAsciidoc(File asciidocFile) {
//    Asciidoctor asciidoctor = Asciidoctor.Factory.create();
//    List<ContentPart> parts = asciidoctor.readDocumentStructure(asciidocFile, new HashMap<>()).getParts();
//    if(parts.isEmpty()) {
//        return null;
//    } else {
//        return parts.get(0).getContent();
//    }
//}
}
