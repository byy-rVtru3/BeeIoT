package handlers

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/models/dbTypes"
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

func (m *MockDB) NewHive(_ context.Context, _, _, _ string) error {
	return nil
}

func (m *MockDB) GetHives(_ context.Context, _ string, _ *bool) ([]dbTypes.Hive, error) {
	return []dbTypes.Hive{{Id: 1, NameHive: "Test Hive"}}, nil
}

func (m *MockDB) GetHiveByName(_ context.Context, _ string, _ string, _ *bool) (dbTypes.Hive, error) {
	return dbTypes.Hive{Id: 1, NameHive: "Test Hive"}, nil
}

func (m *MockDB) UpdateHive(_ context.Context, _ string, _ httpType.UpdateHive) error {
	return nil
}

func (m *MockDB) UpdateHiveStatus(_ context.Context, _, _ string, _ bool) error {
	return nil
}

func (m *MockDB) DeleteHive(_ context.Context, _ string, _ string) error {
	return nil
}

func (m *MockDB) GetEmailHiveBySensorID(_ context.Context, _ string) (string, string, error) {
	return "test@example.com", "Test Hive", nil
}

func (m *MockDB) GetNoiseSinceDay(_ context.Context, _ int, _ time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error) {
	return map[time.Time][]dbTypes.HivesNoiseData{
		time.Now(): {{Level: 50.0}},
	}, nil
}

func (m *MockDB) GetTemperaturesSinceTimeById(_ context.Context, _ int, _ time.Time) ([]dbTypes.HivesTemperatureData, error) {
	return []dbTypes.HivesTemperatureData{{Temperature: 25.0}}, nil
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

func (m *MockInMemoryDB) SetLastDeviceStatus(_ context.Context, _, _ string) error {
	return nil
}

func (m *MockInMemoryDB) GetLastDeviceStatus(_ context.Context, _ string) (string, error) {
	return "", nil
}

type MockPasswordKeeper struct {
	codes map[string]struct {
		code     string
		password string
		name     string
	}
}

func (m *MockPasswordKeeper) AddCode(_ context.Context, email, code, password, name string, _ time.Duration) error {
	if m.codes == nil {
		m.codes = make(map[string]struct {
			code     string
			password string
			name     string
		})
	}
	m.codes[email] = struct {
		code     string
		password string
		name     string
	}{code: code, password: password, name: name}
	return nil
}

func (m *MockPasswordKeeper) GetPassword(_ context.Context, email string) (string, string, string, error) {
	if m.codes == nil {
		return "", "", "", nil
	}
	data, ok := m.codes[email]
	if !ok {
		return "", "", "", nil
	}
	return data.code, data.password, data.name, nil
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

func TestCreateHive(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	body := []byte(`{"name": "New Hive"}`)
	req := httptest.NewRequest("POST", "/api/hive/create", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.CreateHive(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestGetHives(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	req := httptest.NewRequest("GET", "/api/hives", nil)
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.GetHives(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestGetHive(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	req := httptest.NewRequest("GET", "/api/hive?name=Test%20Hive", nil)
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.GetHive(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestUpdateHive(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	body := []byte(`{"old_name": "Old Hive", "new_name": "New Hive"}`)
	req := httptest.NewRequest("POST", "/api/hive/update", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.UpdateHive(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestDeleteHive(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	body := []byte(`{"name": "Test Hive"}`)
	req := httptest.NewRequest("DELETE", "/api/hive/delete", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.DeleteHive(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestConfirmRegistration(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	mockSender := &MockConfirmSender{}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, mockSender, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	// Step 1: Generate code
	email := "test@example.com"
	password := "password123"
	code, _ := h.conf.NewCode(email, password, "Test User")

	// Step 2: Confirm registration
	body, _ := json.Marshal(httpType.Confirm{
		Email: email,
		Code:  code,
	})
	req := httptest.NewRequest("POST", "/api/auth/confirm", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	h.ConfirmRegistration(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestConfirmChangePassword(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{ExistUserResult: true} // User must exist
	mockSender := &MockConfirmSender{}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, mockSender, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	// Step 1: Generate code
	email := "test@example.com"
	password := "newpassword123"
	code, _ := h.conf.NewCode(email, password, "")

	// Step 2: Confirm change password
	body, _ := json.Marshal(httpType.Confirm{
		Email: email,
		Code:  code,
	})
	req := httptest.NewRequest("POST", "/api/auth/confirm/password", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	h.ConfirmChangePassword(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestChangePassword(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{ExistUserResult: true}
	mockSender := &MockConfirmSender{}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, mockSender, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	body := []byte(`{"email": "test@example.com", "password": "newpassword"}`)
	req := httptest.NewRequest("POST", "/api/auth/change/password", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	h.ChangePassword(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	if mockSender.LastEmail != "test@example.com" {
		t.Errorf("Confirmation code email mismatch")
	}
}

func TestQueenCalculator(t *testing.T) {
	logger := zerolog.Nop()
	h := &Handler{logger: logger}

	body := []byte(`{"start_date": "2023-05-01"}`)
	req := httptest.NewRequest("POST", "/api/queen/calc", bytes.NewBuffer(body))
	w := httptest.NewRecorder()

	h.QueenCalculator(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	var resp Response
	if err := json.NewDecoder(w.Body).Decode(&resp); err != nil {
		t.Fatalf("Failed to decode response: %v", err)
	}

	// Basic check if calendar data is present
	dataMap, ok := resp.Data.(map[string]interface{})
	if !ok {
		t.Errorf("Expected data map, got %T", resp.Data)
	} else if dataMap["start_date"] != "2023-05-01" {
		t.Errorf("Expected start_date 2023-05-01, got %v", dataMap["start_date"])
	}
}

func TestGetNoiseAndTemp(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h, err := NewHandler(mockDB, nil, nil, nil, nil, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	req := httptest.NewRequest("GET", "/api/mqtt/data?sensor=sensor1", nil)
	w := httptest.NewRecorder()

	h.GetNoiseAndTemp(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

// TestMQTTSendConfig is disabled because of difficulty mocking concrete mqtt.Client struct
// func TestMQTTSendConfig(t *testing.T) {
// 	...
// }
