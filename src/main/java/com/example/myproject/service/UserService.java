package com.example.myproject.service;

import com.example.myproject.entity.User;
import com.example.myproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // 새로운 사용자를 생성하고 저장하는 메소드
    public User createUser(String id,
                           String rawPassword,
                           String name,
                           String email,
                           String auth,
                           String phone,
                           String address) {
        // 이미 존재하는 사용자 ID인지 확인
        if (userRepository.existsById(id)) {
            throw new RuntimeException("이미 존재하는 ID 입니다 : " + id);
        }

        User newUser = new User();
        newUser.setId(id);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setAuth(auth);
        newUser.setPhone(phone);
        newUser.setAddress(address);

        return userRepository.save(newUser);
    }

    // 사용자 ID의 중복 여부를 확인하는 메서드
    public boolean isIdDuplicate(String id) {
        return userRepository.existsById(id);
    }

    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }
}
