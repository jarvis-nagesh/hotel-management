package com.example.hotelmanagement.model.dto;

import jakarta.validation.constraints.Min;

public class CreateTableRequest {
    @Min(1)
    private int tableNumber;

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }
}
