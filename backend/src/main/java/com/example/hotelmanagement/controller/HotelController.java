package com.example.hotelmanagement.controller;

import com.example.hotelmanagement.model.MenuItem;
import com.example.hotelmanagement.model.TableOrder;
import com.example.hotelmanagement.model.dto.AddOrderItemRequest;
import com.example.hotelmanagement.model.dto.CreateTableRequest;
import com.example.hotelmanagement.service.HotelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/menu")
    public List<MenuItem> getMenu() {
        return hotelService.getMenu();
    }

    @PostMapping("/menu")
    public MenuItem addMenu(@RequestBody @Valid AddMenuItemRequest request) {
        return hotelService.addMenuItem(request.getName(), request.getPrice());
    }

    @GetMapping("/tables/active")
    public List<TableOrder> getActiveTables() {
        return hotelService.getActiveTables();
    }

    @PostMapping("/tables/start")
    public TableOrder startTable(@RequestBody @Valid CreateTableRequest request) {
        return hotelService.startTable(request.getTableNumber());
    }

    @PostMapping("/tables/{tableOrderId}/items")
    public TableOrder addItem(@PathVariable Long tableOrderId, @RequestBody @Valid AddOrderItemRequest request) {
        return hotelService.addOrderItem(tableOrderId, request.getMenuItemId(), request.getQuantity());
    }

    @PostMapping("/tables/{tableOrderId}/serve")
    public TableOrder serveTable(@PathVariable Long tableOrderId) {
        return hotelService.serveTable(tableOrderId);
    }

    @GetMapping("/tables/{tableOrderId}/bill")
    public TableOrder getBill(@PathVariable Long tableOrderId) {
        return hotelService.getBill(tableOrderId);
    }

    public static class AddMenuItemRequest {
        @NotBlank
        private String name;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
