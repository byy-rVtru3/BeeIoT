package middlewares

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/jwtToken"
	"context"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/rs/zerolog"
)

type MockInMemoryDB struct {
	interfaces.InMemoryDB
}

func (m *MockInMemoryDB) ExistJwt(ctx context.Context, email, jwtId string) (bool, error) {
	return true, nil
}

func TestCheckAuth(t *testing.T) {
	os.Setenv("JWT_SECRET", "testsecret")
	defer os.Unsetenv("JWT_SECRET")

	logger := zerolog.Nop()
	inMem := &MockInMemoryDB{}

	mw, err := NewMiddleWares(inMem, logger)
	if err != nil {
		t.Fatalf("NewMiddleWares failed: %v", err)
	}

	// Create a valid token
	jwtService, _ := jwtToken.NewJWTToken()
	token, _ := jwtService.GenerateToken("test@example.com")

	nextHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})

	handler := mw.CheckAuth(nextHandler)

	// Case 1: No header
	req := httptest.NewRequest("GET", "/", nil)
	w := httptest.NewRecorder()
	handler.ServeHTTP(w, req)
	if w.Code != http.StatusUnauthorized {
		t.Errorf("Expected 401 for no header, got %d", w.Code)
	}

	// Case 2: Invalid header format
	req = httptest.NewRequest("GET", "/", nil)
	req.Header.Set("Authorization", "InvalidFormat")
	w = httptest.NewRecorder()
	handler.ServeHTTP(w, req)
	if w.Code != http.StatusUnauthorized {
		t.Errorf("Expected 401 for invalid header, got %d", w.Code)
	}

	// Case 3: Valid token
	req = httptest.NewRequest("GET", "/", nil)
	req.Header.Set("Authorization", "Bearer "+token)
	w = httptest.NewRecorder()
	handler.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Errorf("Expected 200 for valid token, got %d", w.Code)
	}
}
