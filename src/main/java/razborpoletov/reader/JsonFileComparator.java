package razborpoletov.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import razborpoletov.reader.entity.UsefulThing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 30/04/15.
 */
public class JsonFileComparator {
    public static void createCompleteUsefulThinsFile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<UsefulThing> target = objectMapper.readValue(new File("new-useful-stuff.json"),
                new TypeReference<List<UsefulThing>>() {
                });
        List<UsefulThing> source = objectMapper.readValue(new File("old-useful-stuff.json"),
                new TypeReference<List<UsefulThing>>() {
                });
        List<UsefulThing> completeUsefulThings = new ArrayList<>();
        target = target.stream().filter(ut -> source.stream().anyMatch(ut1 -> Objects.equals(ut1
                .getLink(), ut.getLink()))).collect(Collectors.toList());
        for (int i = 0; i < target.size(); i++) {
            if(Objects.equals(target.get(i).getLink(), source.get(i).getLink())) {
                completeUsefulThings.add(setContent(target.get(i), source.get(i)));
            } else {
                for(UsefulThing sourceUT : source) {
                    if(Objects.equals(target.get(i).getLink(), sourceUT)) {
                        completeUsefulThings.add(setContent(target.get(i), sourceUT));
                    }
                }
            }
        }
        objectMapper.writeValue(new File("complete-useful-things.json"), completeUsefulThings);
    }
    private static UsefulThing setContent(UsefulThing target, UsefulThing source) {
        UsefulThing usefulThing = new UsefulThing();
        usefulThing.setDescription(target.getDescription() != null ? target
                .getDescription() : source.getDescription());
        usefulThing.setTags(target.getTags());
        usefulThing.setLink(target.getLink());
        usefulThing.setChecked(target.isChecked());
        usefulThing.setPodcastId(target.getPodcastId());
        usefulThing.setProvider(target.getProvider());
        return usefulThing;
    }
}
