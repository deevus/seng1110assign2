public class Song {
  //private fields
  private String name;
  private String artist;
  private int fileSize;
  private int duration;

  public static int numberOfSongs = 0;

  //constructors
  public Song() {
    numberOfSongs++;
  }

  public Song(String name, String artist, int fileSize, int duration) {
    this();

    setName(name);
    setArtist(artist);
    setFileSize(fileSize);
    setDuration(duration);
  }

  //name getter/setter
  public void setName(String value) {
    name = value;
  }

  public String getName() {
    return name;
  }

  //artist getter/setter
  public void setArtist(String value) {
    artist = value;
  }

  public String getArtist() {
    return artist;
  }

  //filesize getter/setter
  public void setFileSize(int value) {
    fileSize = value;
  }

  public int getFileSize() {
    return fileSize;
  }

  //duration getter/setter
  public void setDuration(int value) {
    duration = value;
  }

  public int getDuration() {
    return duration;
  }

  /*
    Songs are considered equal if song name and artist name matches (case insensitive)
   */
  public boolean equals(Song s) {
    return s.artist.compareToIgnoreCase(this.artist) + s.name.compareToIgnoreCase(this.name) == 0;
  }

  @Override
  public String toString() {
    //details string - will display artist, track name, duration and file size
    int hours, minutes, seconds;
    hours = this.getDuration() / 3600;
    minutes = (this.getDuration() - hours * 3600) / 60;
    seconds = (this.getDuration() - hours * 3600 - minutes * 60);

    return String.format("%s - %s [%3$02d:%4$02d:%5$02d] (%6$dKb)", this.getArtist(), this.getName(), hours, minutes, seconds, this.getFileSize());
  }
}