import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

public class Interface {
  private static final int OPTION_BACK = 0;
  private static final int DATABASE_EMPTY = -1;
  private static final int PLAYLISTS_SIZE_INCREMENT = 4;

  private Scanner console;
  private Playlist[] playlists;
  private SongDatabase database;
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
        choice = Integer.parseInt(console.next());
      } catch (Exception e) {
        System.out.println("Please enter a positive number.");
        continue;
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
  }

  /*
   * Removes a song (selected by the user) from the database
   */
  private void removeSongFromDatabase() {
    //get song to remove from user
    int songNum = selectSongFromDatabase(database);
    if (songNum == -1) return;
    Song song = database.getSong(songNum);

    //song will be null if cancelled or incorrect choice
    if (song != null) {
      //set the song in the db to null
      database.removeSong(songNum);

      //then remove from any playlists
      for (int i = 0; i < logicalSize; i++) {
        Playlist pl = getPlaylist(i);
        if (pl != null) {
          int index = pl.indexOf(song);
          if (index >= 0) {
            pl.removeSong(index);
          }
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
        case 1: {
          Playlist pl = addPlaylist();
          if (pl != null) {
            managePlaylist(pl);
          }
          break;
        }
        case 2: {
          if (getTotalPlaylists() == 0) {
            System.out.println("There are no playlists in the system. You need to create one first.");
            break;
          }

          System.out.println("Select a playlist to manage:");
          int playlistNum = selectPlaylist();
          if (playlistNum < 0) {
            break;
          }
          Playlist playlist = getPlaylist(playlistNum);
          if (playlist != null) {
            managePlaylist(playlist);
          }
          break;
        }
        case 3: {
          selectAndRemovePlaylist();
          break;
        }
        case OPTION_BACK: {
          continue;
        }
      }
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
        case 1: {
          //only attempt to print if playlist has songs
          if (pl.getTotalSongs() > 0) {
            printAllSongs(pl);
          } else {
            System.out.println("This playlist has no songs.");
          }
          break;
        }
        case 2:
          addSongToPlaylist(pl);
          break;
        case 3:
          removeSongFromPlaylist(pl);
          break;
        case 4: {
          removePlaylist(pl);
          return; //we can no longer manage this playlist as it's deleted
        }
        case OPTION_BACK:
          continue;
      }
    }
  }

  /*
   * Will attempt to add a song (which is selected by the user) to the given playlist
   */
  private void addSongToPlaylist(Playlist pl) {
    //song selection
    System.out.println("Select a song to add to the playlist:");
    int songNum = selectSongFromDatabase(database);
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
    //clear buffer
    console.nextLine();

    System.out.println("Please enter song details...");

    //get song name
    System.out.println("Name:");
    String name;
    do {
      name = console.nextLine();
    } while (!checkName(name));

    //get song artist
    System.out.println("Artist:");
    String artist;
    do {
      artist = console.nextLine();
    } while (!checkArtist(artist));

    //get song duration
    System.out.println("Duration:");
    int duration = 0;
    do try {
      duration = Integer.parseInt(console.next());
    } catch (Exception e) {
      continue;
    } while (!checkDuration(duration));

    //get song size
    System.out.println("File Size:");
    int fileSize = -1;
    do try {
      fileSize = Integer.parseInt(console.next());
    } catch (Exception e) {
      continue;
    } while (!checkFileSize(fileSize));

    return new Song(name, artist, fileSize, duration);
  }

  /*
   * Prompts the user to select a playlist by index
   */
  private int selectPlaylist() {
    //display playlists
    for (int i = 0; i < logicalSize; i++) {
      //get song object
      Playlist playlist = getPlaylist(i);
      if (playlist == null)
        continue;

      //details string - will display playlist details, if no songs, then "Empty"
      String details = String.format("Songs: %d; Duration: %d; Size: %dKb", playlist.getTotalSongs(), playlist.getTotalTime(), playlist.getTotalSize());

      //print out option to console
      System.out.printf("[%d]: %s\n", i + 1, details);
    }

    System.out.println(String.format("[%d]: Cancel", OPTION_BACK));
    return console.nextInt() - 1;
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
    for (int i = 0; i < pl.getTotalSongs(); i++) {
      //get song object
      Song song = pl.getSong(i);
      if (song != null) {
        //print details of song to user
        printSong(i + 1, song);
      }
    }

    System.out.println("[0]: Cancel");
    return console.nextInt() - 1;
  }

  /*
   * Display all songs to the user for a given playlist.
   */
  private void printAllSongs(Playlist pl) {
    for (int i = 0; i < pl.getTotalSongs(); i++) {
      //get song object
      Song song = pl.getSong(i);

      //display song details
      printSong(i + 1, song);
    }
  }

  /*
   * Print the details of a song to the console
   * The index will be displayed alongside, and may be used to identify the song
   */
  private void printSong(int index, Song song) {
    //details string - will display artist, track name, duration and file size
    int hours, minutes, seconds;
    hours = song.getDuration() / 3600;
    minutes = (song.getDuration() - hours * 3600) / 60;
    seconds = (song.getDuration() - hours * 3600 - minutes * 60);

    String details = String.format("%s - %s [%3$02d:%4$02d:%5$02d] (%6$dKb)", song.getArtist(), song.getName(), hours, minutes, seconds, song.getFileSize());

    //print out option to console
    System.out.printf("[%d]: %s\n", index, details);
  }

  /*
   * Prompts the user to select a song from the song database
   *
   * Returns the index of the song which can be passed to database.getSong
   */
  private int selectSongFromDatabase(SongDatabase db) {
    //display songs
    int songCount = 0;
    for (int i = 0; i <= db.getTotalSongs() - 1; i++) {
      //get song object
      Song song = db.getSong(i);

      //we don't want to display songs that are null
      if (song == null)
        continue;
      else songCount++;

      printSong(i + 1, song);
    }

    if (songCount == 0) {
      System.out.println("There are no songs in the database.");
      return DATABASE_EMPTY;
    }

    System.out.println(String.format("[%d]: Cancel", OPTION_BACK));
    return console.nextInt() - 1;
  }

  /*
   * Lists the songs in the database by duration
   */
  private void listSongsByDuration() {
    if (database.getTotalSongs() == 0) {
      System.out.println("There are no songs in the database.");
      return;
    }

    System.out.println("Enter duration ceiling (only songs with a duration less than the ceiling will be shown):");
    int duration = -1;
    do {
      try {
        duration = console.nextInt();
        if (duration <= 0) {
          System.out.println("Please enter a number greater than zero.");
        }
      } catch (Exception e) {
        continue;
      }
    } while (duration <= 0);

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
    for (int i = 0; i < sortedSongs.length; i++) {
      Song song = sortedSongs[i];
      if (song != null) {
        count++;
        printSong(count, sortedSongs[i]);
      }
    }

    if (count == 0) {
      System.out.printf("No songs were found with a duration less than %d seconds.\n", duration);
    }
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
    } else {
      System.out.println("Select a database file to load:");
    }

    //get the selected database
    String format = "[%d] %s\n";
    for (int i = 0; i < fileList.length; i++) {
      System.out.printf(format, i + 1, fileList[i].getName());
    }
    int selection = console.nextInt() - 1;

    //load it into a file object
    File selectedFile = fileList[selection];

    //pass into song database to load in
    int songsLoaded = database.loadSongs(selectedFile);
    if (songsLoaded > 0) {
      System.out.printf("Loaded %d new songs from %s successfully\n", songsLoaded, selectedFile);
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
    String fileName = console.next();

    //get the application path
    String appPath = System.getProperty("user.dir");
    //save to file in application directory
    try {
      String destination = String.format("%s/%s", appPath, fileName);
      boolean saveResult = database.saveToFile(destination);
      if (saveResult) {
        System.out.printf("Database saved successfully to %s successfully\n", destination);
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
        "[3]: Load from file",
        "[4]: Save to file",
        "[5]: List Songs By Duration",
        "[6]: List All Songs",
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
          try {
            loadDatabaseFromFile();
          } catch (Exception e) {
          }
          break;
        case 4:
          saveDatabaseToFile();
          break;
        case 5:
          listSongsByDuration();
          break;
        case 6:
          listAllSongsInDatabase();
          break;
        case OPTION_BACK:
          continue;
      }
    }
  }

  /*
    Prints all the songs in the database to the console
    Ordered by index
   */
  private void listAllSongsInDatabase() {
    for (int i = 0; i < database.getTotalSongs(); i++) {
      printSong(i + 1, database.getSong(i));
    }
  }
}