package demo;

import com.mpatric.mp3agic.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class RenameTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenameTest.class);
    public static final String ARTIST = "artist";
    public static final String TITLE = "title";

    @Test
    public void rename_artist_from_title() throws Exception {
        Path folder = Paths.get("/", "media", "music");
        final List<Path> mp3Files = collectMp3Files(folder);

        for (Path path : mp3Files) {
            Map<String, String> data = parseData(path);
            String fileName = writeNewFile(path, data);

            File file = new File(fileName);
            assertTrue("file was written", file.exists());
            assertEquals("artist is corrected", data.get(ARTIST), readArtist(Paths.get(file.toURI())));
        }
    }

    private String readArtist(Path path) throws InvalidDataException, IOException, UnsupportedTagException {
        Mp3File mp3File = new Mp3File(path.toFile());
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        return id3v2Tag.getArtist();
    }

    private String writeNewFile(Path path, Map<String, String> data) throws IOException, NotSupportedException, InvalidDataException, UnsupportedTagException {
        Mp3File mp3File = new Mp3File(path.toFile());
        ID3v2 id3v2Tag = mp3File.getId3v2Tag();
        id3v2Tag.setTitle(data.get(TITLE));
        id3v2Tag.setArtist(data.get(ARTIST));

        StringBuilder newFile = new StringBuilder().append(path.getParent().toAbsolutePath().toString());
        // put in output folder
        newFile.append("/out/");
        //create dir
        File outputDirectory = new File(newFile.toString());
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }

        String track = id3v2Tag.getTrack(); //returns e.g. "1/22"
        String trackNumber = track.substring(0, track.indexOf('/'));

        newFile.append(String.format("%02d", Integer.valueOf(trackNumber)));
        newFile.append(". ");
        newFile.append(id3v2Tag.getArtist());
        newFile.append(" - ");
        newFile.append(id3v2Tag.getTitle());
        newFile.append(".mp3");

        mp3File.save(newFile.toString());

        return newFile.toString();
    }

    private Map<String, String> parseData(Path path) throws InvalidDataException, IOException, UnsupportedTagException {
        Map<String, String> resultMap = Collections.emptyMap();

        Mp3File mp3file = new Mp3File(path.toFile());
        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
        String info = id3v2Tag.getTitle();
        LOGGER.debug("{} read from id3v2.", info);

        String[] parsedElements = info.split("-");

        if (parsedElements.length > 0) {
            resultMap = new HashMap<>();
            resultMap.put(TITLE, parsedElements[0].trim());
            resultMap.put(ARTIST, parsedElements[1].trim());
        }

        return resultMap;
    }

    private List<Path> collectMp3Files(Path folder) throws IOException {
        List<Path> mp3Files = new ArrayList<>();
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".mp3")) {
                    mp3Files.add(file);
                    LOGGER.debug("file {} added.", file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return mp3Files;
    }
}
