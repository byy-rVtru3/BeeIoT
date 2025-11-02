package jwtToken

import (
	"errors"
	"fmt"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

type Claim struct {
	Email string `json:"email"`
	jwt.RegisteredClaims
}

type JWTToken struct {
	secret string
}

func NewJWTToken() (*JWTToken, error) {
	secret, ok := os.LookupEnv("JWT_SECRET")
	if !ok {
		return nil, errors.New("JWT_SECRET environment variable is not set")
	}
	return &JWTToken{secret: secret}, nil
}

func (j *JWTToken) GenerateToken(email string) (string, error) {
	expirationTime := time.Now().Add(30 * 24 * time.Hour) // на месяц
	claim := &Claim{
		Email: email,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expirationTime),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			Issuer:    "admin",
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claim)
	return token.SignedString([]byte(j.secret))
}

func (j *JWTToken) ParseToken(tokenStr string) (string, error) {
	claim := &Claim{}
	token, err := jwt.ParseWithClaims(tokenStr, claim, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("неожиданный метод подписи")
		}
		return []byte(j.secret), nil
	})

	switch {
	case errors.Is(err, jwt.ErrTokenExpired):
		return "", jwt.ErrTokenExpired
	case err != nil:
		return "", fmt.Errorf("ошибка разбора токена: %w", err)
	case !token.Valid:
		return "", jwt.ErrTokenExpired
	default:
		return claim.Email, nil
	}
}
