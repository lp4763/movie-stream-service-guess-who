package Entities;

import java.util.Date;

public class Movie {
    private String title;
    private String studio;
    private String genre;
    private int userRatingCount;
    private float ratingAvgScore;
    private String mpaaRating;
    private int movieId;
    public Movie(String title, String  studio, String genre, int userRatingCount, float ratingAvgScore,
                 String mpaaRating, int movieId){
        this.title = title;
        this.studio = studio;
        this.genre = genre;
        this.userRatingCount = userRatingCount;
        this.ratingAvgScore = ratingAvgScore;
        this.mpaaRating = mpaaRating;
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }
    public String getGenre() {
        return genre;
    }
    public String getStudio() {
        return studio;
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
                ", studio='" + studio + '\'' +
                ", genre='" + genre + '\'' +
                ", userRatingCount=" + userRatingCount +
                ", ratingAvgScore=" + ratingAvgScore +
                ", mpaaRating='" + mpaaRating + '\'' +
                ", movieId=" + movieId +
                '}';
    }
}
