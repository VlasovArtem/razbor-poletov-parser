package org.avlasov.razborpoletov.reader.service.data.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.cli.Option;
import org.avlasov.razborpoletov.reader.PowerMockitoTestCase;
import org.avlasov.razborpoletov.reader.cli.ParserCommandLine;
import org.avlasov.razborpoletov.reader.cli.enums.CommandLineArgument;
import org.avlasov.razborpoletov.reader.entity.User;
import org.avlasov.razborpoletov.reader.parser.data.UserParser;
import org.avlasov.razborpoletov.reader.utils.PodcastFileUtils;
import org.avlasov.razborpoletov.reader.utils.PodcastFolderUtils;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * Created By artemvlasov on 06/01/2018
 **/
@PrepareForTest(value = { UserServiceDataImpl.class, PodcastFileUtils.class, PodcastFolderUtils.class })
public class UserServiceDataImplTest extends PowerMockitoTestCase {

    @Mock
    private PodcastFolderUtils folderUtils;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private File mockFile;
    @Mock
    private ParserCommandLine parserCommandLine;
    @Mock
    private UserParser userParser;
    @Mock
    private Option option;
    private UserServiceDataImpl userServiceData;

    @Before
    public void setUp() throws Exception {
        List<File> files = Collections.singletonList(mockFile);
        whenNew(File.class).withAnyArguments().thenReturn(mockFile);
        when(objectMapper.readValue(Mockito.any(File.class), Mockito.any(JavaType.class))).thenReturn(getList(getJsonData().build()));
        mockStatic(PodcastFileUtils.class);
        when(PodcastFileUtils.getPodcastNumber(Mockito.any(File.class))).thenReturn(Optional.of(160));
        when(folderUtils.getLastPodcastFile()).thenReturn(mockFile);
        when(folderUtils.getPodcastsFiles(Mockito.anyInt(), Mockito.anyInt())).thenReturn(files);
        when(parserCommandLine.getOption(CommandLineArgument.CREATORS_GUESTS)).thenReturn(Optional.of(option));
        when(option.getValue()).thenReturn("UPDATE");
        when(userParser.parse(Mockito.anyListOf(File.class))).thenReturn(getList(getNewUser().build(),
                getNewDataExistingTwitterId().build()));
        when(mockFile.exists()).thenReturn(true);
        when(objectMapper.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());
        doNothing().when(objectMapper).writeValue(any(File.class), any());
        userServiceData = new UserServiceDataImpl(userParser, this.objectMapper, parserCommandLine, folderUtils);
    }

    @Test
    public void parse_WithValidData_ReturnListOfUsers() {
        List<User> users = userServiceData.parse(Collections.singletonList(new File("test")));
        assertThat(users, IsCollectionWithSize.hasSize(2));
        Optional<User> first = users
                .stream()
                .filter(user -> user.getTwitterId() == 999)
                .findFirst();
        assertTrue(first.isPresent());
        assertThat(first.get().getAppearanceEpisodeNumbers(), IsCollectionWithSize.hasSize(3));
        assertEquals("Test New Bio", first.get().getBio());
    }

    @Test
    public void parse_WithInvalueCreatorsGuestArgument_ReturnLiftOfUsers() {
        when(option.getValue()).thenReturn("INVALID");
        List<User> users = userServiceData.parse(Collections.singletonList(new File("test")));
        assertThat(users, IsCollectionWithSize.hasSize(2));
    }

    @Test
    public void parse_WithAllUsersParsed_ReturnEmptyCollection() {
        when(PodcastFileUtils.getPodcastNumber(Mockito.any(File.class))).thenReturn(Optional.of(150));
        List<User> users = userServiceData.parse(Collections.singletonList(new File("test")));
        assertThat(users, IsEmptyCollection.empty());
    }

    @Test
    public void parse_WithCreatorsGuestsArgumentAll_ReturnUserCollection() {
        when(folderUtils.getAllPodcastFiles()).thenReturn(Collections.singletonList(mockFile));
        when(parserCommandLine.getOption(CommandLineArgument.CREATORS_GUESTS)).thenReturn(Optional.of(option));
        when(option.getValue()).thenReturn("ALL");
        List<User> parse = userServiceData.parse(Collections.emptyList());
        assertThat(parse, IsCollectionWithSize.hasSize(2));
    }

    @Test
    public void parse_WithEmptyJsonData_ReturnUserCollection() throws IOException {
        when(objectMapper.readValue(Mockito.any(File.class), Mockito.any(JavaType.class)))
                .thenReturn(Collections.emptyList());
        userServiceData = new UserServiceDataImpl(userParser, this.objectMapper, parserCommandLine, folderUtils);
        List<User> parse = userServiceData.parse(Collections.emptyList());
        assertThat(parse, IsCollectionWithSize.hasSize(2));
    }

    @Test(expected = RuntimeException.class)
    public void parse_WithCollectingJsonThrownException_ThrowException() throws IOException {
        when(objectMapper.readValue(Mockito.any(File.class), Mockito.any(JavaType.class)))
                .thenThrow(new IOException());
        new UserServiceDataImpl(userParser, this.objectMapper, parserCommandLine, folderUtils);
    }

    @Test
    public void saveJsonData_WithValidData_ReturnFile() {
        File file = userServiceData.saveJsonData(getList(getNewDataExistingTwitterId().build()));
        assertNotNull(file);
    }

    @Test
    public void saveJsonData_WithEmpty_ReturnFile() {
        File file = userServiceData.saveJsonData(getList());
        assertNotNull(file);
    }

    @Test(expected = RuntimeException.class)
    public void saveJsonData_WithModifyingFileException_ThrowException() throws IOException {
        doThrow(new IOException()).when(mockFile).createNewFile();
        userServiceData.saveJsonData(getList(getNewDataExistingTwitterId().build()));
    }

    @Test(expected = RuntimeException.class)
    public void saveJsonData_WithObjectMapperWriteValueThrownException_ThrowException() throws IOException {
        doThrow(new IOException()).when(objectMapper).writeValue(any(File.class), any());
        userServiceData.saveJsonData(getList(getNewDataExistingTwitterId().build()));
    }



    private User.UserBuilder getJsonData() {
        return User.builder()
                .bio("Test Bio")
                .location("Test Location")
                .twitterAccount("Test Account")
                .twitterAccountUrl("https://twitter.com/test")
                .twitterId(999)
                .name("Test Name")
                .twitterImgUrl("Test img ulr")
                .addEpisode(33).addEpisode(150);
    }

    private User.UserBuilder getNewDataExistingTwitterId() {
        return User.builder()
                .bio("Test New Bio")
                .location("Test New Location")
                .twitterAccount("Test New Account")
                .twitterAccountUrl("https://twitter.com/new_test")
                .twitterId(999)
                .name("Test New Name")
                .twitterImgUrl("Test new img ulr")
                .addEpisode(162);
    }

    private User.UserBuilder getNewUser() {
        return User.builder()
                .bio("New Bio 2")
                .location("New Location 2")
                .twitterAccount("New Account 2")
                .twitterAccountUrl("https://twitter.com/test2")
                .twitterId(888)
                .name("New Name 2")
                .twitterImgUrl("new img ulr 2")
                .addEpisode(162);
    }

    private List<User> getList(User... users) {
        return new ArrayList<>(Arrays.asList(users));
    }

}