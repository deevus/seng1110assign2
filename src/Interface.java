import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

public class Interface {
  private static final int OPTION_BACK = 0;
  private static final int DATABASE_EMPTY = -1;
  private static final int PLAYLISTS_SIZE_INCREMENT = 4;

  private final Scanner console;
  private Playlist[] playlists;
  private final SongDatabase database;
  private int logicalSize;

  /*
   * Main entry for the program
   */
  public static void main(String[] args) {
    Interface intFace = new Interface();
    intFace.run();
    intFace.close();
  }

  /*
   * Starts the interface
   */
  private void run() {
    //welcome message
    System.out.println("Welcome to iToons Music");

    //options
    mainMenu();
  }

  /*
   * Interface constructor
   */
  public Interface() {
    console = new Scanner(System.in);
    database = new SongDatabase();
    playlists = new Playlist[PLAYLISTS_SIZE_INCREMENT];
    logicalSize = 0;
  }

  /*
   * Deconstructor method. Should be called when finished.
   */
  public void close() {
    console.close();
  }

  /*
    Used to display options provided through String[] parameter.
    Displays a default prompt
   */
  private int optionPrompt(String[] options) {
    return optionPrompt(options, "Choose from the following options:");
  }

  /*
    Used to display options provided through String[] parameter.
    Will use the provided prompt
   */
  private int optionPrompt(String[] options, String prompt) {
    System.out.println(prompt);
    for (String option : options) {
      System.out.println(option);
    }

    int choice = -1;
    while (choice < 0) {
      try {
        choice = Integer.parseInt(console.nextLine());
      } catch (Exception e) {
        System.out.println("Please enter a positive number.");
      }
    }

    return choice;
  }

  /*
   * Displays menu options for navigating and modifying
   * music database
   */
  private void mainMenu() {
    int option = -1;
    String[] options = {
        "[1]: Manage database",
        "[2]: Manage playlists",
        String.format("[%d]: Quit", OPTION_BACK)
    };

    //until "quit" is chosen, continue to show menu
    while (option != OPTION_BACK) {
      option = optionPrompt(options);

      //process option
      switch (option) {
        case 1: {
          manageDatabase();
          break;
        }
        case 2: {
          managePlaylists();
          break;
        }
        case OPTION_BACK: {
          continue;
        }
        default: {
          System.out.println("Unknown selection, please try again");
        }
      }
    }

    System.out.println("Thank you for using iToons music!");
  }

  /*
   * Removes a song (selected by the user) from the database
   */
  private void removeSongFromDatabase() {
    //get song to remove from user
    int songNum = selectSongFromDatabase(database, "Please select a song to remove:");
    if (songNum == -1) return;
    Song song = database.getSong(songNum);

    //song will be null if cancelled or incorrect choice
    if (song != null) {
      //set the song in the db to null
      database.removeSong(songNum);

      //then remove from any playlists
      for (int i = 0; i < logicalSize; i++) {
        Playlist pl = getPlaylist(i);

        int index;
        if (pl != null) {
          do {
            index = pl.indexOf(song);
            if (index >= 0) {
              pl.removeSong(index);
            }
          } while (index >= 0);
        }
      }

      System.out.println("Song deleted successfully.");
    }
  }

  /*
   * Removes a song (selected by the user) from the provided playlist
   */
  private void removeSongFromPlaylist(Playlist pl) {
    //get song from user
    int songNum = selectSong(pl);

    //song will be null if cancelled or incorrect choice
    if (pl.getSong(songNum) != null) {
      pl.removeSong(songNum);
      System.out.println("Song deleted successfully.");
    }
  }

  /*
   * Adds a new song (entered by the user) to the song database
   */
  private void addSongToDatabase() {
    //get song object from user
    Song song = getSongFromUser();

    //does this song already exist?
    for (int i = 0; i < database.getTotalSongs(); i++) {
      if (database.getSong(i).equals(song)) {
        System.out.println("Song already added to database.");
        return;
      }
    }

    //add to database
    database.addSong(song);
    System.out.println("Song added successfully");
  }

  /*
   * Starts the playlist management menu
   * All playlist management options selected here
   */
  private void managePlaylists() {
    //cannot manage playlists when there is no songs in the database
    int totalSongs = database.getTotalSongs();
    if (totalSongs == 0) {
      System.out.println("You need at least 1 song in the Song Database before you can manage playlists.");
      return;
    }

    String[] options = {
        "[1]: Create a playlist",
        "[2]: Manage existing playlist",
        "[3]: Remove a playlist",
        String.format("[%d]: Back", OPTION_BACK)
    };

    int option = -1;
    while (option != OPTION_BACK) {
      option = optionPrompt(options);

      switch (option) {
        case 1:
          Playlist pl = addPlaylist();
          if (pl != null) {
            managePlaylist(pl);
          }
          break;
        case 2:
          selectAndManagePlaylist();
          break;
        case 3:
          selectAndRemovePlaylist();
          break;
      }
    }
  }

  /*
    Prompts user to select a playlist
    Which they can then manage using the provided menu
   */
  private void selectAndManagePlaylist() {
    if (getTotalPlaylists() == 0) {
      System.out.println("There are no playlists in the system. You need to create one first.");
      return;
    }

    System.out.println("Select a playlist to manage:");
    int playlistNum = selectPlaylist();
    if (playlistNum < 0) {
      return;
    }
    Playlist playlist = getPlaylist(playlistNum);
    if (playlist != null) {
      managePlaylist(playlist);
    }
  }

  /*
   * Returns the amount of playlists currently in the system
   */
  private int getTotalPlaylists() {
    return logicalSize;
  }

  /*
    Attempts to add a new playlist
    Will return null if fails
  */
  private Playlist addPlaylist() {
    Playlist newPlaylist = new Playlist();

    //we need to expand the array ie. there are no spots left
    if (logicalSize >= playlists.length) {
      //get original size so we know where to put new song
      int origSize = playlists.length;

      //expand the array
      playlists = resizeArray(true, playlists);
      setPlaylist(origSize, newPlaylist);
    }
    //we have spots left. find the first empty spot and fill it
    else {
      int index = indexOfFirstEmptyPlaylist();
      setPlaylist(index, newPlaylist);
    }
    logicalSize++;

    System.out.println("Playlist created successfully.");
    return newPlaylist;
  }

  /*
    Finds index of first empty playlist in playlist array
   */
  private int indexOfFirstEmptyPlaylist() {
    //reverse loop because there will only be at most 4 empty slots at the end of the array
    for (int i = playlists.length - 1; i >= 0; i--) {
      if (playlists[i] != null) {
        return i + 1;
      }
    }

    return 0;
  }

  /*
    Resizes playlist array
   */
  private Playlist[] resizeArray(boolean expanding, Playlist[] arr) {
    if (expanding) {
      //increase size by set increment
      int newSize = arr.length + PLAYLISTS_SIZE_INCREMENT;

      //create new expanded array
      Playlist[] expandedArray = new Playlist[newSize];

      //copy old contents to new array
      System.arraycopy(arr, 0, expandedArray, 0, arr.length);

      return expandedArray;
    } else {
      //we are making the array smaller
      int halfSize = arr.length / 2;
      int newSize = halfSize > PLAYLISTS_SIZE_INCREMENT ? halfSize : PLAYLISTS_SIZE_INCREMENT;

      //create new contracted array
      Playlist[] smallArray = new Playlist[newSize];

      //copy old contents to new array
      System.arraycopy(arr, 0, smallArray, 0, smallArray.length);

      return smallArray;
    }
  }

  /*
   * Menu system for managing a given playlist
   */
  private void managePlaylist(Playlist pl) {
    String[] options = {
        "[1]: View Songs In Playlist",
        "[2]: Add Song To Playlist",
        "[3]: Remove Song From Playlist",
        "[4]: Remove Playlist",
        String.format("[%d]: Back", OPTION_BACK)
    };

    //display playlist options
    int option = -1;
    while (option != OPTION_BACK) {
      option = optionPrompt(options, "What would you like to do with this playlist?");

      switch (option) {
        case 1:
          //only attempt to print if playlist has songs
          if (pl.getTotalSongs() > 0) {
            printAllSongs(pl);
          } else {
            System.out.println("This playlist has no songs.");
          }
          break;
        case 2:
          addSongToPlaylist(pl);
          break;
        case 3:
          removeSongFromPlaylist(pl);
          break;
        case 4:
          removePlaylist(pl);
          return; //we can no longer manage this playlist as it's deleted
      }
    }
  }

  /*
   * Will attempt to add a song (which is selected by the user) to the given playlist
   */
  private void addSongToPlaylist(Playlist pl) {
    //song selection
    int songNum = selectSongFromDatabase(database, "Select a song to add to the playlist:");
    if (songNum < 0) {
      return;
    }
    Song song = database.getSong(songNum);

    //the enum PlaylistActionState informs us of the method result
    Playlist.PlaylistActionState result = pl.addSong(song);
    switch (result) {
      case NO_ERROR:
        break;
      case ERROR_MAX_TIME_REACHED: {
        System.out.println("Error: Could not add song. Maximum total time for playlist reached.");
        break;
      }
      case ERROR_MAX_SIZE_REACHED: {
        System.out.println("Error: Could not add song. Maximum file size for playlist reached");
        break;
      }
      default:
        System.out.println("Error: Unknown error");
        break;
    }
  }

  /*
   * Prompt the user to enter the details of a new song
   * Uses check methods to validate input
   */
  private Song getSongFromUser() {
    System.out.println("Please enter song details...");

    //get song name
    System.out.println("Name:");
    String name = getNameFromUser();

    //get song artist
    System.out.println("Artist:");
    String artist = getArtistFromUser();

    //get song duration
    System.out.println("Duration (seconds):");
    int duration = getDurationFromUser();

    //get song size
    System.out.println("File Size (kB):");
    int fileSize = getFileSizeFromUser();

    return new Song(name, artist, fileSize, duration);
  }

  private int getFileSizeFromUser() {
    int fileSize = -1;
    do try {
      fileSize = Integer.parseInt(console.nextLine());
    } catch (Exception e) {
      //do nothing
    } while (!checkFileSize(fileSize));
    return fileSize;
  }

  private int getDurationFromUser() {
    int duration = 0;
    do try {
      duration = Integer.parseInt(console.nextLine());
    } catch (Exception e) {
      //do nothing
    } while (!checkDuration(duration));
    return duration;
  }

  private String getArtistFromUser() {
    String artist;
    do {
      artist = console.nextLine();
    } while (!checkArtist(artist));
    return artist;
  }

  private String getNameFromUser() {
    String name;
    do {
      name = console.nextLine();
    } while (!checkName(name));
    return name;
  }

  /*
   * Prompts the user to select a playlist by index
   */
  private int selectPlaylist() {
    //display playlists
    String[] playlistOption = new String[logicalSize + 1];

    for (int i = 0; i < logicalSize; i++) {
      //get playlist object
      Playlist playlist = getPlaylist(i);

      //details string - will display playlist details, if no songs, then "Empty"
      String details = String.format("Songs: %d; Duration (seconds): %d; Size: %dKb", playlist.getTotalSongs(), playlist.getTotalTime(), playlist.getTotalSize());

      //print out option to console
      playlistOption[i] = String.format("[%d]: %s", i + 1, details);
    }

    playlistOption[logicalSize] = String.format("[%d]: Cancel", OPTION_BACK);
    return optionPrompt(playlistOption, "Choose a playlist:") - 1;
  }

  /*
   * Getter for playlist objects by a given index value
   */
  private Playlist getPlaylist(int num) {
    return playlists[num];
  }

  /*
   * Setter for playlist objects by a given index value
   */
  private void setPlaylist(int index, Playlist pl) {
    playlists[index] = pl;
  }

  //song validation methods
  private boolean checkName(String name) {
    if (name == null || name.length() == 0) {
      System.out.println("Name must have a value and cannot be null");
      return false;
    }
    return true;
  }

  private boolean checkArtist(String artist) {
    if (artist == null || artist.length() == 0) {
      System.out.println("Artist must have a value and cannot be null");
      return false;
    }
    return true;
  }

  private boolean checkFileSize(int fileSize) {
    if (fileSize <= 0) {
      System.out.println("File Size must be a number greater than zero");
      return false;
    }
    return true;
  }

  private boolean checkDuration(int duration) {
    if (duration <= 0) {
      System.out.println("Duration must be a number greater than zero");
      return false;
    }
    return true;
  }


  /*
   * Prompts the user to select a song from the given playlist
   *
   * Returns the index of the song which can be passed to playlist.getSong
   */
  private int selectSong(Playlist pl) {
    //exit method if there are no songs in the playlist
    if (pl.getTotalSongs() == 0) {
      System.out.println("There are no songs in the playlist.");
      return -1;
    }

    //display songs
    int totalSongs = pl.getTotalSongs();
    String[] songOptions = new String[totalSongs + 1];

    for (int i = 0; i < totalSongs; i++) {
      //get song object
      Song song = pl.getSong(i);
      if (song != null) {
        //print details of song to user
        songOptions[i] = songDetailsWithIndex(song, i + 1);
      }
    }

    songOptions[totalSongs] = String.format("[%d]: Cancel", OPTION_BACK);
    return optionPrompt(songOptions, "Select a song:") - 1;
  }

  /*
   * Display all songs to the user for a given playlist.
   */
  private void printAllSongs(Playlist pl) {
    for (int i = 0; i < pl.getTotalSongs(); i++) {
      //get song object
      Song song = pl.getSong(i);

      //display song details
      System.out.println(songDetailsWithIndex(song, i + 1));
    }
  }

  /*
    Gets String for printing a song to console
   */
  private String songDetailsWithIndex(Song song, int index) {
    return String.format("[%d] %s", index, song.toString());
  }

  /*
   * Prompts the user to select a song from the song database
   *
   * Returns the index of the song which can be passed to database.getSong
   */
  private int selectSongFromDatabase(SongDatabase db, String prompt) {
    String[] options = new String[db.getTotalSongs() + 1];

    //display songs
    int songCount = 0;
    for (int i = 0; i <= db.getTotalSongs() - 1; i++) {
      //get song object
      Song song = db.getSong(i);
      songCount++;

      options[i] = songDetailsWithIndex(song, songCount);
    }

    if (songCount == 0) {
      System.out.println("There are no songs in the database.");
      return DATABASE_EMPTY;
    }

    options[songCount] = String.format("[%d]: Cancel", OPTION_BACK);
    return optionPrompt(options, prompt) - 1;

  }

  /*
   * Lists the songs in the database by duration
   */
  private void listSongsByDuration() {
    if (database.getTotalSongs() == 0) {
      System.out.println("There are no songs in the database.");
      return;
    }

    int duration = getDurationCeiling();

    String[] filterOptions = {
        "[1]: Name",
        "[2]: Artist",
        "[3]: File Size",
        "[4]: Duration"
    };

    Song[] sortedSongs = null;
    while (sortedSongs == null) {
      int choice = optionPrompt(filterOptions, "Choose how you would like to order the results:");
      switch (choice) {
        case 1:
          sortedSongs = database.songsLessThanDurationByName(duration);
          break;
        case 2:
          sortedSongs = database.songsLessThanDurationByArtist(duration);
          break;
        case 3:
          sortedSongs = database.songsLessThanDurationByFileSize(duration);
          break;
        case 4:
          sortedSongs = database.songsLessThanDurationByDuration(duration);
          break;
        default:
          System.out.println("Invalid choice, please try again.");
      }
    }

    int count = 0;
    for (Song song : sortedSongs) {
      if (song != null) {
        count++;
        System.out.println(songDetailsWithIndex(song, count));
      }
    }

    if (count == 0) {
      System.out.println(String.format("No songs were found with a duration less than %d seconds.", duration));
    }
  }

  private int getDurationCeiling() {
    System.out.println("Enter max duration (in seconds) - only songs with a duration less than given will be shown:");
    int duration = -1;
    do {
      try {
        duration = Integer.parseInt(console.nextLine());
        if (duration <= 0) {
          System.out.println("Please enter a number greater than zero.");
        }
      } catch (Exception e) {
        //do nothing
      }
    } while (duration <= 0);
    return duration;
  }

  /*
   * Prompts user to select playlist which will then be removed
   */
  private void selectAndRemovePlaylist() {
    if (getTotalPlaylists() == 0) {
      System.out.println("Error: There are no playlists");
      return;
    }

    int i = selectPlaylist();
    if (i == -1) return;

    Playlist pl = getPlaylist(i);
    if (pl == null) {
      System.out.println("Incorrect selection.");
    } else {
      removePlaylist(pl);
    }
  }

  /*
   * Removes the given playlist.
   */
  private void removePlaylist(Playlist pl) {
    int index = indexOfPlaylist(pl);
    //set playlist to null
    playlists[index] = null;

    //update logicalSize
    logicalSize--;

    //move null index item to the end
    for (int i = index; i < playlists.length; i++) {
      if (playlists.length == i + 1 || playlists[i + 1] == null) {
        return;
      }

      playlists[i] = playlists[i + 1];
      playlists[i + 1] = null;
    }

    if (logicalSize < (playlists.length / 2)) {
      //resize the array
      playlists = resizeArray(false, playlists);
    }
    System.out.println("Playlist removed successfully.");
  }

  /*
   * Finds the index of a given playlist. Returns -1 if not found.
   */
  private int indexOfPlaylist(Playlist pl) {
    for (int i = 0; i < logicalSize; i++) {
      if (pl == getPlaylist(i)) {
        return i;
      }
    }

    return -1;
  }

  /*
    Prompts the user to choose a song database file
    Then loads it into database object
    Will only load new songs (duplicates will be ignored)
   */
  private void loadDatabaseFromFile() throws ClassNotFoundException, IOException {
    //list databases in directory
    File files = new File(System.getProperty("user.dir"));

    //we only want txt files shown
    FilenameFilter filter = new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".txt");
      }
    };

    //removes any files from the list that aren't txt
    File[] fileList = files.listFiles(filter);

    if (fileList.length == 0) {
      System.out.println("No databases found");
      return;
    }

    //get the selected database
    String[] options = new String[fileList.length];

    String format = "[%d] %s";
    for (int i = 0; i < fileList.length; i++) {
      options[i] = String.format(format, i + 1, fileList[i].getName());
    }
    int selection = optionPrompt(options, "Select a database to load:") - 1;

    //load it into a file object
    File selectedFile = fileList[selection];

    //pass into song database to load in
    int songsLoaded = database.loadSongs(selectedFile);
    if (songsLoaded > 0) {
      System.out.println(String.format("Loaded %d new songs from %s successfully", songsLoaded, selectedFile));
    } else {
      System.out.println("No new songs were loaded from the database.");
      System.out.println("Please note that duplicates will not be loaded.");
    }
  }

  /*
    Used to save song database to a file
    Will prompt user for filename
   */
  private void saveDatabaseToFile() {
    System.out.println("Enter name of database (extension will be added automatically):");
    String fileName = console.nextLine();

    //get the application path
    String appPath = System.getProperty("user.dir");
    //save to file in application directory
    try {
      String destination = String.format("%s/%s", appPath, fileName);
      boolean saveResult = database.saveToFile(destination);
      if (saveResult) {
        System.out.println(String.format("Database saved successfully to %s successfully", destination));
      }
    } catch (IOException e) {
      System.out.println("There was an error saving database to file: " + e.getMessage());
    }
  }

  /*
    Manage database
   */
  private void manageDatabase() {
    String[] options = {
        "[1]: Add a song",
        "[2]: Remove a song",
        "[3]: Edit a song",
        "[4]: Load from file",
        "[5]: Save to file",
        "[6]: List Songs By Duration",
        "[7]: List All Songs",
        String.format("[%d]: Back", OPTION_BACK)
    };

    int option = -1;
    while (option != OPTION_BACK) {
      option = optionPrompt(options);
      switch (option) {
        case 1:
          addSongToDatabase();
          break;
        case 2:
          removeSongFromDatabase();
          break;
        case 3:
          editSongInDatabase();
          break;
        case 4:
          try {
            loadDatabaseFromFile();
          } catch (Exception e) {
            System.out.println("Error loading database.");
          }
          break;
        case 5:
          saveDatabaseToFile();
          break;
        case 6:
          listSongsByDuration();
          break;
        case 7:
          listAllSongsInDatabase();
          break;
      }
    }
  }

  /*
    Edit a song in the database
   */
  private void editSongInDatabase() {
    int songChoice = selectSongFromDatabase(database, "Select which song you would like to edit:");
    if (songChoice == OPTION_BACK - 1) return;

    Song song = database.getSong(songChoice);
    String[] editOptions = {
        "[1]: Name",
        "[2]: Artist",
        String.format("[%d]: End Editing", OPTION_BACK)
    };

    int editChoice = -1;
    while (editChoice != OPTION_BACK) {
      editChoice = optionPrompt(editOptions, "What would you like to edit?");
      switch (editChoice) {
        case OPTION_BACK:
          return;
        case 1:
          System.out.println("Enter a new name:");
          String name = getNameFromUser();
          song.setName(name);
          System.out.println("Name updated successfully.");
          break;
        case 2:
          System.out.println("Enter new artist name:");
          String artist = getArtistFromUser();
          song.setArtist(artist);
          System.out.println("Artist updated successfully.");
          break;
      }
    }
  }

  /*
    Prints all the songs in the database to the console
    Ordered by index
   */
  private void listAllSongsInDatabase() {
    for (int i = 0; i < database.getTotalSongs(); i++) {
      System.out.println(songDetailsWithIndex(database.getSong(i), i + 1));
    }
  }
}