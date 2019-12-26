package com.org.tickes.entities;

import java.util.List;

public class TrainEntity {
    String trainNumber;
    List<String> seatNameList;

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public List<String> getSeatNameList() {
        return seatNameList;
    }

    public void setSeatNameList(List<String> seatNameList) {
        this.seatNameList = seatNameList;
    }
}
