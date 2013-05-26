import java.io.*;
import java.util.*;

public class SongDatabase {
	private static final int SIZE_INCREMENT = 4;
	
	private Song[] allSongs;
	private int logicalSize;

	/*
		Empty constructor
	*/
	public SongDatabase() {
		allSongs = new Song[SIZE_INCREMENT];
		logicalSize = 0;
	}

	/*
		Add a song to database
	*/
	public void addSong(Song song) {
		//we need to expand the array ie. there are no spots left
		if (logicalSize >= allSongs.length) {
			//get original size so we know where to put new song
			int origSize = allSongs.length;
			
			//expand the array
			allSongs = resizeArray(true, allSongs);
			allSongs[origSize] = song;
		}
		//we have spots left. find the first empty spot and fill it
		else {
			int index = indexOfFirstEmpty();
			allSongs[index] = song;
		}
		
		logicalSize++;
	}
	
	/*
	 * Returns the first empty index in the songs array
	 */
	private int indexOfFirstEmpty() {
		//reverse loop because there will only be at most 4 empty slots at the end of the array
		//this has a maximum of O(4) complexity
		//a forward loop would have O(n + 1) complexity
		for (int i = allSongs.length - 1; i >= 0; i--) {
			if (allSongs[i] != null) {
				return i + 1;
			}
		}
		
		return 0;
	}
	
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
		Mutator Methods
	*/
	public Song getSong(int num) {
		return allSongs[num];
	}

	/*
	 * Setter for songs given an index
	 */
	public void setSong(int index, Song song) {
		allSongs[index] = song;
	}
	
	/*
	 * Removes a song from the database
	 */
	public void removeSong(int index) {
		//set song to null
		allSongs[index] = null;
		
		//update logicalSize
		logicalSize--;

    //update numberOfSongs
    Song.numberOfSongs--;
		
		//move null index item to the end
		for (int i = index; i < allSongs.length; i++) {
			if (allSongs.length == i + 1 || allSongs[i + 1] == null) {
				return;
			}
			
			allSongs[i] = allSongs[i + 1];
			allSongs[i + 1] = null;
		}
		
		if (logicalSize < (allSongs.length / 2)) {
			//resize the array
			allSongs = resizeArray(false, allSongs);
		}
	}

	/*
	 * Gets the total amount of songs in the database
	 */
	public int getTotalSongs() {
		return logicalSize;
	}

	/*
		Load songs into array from given file
	*/
	public int loadSongs(File dataFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		Scanner fileScanner = new Scanner(dataFile);

    //confirm that we have a songdb file
    boolean isSongDatabase = fileScanner.nextLine().equals("SongDatabase File");
    if (!isSongDatabase) {
      throw new IOException("Given file is not a SongDatabase file.");
    }

		int count = 0;
		while (fileScanner.hasNextLine()) {
      if (fileScanner.nextLine().startsWith("Song")) {
        String songName = fileScanner.nextLine();
        String artistName = fileScanner.nextLine();

        if (contains(songName, artistName)) {
          continue;
        }

        String fileSizeStr = fileScanner.nextLine();
        int fileSize = Integer.parseInt(fileSizeStr.substring(0, fileSizeStr.indexOf('k')));
        int duration = Integer.parseInt(fileScanner.nextLine());

        Song newSong = new Song(songName, artistName, fileSize, duration);
        this.addSong(newSong);
        count++;
      }
		}
		fileScanner.close();
		return count;
	}

	/*
		Save database song objects to file
	*/
	public boolean saveToFile(String filePath) throws IOException {
		//does file path have right extension
		if (!filePath.endsWith(".txt"))
			filePath += ".txt";
		
		PrintWriter out = new PrintWriter(filePath);
		try {
      out.println("SongDatabase File");
			for (int i = 0; i < logicalSize; i++) {
				Song song = allSongs[i];
				String songString = String.format("Song %d\n%s\n%s\n%skB\n%d\n", i + 1, song.getName(), song.getArtist(), song.getFileSize(), song.getDuration());
				out.print(songString);
			}
		}
		finally {
			out.close();
		}
		
		return true;
	}

  /*
    Returns all songs in database that are shorter than the given duration.
    They will be sorted alphabetically
   */
	private Song[] songsLessThan(int duration, Comparator<Song> comparator) {
		if (logicalSize == 0) return new Song[0];

    //filter songs by duration
    Song[] matched = new Song[4];
    int matchedLogicalSize = 0;
    for (int i = 0; i < logicalSize; i++) {
      Song song = allSongs[i];
      if (song.getDuration() < duration) {
        if (matched.length == matchedLogicalSize) {
          matched = resizeArray(true, matched);
        }
        matched[matchedLogicalSize] = song;
        matchedLogicalSize++;
      }
    }

		/*
		 * Sort it
		 */
		Arrays.sort(matched, comparator);
		
		return matched;
	}

  private Comparator<Song> artistComparator() {
    Comparator<Song> songComparator = new Comparator<Song>() {
      public int compare(Song s1, Song s2) {
        if (s1 == null) {
          return -1;
        } else if(s2 == null) {
          return 1;
        }

        return s1.getArtist().compareToIgnoreCase(s2.getArtist());
      }
    };

    return songComparator;
  }

  private Comparator<Song> nameComparator() {
    Comparator<Song> songComparator = new Comparator<Song>() {
      public int compare(Song s1, Song s2) {
        if (s1 == null) {
          return -1;
        } else if(s2 == null) {
          return 1;
        }

        return s1.getName().compareToIgnoreCase(s2.getName());
      }
    };

    return songComparator;
  }

  private Comparator<Song> fileSizeComparator() {
    Comparator<Song> songComparator = new Comparator<Song>() {
      public int compare(Song s1, Song s2) {
        if (s1 == null) {
          return -1;
        } else if(s2 == null) {
          return 1;
        }

        if (s1.getFileSize() == s2.getFileSize()) return 0;
        if (s1.getFileSize() > s2.getFileSize()) return 1;

        return -1;
      }
    };

    return songComparator;
  }

  private Comparator<Song> durationComparator() {
    Comparator<Song> songComparator = new Comparator<Song>() {
      public int compare(Song s1, Song s2) {
        if (s1 == null) {
          return -1;
        } else if(s2 == null) {
          return 1;
        }

        if (s1.getDuration() == s2.getDuration()) return 0;
        if (s1.getDuration() > s2.getDuration()) return 1;

        return -1;
      }
    };

    return songComparator;
  }

  /*
    Checks if given song is in the database
   */
  public boolean contains(String name, String artist) {
    for (int i = 0; i < logicalSize; i++) {
      Song song = allSongs[i];
      if (song.getName().equals(name) && song.getArtist().equals(artist)) {
        return true;
      }
    }
    return false;
  }

  public Song[] songsLessThanDurationByName(int duration) {
    return songsLessThan(duration, nameComparator());
  }

  public Song[] songsLessThanDurationByArtist(int duration) {
    return songsLessThan(duration, artistComparator());
  }

  public Song[] songsLessThanDurationByFileSize(int duration) {
    return songsLessThan(duration, fileSizeComparator());
  }

  public Song[] songsLessThanDurationByDuration(int duration) {
    return songsLessThan(duration, durationComparator());
  }
}