package com.example.currentplacedetailsonmap.controller;

/**
 * Created by Atlas on 2017-05-05.
 */

public class ScoreHandler {

    private int mGoodCount;
    private int mOkCount;
    private int mBadCount;
    private int mCurrentScore;
    private int mHighScore;
    private int mCurrentStreak;
    private int mHighestStreak;

    public ScoreHandler() {
        resetScores();
    }

    public void resetScores() {
        mGoodCount = mOkCount = mBadCount = mCurrentScore = mHighScore = mCurrentStreak = mHighestStreak = 0;
    }

    public int getGoodCount() {
        return mGoodCount;
    }

    public void setGoodCount(int mGoodCount) {
        this.mGoodCount = mGoodCount;
    }

    public void incrementGoodCount() {
        mGoodCount++;
    }

    public int getOkCount() {
        return mOkCount;
    }

    public void setOkCount(int mOkCount) {
        this.mOkCount = mOkCount;
    }

    public void incrementOkCount() {
        mOkCount++;
    }

    public int getBadCount() {
        return mBadCount;
    }

    public void setBadCount(int mBadCount) {
        this.mBadCount = mBadCount;
    }

    public void incrementBadCount() {
        mBadCount++;
    }

    public int getCurrentScore() {
        return mCurrentScore;
    }

    public void setCurrentScore(int mCurrentScore) {

        if (mCurrentScore < 0) {
            this.mCurrentScore = 0;
        } else {
            this.mCurrentScore = mCurrentScore;
        }
    }

    public int getHighScore() {
        return mHighScore;
    }

    public void setHighScore(int mHighScore) {
        this.mHighScore = mHighScore;
    }

    public int getCurrentStreak() {
        return mCurrentStreak;
    }

    public void setCurrentStreak(int mCurrentStreak) {
        this.mCurrentStreak = mCurrentStreak;
    }

    public void incrementCurrentStreak() {
        mCurrentStreak++;
    }

    public int getHigestStreak() {
        return mHighestStreak;
    }

    public void setmHighstreak(int mHighestStreak) {
        this.mHighestStreak = mHighestStreak;
    }

}
