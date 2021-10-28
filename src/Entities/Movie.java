package Entities;

import java.util.Date;

public class Movie {
    private String title;
    private int length;
    private String studio;
    private String genre;
    private String releaseDate;
    private int userRatingCount;
    private float ratingAvgScore;
    private String mpaaRating;
    private int movieId;
    public Movie(String title, int length, String  studio, String genre, String releaseDate,
                 int userRatingCount, float ratingAvgScore, String mpaaRating, int movieId){
        this.title = title;
        this.length = length;
        this.studio = studio;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.userRatingCount = userRatingCount;
        this.ratingAvgScore = ratingAvgScore;
        this.mpaaRating = mpaaRating;
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public int getLength() {
        return length;
    }

    public String getGenre() {
        return genre;
    }
    public String getStudio() {
        return studio;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getUserRatingCount() {
        return userRatingCount;
    }

    public float getRatingAvgScore() {
        return ratingAvgScore;
    }

    public String getMpaaRating() {
        return mpaaRating;
    }

    public int getMovieId() {
        return movieId;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", length=" + length +
                ", studio='" + studio + '\'' +
                ", genre='" + genre + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", userRatingCount=" + userRatingCount +
                ", ratingAvgScore=" + ratingAvgScore +
                ", mpaaRating='" + mpaaRating + '\'' +
                ", movieId=" + movieId +
                '}';
    }
}
