// src/main/java/com/example/demo/service/AuthService.java
package com.example.demo.service;

import com.example.demo.dto.auth.RegisterRequestDTO;
import com.example.demo.entity.User;


public interface AuthService {


    User registerUser(RegisterRequestDTO registerRequest);
    // Có thể thêm các phương thức khác liên quan đến auth sau này

}