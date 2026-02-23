package handlers

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/httpType"
	"BeeIOT/internal/domain/passwords" // Added import
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/rs/zerolog"
)

func TestReadBodyJSON(t *testing.T) {
	h := &Handler{logger: zerolog.Nop()}

	body := []byte(`{"start_date": "2023-01-01"}`)
	req := httptest.NewRequest("POST", "/test", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	var data httpType.QueenRequest
	if err := h.readBodyJSON(w, req, &data); err != nil {
		t.Fatalf("readBodyJSON failed: %v", err)
	}

	if data.StartDate != "2023-01-01" {
		t.Errorf("Expected 2023-01-01, got %s", data.StartDate)
	}
}

func TestWriteBodyJSON(t *testing.T) {
	h := &Handler{logger: zerolog.Nop()}
	w := httptest.NewRecorder()

	h.writeBodyJSON(w, "success", map[string]string{"key": "value"})

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected status 200, got %d", resp.StatusCode)
	}

	var response Response
	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		t.Fatalf("Failed to decode response: %v", err)
	}

	if response.Status != "ok" {
		t.Errorf("Expected status ok, got %s", response.Status)
	}
	if response.Message != "success" {
		t.Errorf("Expected message success, got %s", response.Message)
	}
}

func TestRegistration(t *testing.T) {
	// Setup env for JWT
	t.Setenv("JWT_SECRET", "testsecret")

	logger := zerolog.Nop()
	mockDB := &MockDB{ExistUserResult: false}
	mockSender := &MockConfirmSender{}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, _ := NewHandler(mockDB, mockSender, mockInMem, nil, mockPasswordKeeper, logger)

	// Case 1: Successful registration (confirmation code sent)
	body := []byte(`{"email": "new@test.com", "password": "password"}`)
	req := httptest.NewRequest("POST", "/api/auth/registration", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	h.Registration(w, req)

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected status 200, got %d", resp.StatusCode)
	}

	if mockSender.LastEmail != "new@test.com" {
		t.Errorf("Confirmation code email mismatch")
	}

	// Case 2: User already exists
	mockDB.ExistUserResult = true
	req = httptest.NewRequest("POST", "/api/auth/registration", bytes.NewBuffer(body))
	w = httptest.NewRecorder()

	h.Registration(w, req)
	if w.Result().StatusCode != http.StatusConflict {
		t.Errorf("Expected 409 for existing user, got %d", w.Result().StatusCode)
	}
}

func TestLogin(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")

	logger := zerolog.Nop()
	mockDB := &MockDB{ExistUserResult: true}
	mockInMem := &MockInMemoryDB{}

	// Setup Handler
	h, _ := NewHandler(mockDB, nil, mockInMem, nil, nil, logger)

	// Prepare hashed password
	hashedPassword, _ := passwords.HashPassword("password123")
	mockDB.LoginPassword = hashedPassword

	// Case 1: Successful login
	body := []byte(`{"email": "test@example.com", "password": "password123"}`)
	req := httptest.NewRequest("POST", "/api/auth/login", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	h.Login(w, req)

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected status 200, got %d", resp.StatusCode)
	}

	var response Response
	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		t.Fatalf("Failed to decode response: %v", err)
	}

	if response.Status != "ok" {
		t.Errorf("Expected status ok, got %s", response.Status)
	}

	// Check token
	data, ok := response.Data.(map[string]interface{})
	if !ok {
		// It might be parsed differently if 'any' is used.
		// JSON unmarshal into interface{} map[string]interface{}
		t.Errorf("Expected data map, got %T", response.Data)
	} else if data["token"] == "" {
		t.Error("Token should be present")
	}

	// Case 2: Wrong password
	bodyWrong := []byte(`{"email": "test@example.com", "password": "wrongpassword"}`)
	req = httptest.NewRequest("POST", "/api/auth/login", bytes.NewBuffer(bodyWrong))
	w = httptest.NewRecorder()

	h.Login(w, req)

	if w.Result().StatusCode != http.StatusNotFound {
		t.Errorf("Expected 404 for wrong password, got %d", w.Result().StatusCode)
	}
}

func (m *MockDB) DeleteUser(_ context.Context, _ string) error {
	return nil
}

func (m *MockInMemoryDB) DeleteJwt(_ context.Context, _, _ string) error {
	return nil
}

type MockDB struct {
	interfaces.DB
	ExistUserResult bool
	ExistUserError  error
	LoginPassword   string
	LoginError      error
}

func (m *MockDB) IsExistUser(_ context.Context, _ string) (bool, error) {
	return m.ExistUserResult, m.ExistUserError
}

func (m *MockDB) Login(_ context.Context, _ httpType.Login) (string, error) {
	return m.LoginPassword, m.LoginError
}

func (m *MockDB) ChangePassword(_ context.Context, _ httpType.ChangePassword) error {
	return nil
}

func (m *MockDB) Registration(_ context.Context, _ httpType.Registration) error {
	return nil
}

type MockConfirmSender struct {
	LastEmail string
	LastCode  string
}

func (m *MockConfirmSender) SendConfirmationCode(toEmail, code string) error {
	m.LastEmail = toEmail
	m.LastCode = code
	return nil
}

type MockInMemoryDB struct {
	interfaces.InMemoryDB
}

func (m *MockInMemoryDB) SetNotification(_ context.Context, _ string, _ httpType.NotificationData) error {
	return nil
}

func (m *MockInMemoryDB) SetJwt(_ context.Context, _, _ string) error {
	return nil
}

func (m *MockInMemoryDB) DeleteAllJwts(_ context.Context, _ string) error {
	return nil
}

type MockPasswordKeeper struct {
	codes map[string]struct {
		code     string
		password string
	}
}

func (m *MockPasswordKeeper) AddCode(_ context.Context, email, code, password string, _ time.Duration) error {
	if m.codes == nil {
		m.codes = make(map[string]struct {
			code     string
			password string
		})
	}
	m.codes[email] = struct {
		code     string
		password string
	}{code: code, password: password}
	return nil
}

func (m *MockPasswordKeeper) GetPassword(_ context.Context, email string) (string, string, error) {
	if m.codes == nil {
		return "", "", nil
	}
	data, ok := m.codes[email]
	if !ok {
		return "", "", nil
	}
	return data.code, data.password, nil
}

func TestLogout(t *testing.T) {
	h := &Handler{logger: zerolog.Nop(), inMemDb: &MockInMemoryDB{}}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	req := httptest.NewRequest("DELETE", "/api/auth/logout", nil)
	req = req.WithContext(ctx)
	req.Header.Set("Authorization", "Bearer token123")

	w := httptest.NewRecorder()

	h.Logout(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestDeleteUser(t *testing.T) {
	mockDB := &MockDB{}
	mockInMem := &MockInMemoryDB{}
	h := &Handler{logger: zerolog.Nop(), db: mockDB, inMemDb: mockInMem}

	ctx := context.WithValue(context.Background(), "email", "delete@test.com")
	req := httptest.NewRequest("DELETE", "/api/auth/delete/user", nil)
	req = req.WithContext(ctx)

	w := httptest.NewRecorder()

	h.DeleteUser(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestRefreshToken(t *testing.T) {
	// Setup env for JWT
	t.Setenv("JWT_SECRET", "testsecret")

	logger := zerolog.Nop()
	mockConfirm := &MockConfirmSender{}
	mockDB := &MockDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, _ := NewHandler(mockDB, mockConfirm, nil, nil, mockPasswordKeeper, logger)

	body := []byte(`{"email": "refresh@test.com", "password": "pass"}`)
	req := httptest.NewRequest("POST", "/api/auth/refresh/token", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	h.RefreshToken(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	if mockConfirm.LastEmail != "refresh@test.com" {
		t.Errorf("Confirmation code mismatch")
	}
}
