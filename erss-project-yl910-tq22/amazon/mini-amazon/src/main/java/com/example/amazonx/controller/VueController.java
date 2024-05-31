package com.example.amazonx.controller;

import com.example.amazonx.service.OrderService;
import com.example.amazonx.service.UserService;
import com.example.amazonx.model.Order;
import com.example.amazonx.model.Product;
import com.example.amazonx.model.User;
import com.example.amazonx.component.GlobalState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/")
public class VueController {

    private final GlobalState globalState;
    private final OrderService orderService;
    private final UserService userService;

    public VueController(GlobalState globalState
            , OrderService orderService
             , UserService userService
    ) {
        this.globalState = globalState;
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody Map<String, Object> registerData) {
        // Logic to save user in the database
        System.out.println("receiving registerUser!");
        String username = registerData.get("username").toString();
        String password = registerData.get("password").toString();
        if (userService.registerNewUserAccount(username, password)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, Object> loginData) {
        System.out.println("receiving login!");
        String username = loginData.get("username").toString();
        String password = loginData.get("password").toString();
        if (!userService.authenticate(username, password)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(username);

    }



    @PostMapping("/create-order")
    public ResponseEntity<Void> createOrder(@RequestBody Map<String, Object> orderData) {
        System.out.println("receiving createOrder!");
        if (!globalState.isConnected()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String upsUserId = orderData.get("upsUserId").toString();
            long productId = Long.parseLong(orderData.get("productId").toString());
            int quantity = Integer.parseInt(orderData.get("quantity").toString());
            int destX = Integer.parseInt(orderData.get("destX").toString());
            int destY = Integer.parseInt(orderData.get("destY").toString());
            String username = orderData.get("username").toString();
            System.out.println("productId: " + productId + " quantity: " + quantity + " destX: " + destX + " destY: " + destY);
            orderService.processOrder(upsUserId, productId, quantity, destX, destY
                    , username
            );
        } catch (Exception e) {
            System.out.println("Error: " + e);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/list-order")
    public ResponseEntity<List<Order>> listOrders(@RequestBody Map<String, Object> data) {
        System.out.println("receiving listOrders!");

        if (!globalState.isConnected()) {
            return ResponseEntity.badRequest().build();
        }
        String username = data.get("username").toString();
        return ResponseEntity.ok(orderService.listOrders(username));
    }

    @GetMapping("/list-product")
    public ResponseEntity<List<Product>> listProducts() {
        System.out.println("receiving listProducts!");
        if (!globalState.isConnected()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderService.listProducts());
    }
}
