public class Playlist {
  private Song[] songs;
  private int logicalSize;

  /*
    Constants
    Values can be accessed outside using getters
  */
  private final int MAX_TIME = 60 * 25;
  private final int MAX_SIZE = 512;
  private final int SIZE_INCREMENT = 4;

  public Playlist() {
    songs = new Song[SIZE_INCREMENT];
    logicalSize = 0;
  }

  public enum PlaylistActionState {
    NO_ERROR,
    ERROR_MAX_TIME_REACHED,
    ERROR_MAX_SIZE_REACHED,
    ERROR_INVALID_INDEX
  }

  /*
    Add a song to a free spot
  */
  public PlaylistActionState addSong(Song song) {
    //does the song fit within the time constraints?
    if (getTotalTime() + song.getDuration() > MAX_TIME) {
      return PlaylistActionState.ERROR_MAX_TIME_REACHED;
    }

    //does the song fit within the size constraints?
    if (getTotalSize() + song.getFileSize() > MAX_SIZE) {
      return PlaylistActionState.ERROR_MAX_SIZE_REACHED;
    }

    //we need to expand the array ie. there are no spots left
    if (logicalSize >= songs.length) {
      //get original size so we know where to put new song
      int origSize = songs.length;

      //expand the array
      songs = resizeArray(true, songs);
      songs[origSize] = song;
    }
    //we have spots left. find the first empty spot and fill it
    else {
      int index = indexOfFirstEmpty();
      songs[index] = song;
    }

    logicalSize++;
    return PlaylistActionState.NO_ERROR;
  }

  /*
    Getters and Setters for private members
  */
  public PlaylistActionState setSong(int num, Song song) {
    try {
      songs[num] = song;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return PlaylistActionState.ERROR_INVALID_INDEX;
    }

    return PlaylistActionState.NO_ERROR;
  }

  /*
    Getter for songs in playlist
    Returns the song corresponding to the given index
    Returns null if invalid index
  */
  public Song getSong(int num) {
    try {
      return songs[num];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  /*
    Get the total size of songs in playlist
  */
  public int getTotalSize() {
    int totalSize = 0;
    for (int i = 0;i < logicalSize; i++) {
      totalSize += songs[i].getFileSize();
    }
    return totalSize;
  }

  /*
    Get the total time of songs in playlist
  */
  public int getTotalTime() {
    //todo: fix when deleting song from database when in playlist
    int totalTime = 0;
    for (int i = 0;i < logicalSize; i++) {
      totalTime += songs[i].getDuration();
    }
    return totalTime;
  }

  /*
    Get the total songs in the playlist
    ignoring nulls
  */
  public int getTotalSongs() {
    return logicalSize;
  }

  /*
    Getters for constants
    Could be changed to variable in future
  */
  public int getMaxSize() {
    return MAX_SIZE;
  }
  public int getMaxTime() {
    return MAX_TIME;
  }

  /*
    Gets the int index of a song in the playlist
    Returns -1 if not found
  */
  public int indexOf(Song song) {
    for (int i = 0; i < logicalSize; i++) {
      if (song == songs[i]) {
        return i;
      }
    }
    return -1;
  }

  /*
    Used to resize songs array
   */
  private Song[] resizeArray(boolean expanding, Song[] arr) {
    if (expanding) {
      //increase size by set increment
      int newSize = arr.length + SIZE_INCREMENT;

      //create new expanded array
      Song[] expandedArray = new Song[newSize];

      //copy old contents to new array
      System.arraycopy(arr, 0, expandedArray, 0, arr.length);

      return expandedArray;
    }
    else {
      //we are making the array smaller
      int halfSize = arr.length / 2;
      int newSize = halfSize > SIZE_INCREMENT ? halfSize : SIZE_INCREMENT;

      //create new contracted array
      Song[] smallArray = new Song[newSize];

      //copy old contents to new array
      System.arraycopy(arr, 0, smallArray, 0, smallArray.length);

      return smallArray;
    }
  }

  /*
   * Returns the first empty index in the songs array
   */
  private int indexOfFirstEmpty() {
    //reverse loop because there will only be at most 4 empty slots at the end of the array
    //this has a maximum of O(4) complexity
    //a forward loop would have O(n + 1) complexity
    for (int i = songs.length - 1; i >= 0; i--) {
      if (songs[i] != null) {
        return i + 1;
      }
    }

    return 0;
  }

  /*
   * Removes a song from the playlist
   */
  public void removeSong(int index) {
    //set song to null
    songs[index] = null;

    //update logicalSize
    logicalSize--;

    //move null index item to the end
    for (int i = index; i < songs.length; i++) {
      if (songs.length == i + 1 || songs[i + 1] == null) {
        return;
      }

      songs[i] = songs[i + 1];
      songs[i + 1] = null;
    }

    if (logicalSize < (songs.length / 2)) {
      //resize the array
      songs = resizeArray(false, songs);
    }
  }}