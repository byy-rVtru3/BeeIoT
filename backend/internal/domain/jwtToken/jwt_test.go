package jwtToken

import (
	"os"
	"testing"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

func TestNewJWTToken(t *testing.T) {
	os.Unsetenv("JWT_SECRET")
	_, err := NewJWTToken()
	if err == nil {
		t.Error("Expected error when JWT_SECRET is not set")
	}

	os.Setenv("JWT_SECRET", "testsecret")
	defer os.Unsetenv("JWT_SECRET")

	jwtService, err := NewJWTToken()
	if err != nil {
		t.Fatalf("NewJWTToken failed: %v", err)
	}
	if jwtService == nil {
		t.Fatal("jwtService should not be nil")
	}
}

func TestGenerateAndParseToken(t *testing.T) {
	os.Setenv("JWT_SECRET", "testsecret")
	defer os.Unsetenv("JWT_SECRET")

	jwtService, _ := NewJWTToken()
	email := "test@example.com"

	token, err := jwtService.GenerateToken(email)
	if err != nil {
		t.Fatalf("GenerateToken failed: %v", err)
	}
	if token == "" {
		t.Error("Token should not be empty")
	}

	parsedEmail, err := jwtService.ParseToken(token)
	if err != nil {
		t.Fatalf("ParseToken failed: %v", err)
	}

	if parsedEmail != email {
		t.Errorf("Expected email %s, got %s", email, parsedEmail)
	}
}

func TestParseInvalidToken(t *testing.T) {
	os.Setenv("JWT_SECRET", "testsecret")
	defer os.Unsetenv("JWT_SECRET")

	jwtService, _ := NewJWTToken()

	_, err := jwtService.ParseToken("invalid-token")
	if err == nil {
		t.Error("Expected error for invalid token")
	}
}

func TestExpiredToken(t *testing.T) {
	secret := "testsecret"
	os.Setenv("JWT_SECRET", secret)
	defer os.Unsetenv("JWT_SECRET")

	jwtService, _ := NewJWTToken()

	// Manually create an expired token
	expirationTime := time.Now().Add(-1 * time.Hour)
	claim := &Claim{
		Email: "test@example.com",
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expirationTime),
			IssuedAt:  jwt.NewNumericDate(time.Now().Add(-2 * time.Hour)),
			Issuer:    "admin",
		},
	}
	tokenObj := jwt.NewWithClaims(jwt.SigningMethodHS256, claim)
	tokenString, _ := tokenObj.SignedString([]byte(secret))

	_, err := jwtService.ParseToken(tokenString)
	if err != jwt.ErrTokenExpired {
		t.Errorf("Expected ErrTokenExpired, got %v", err)
	}
}
