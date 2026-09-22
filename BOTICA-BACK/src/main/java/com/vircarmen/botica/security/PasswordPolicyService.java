package com.vircarmen.botica.security;

import org.springframework.stereotype.Service;

import com.vircarmen.botica.exception.BusinessException;

@Service
public class PasswordPolicyService {
    public void validar(String password) {
        if (password == null || password.length() < 12
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)
                || password.chars().allMatch(Character::isLetterOrDigit)) {
            throw new BusinessException(
                    "La contraseña debe tener al menos 12 caracteres, mayúscula, minúscula, número y símbolo.");
        }
    }
}
