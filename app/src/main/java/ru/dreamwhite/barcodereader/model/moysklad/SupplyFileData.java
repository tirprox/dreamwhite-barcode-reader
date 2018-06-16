package ru.dreamwhite.barcodereader.model.moysklad;

public class SupplyFileData {
    public String title = "";
    public int uniquePositions = 0, overallPositions = 0;

    public SupplyFileData(String title, int uniquePositions, int overallPositions) {
        this.title = title;
        this.uniquePositions = uniquePositions;
        this.overallPositions = overallPositions;
    }
}
