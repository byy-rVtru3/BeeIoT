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
	UserEmail       string
	UserName        string
	CreatedTaskID   string
	TaskData        dbTypes.Task
	TasksList       []dbTypes.Task
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

func (m *MockDB) DeleteHive(_ context.Context, _ string, _ string) error {
	return nil
}

func (m *MockDB) GetEmailHiveBySensorID(_ context.Context, _ string) (string, string, error) {
	return "test@example.com", "Test Hive", nil
}

func (m *MockDB) NewHub(_ context.Context, _, _, _ string) error {
	return nil
}

func (m *MockDB) GetHubs(_ context.Context, _ string) ([]dbTypes.Hub, error) {
	return []dbTypes.Hub{{Id: 1, NameHub: "Test Hub", Sensor: "hub-001"}}, nil
}

func (m *MockDB) GetHubBySensor(_ context.Context, _, _ string) (dbTypes.Hub, error) {
	return dbTypes.Hub{Id: 1, NameHub: "Test Hub", Sensor: "hub-001"}, nil
}

func (m *MockDB) DeleteHub(_ context.Context, _, _ string) error {
	return nil
}

func (m *MockDB) UpdateHub(_ context.Context, _ string, _ httpType.UpdateHub) error {
	return nil
}

func (m *MockDB) NewQueen(_ context.Context, _, _, _ string) error {
	return nil
}

func (m *MockDB) GetQueens(_ context.Context, _ string) ([]dbTypes.Queen, error) {
	return []dbTypes.Queen{{Id: 1, Name: "Матка-1", StartDate: time.Date(2024, 6, 1, 0, 0, 0, 0, time.UTC)}}, nil
}

func (m *MockDB) GetQueenByName(_ context.Context, _, _ string) (dbTypes.Queen, error) {
	return dbTypes.Queen{Id: 1, Name: "Матка-1", StartDate: time.Date(2024, 6, 1, 0, 0, 0, 0, time.UTC)}, nil
}

func (m *MockDB) DeleteQueen(_ context.Context, _, _ string) error {
	return nil
}

func (m *MockDB) UpdateQueen(_ context.Context, _ string, _ httpType.UpdateQueen) error {
	return nil
}

func (m *MockDB) GetNoiseSinceDay(_ context.Context, _ int, _ time.Time) (map[time.Time][]dbTypes.HivesNoiseData, error) {
	return map[time.Time][]dbTypes.HivesNoiseData{
		time.Now(): {{Level: 50.0}},
	}, nil
}

func (m *MockDB) GetTemperaturesSinceTimeById(_ context.Context, _ int, _ time.Time) ([]dbTypes.HivesTemperatureData, error) {
	return []dbTypes.HivesTemperatureData{{Temperature: 25.0}}, nil
}

func (m *MockDB) GetUserByEmail(_ context.Context, _ string) (string, string, error) {
	return m.UserEmail, m.UserName, nil
}

func (m *MockDB) CreateTask(_ context.Context, _ string, _ httpType.CreateTaskRequest) (string, error) {
	return m.CreatedTaskID, nil
}

func (m *MockDB) GetTasks(_ context.Context, _ string, _ string) ([]dbTypes.Task, error) {
	return m.TasksList, nil
}

func (m *MockDB) UpdateTask(_ context.Context, _ string, _ httpType.UpdateTaskRequest) error {
	return nil
}

func (m *MockDB) DeleteTask(_ context.Context, _ string, _ string) error {
	return nil
}

func (m *MockDB) GetTaskByID(_ context.Context, _ string) (dbTypes.Task, error) {
	return m.TaskData, nil
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

func TestGetMe(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{
		UserEmail: "test@example.com",
		UserName:  "Test User",
	}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, nil, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	req := httptest.NewRequest("GET", "/api/auth/me", nil)
	req = req.WithContext(context.WithValue(req.Context(), "email", "test@example.com"))
	w := httptest.NewRecorder()

	h.GetMe(w, req)

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", resp.StatusCode)
	}
}

func TestCreateTask(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{
		CreatedTaskID: "task-123",
		TaskData: dbTypes.Task{
			ID:          "task-123",
			HiveName:    "Улей-1",
			Title:       "Осенняя ревизия",
			Description: "Проверка кормов",
			CreatedAt:   time.Now(),
			Email:       "test@example.com",
		},
	}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, nil, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	body, _ := json.Marshal(httpType.CreateTaskRequest{
		HiveName:    "Улей-1",
		Title:       "Осенняя ревизия",
		Description: "Проверка кормов",
	})
	req := httptest.NewRequest("POST", "/api/task/create", bytes.NewBuffer(body))
	req = req.WithContext(context.WithValue(req.Context(), "email", "test@example.com"))
	w := httptest.NewRecorder()

	h.CreateTask(w, req)

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", resp.StatusCode)
	}
}

func TestGetTasks(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	tasks := []dbTypes.Task{
		{
			ID:          "task-1",
			HiveName:    "Улей-1",
			Title:       "Ревизия",
			Description: "Проверка",
			CreatedAt:   time.Now(),
			Email:       "test@example.com",
		},
	}
	mockDB := &MockDB{TasksList: tasks}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, nil, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	req := httptest.NewRequest("GET", "/api/task/list", nil)
	req = req.WithContext(context.WithValue(req.Context(), "email", "test@example.com"))
	w := httptest.NewRecorder()

	h.GetTasks(w, req)

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", resp.StatusCode)
	}
}

func TestUpdateTask(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{
		TaskData: dbTypes.Task{
			ID:          "task-1",
			HiveName:    "Улей-1",
			Title:       "Ревизия",
			Description: "Проверка",
			CreatedAt:   time.Now(),
			Email:       "test@example.com",
		},
	}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, nil, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	newTitle := "Обновленная ревизия"
	body, _ := json.Marshal(httpType.UpdateTaskRequest{
		ID:    "task-1",
		Title: &newTitle,
	})
	req := httptest.NewRequest("PUT", "/api/task/update", bytes.NewBuffer(body))
	req = req.WithContext(context.WithValue(req.Context(), "email", "test@example.com"))
	w := httptest.NewRecorder()

	h.UpdateTask(w, req)

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", resp.StatusCode)
	}
}

func TestDeleteTask(t *testing.T) {
	t.Setenv("JWT_SECRET", "testsecret")
	logger := zerolog.Nop()
	mockDB := &MockDB{
		TaskData: dbTypes.Task{
			ID:       "task-1",
			HiveName: "Улей-1",
			Title:    "Ревизия",
			Email:    "test@example.com",
		},
	}
	mockInMem := &MockInMemoryDB{}
	mockPasswordKeeper := &MockPasswordKeeper{}

	h, err := NewHandler(mockDB, nil, mockInMem, nil, mockPasswordKeeper, logger)
	if err != nil {
		t.Fatalf("NewHandler failed: %v", err)
	}

	body, _ := json.Marshal(httpType.DeleteTaskRequest{
		ID: "task-1",
	})
	req := httptest.NewRequest("DELETE", "/api/task/delete", bytes.NewBuffer(body))
	req = req.WithContext(context.WithValue(req.Context(), "email", "test@example.com"))
	w := httptest.NewRecorder()

	h.DeleteTask(w, req)

	resp := w.Result()
	if resp.StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", resp.StatusCode)
	}
}

// ==================== Hub handler tests ====================

func TestCreateHub(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")

	// Успешное создание
	body := []byte(`{"id": "hub-001", "name": "Мой хаб"}`)
	req := httptest.NewRequest("POST", "/api/hub/create", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.CreateHub(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	// Пустой ID — должен вернуть 400
	body = []byte(`{"id": "", "name": "Мой хаб"}`)
	req = httptest.NewRequest("POST", "/api/hub/create", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.CreateHub(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for empty hub id, got %d", w.Result().StatusCode)
	}

	// Пустое имя — должен вернуть 400
	body = []byte(`{"id": "hub-001", "name": ""}`)
	req = httptest.NewRequest("POST", "/api/hub/create", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.CreateHub(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for empty hub name, got %d", w.Result().StatusCode)
	}
}

func TestGetHubs(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	req := httptest.NewRequest("GET", "/api/hubs", nil)
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.GetHubs(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	var response Response
	if err := json.NewDecoder(w.Result().Body).Decode(&response); err != nil {
		t.Fatalf("Failed to decode response: %v", err)
	}
	if response.Status != "ok" {
		t.Errorf("Expected status ok, got %s", response.Status)
	}
}

func TestGetHub(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")

	// Успешный запрос
	req := httptest.NewRequest("GET", "/api/hub?id=hub-001", nil)
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.GetHub(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	// Без параметра id — 400
	req = httptest.NewRequest("GET", "/api/hub", nil)
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.GetHub(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for missing id param, got %d", w.Result().StatusCode)
	}
}

func TestDeleteHubHandler(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	body := []byte(`{"id": "hub-001"}`)
	req := httptest.NewRequest("DELETE", "/api/hub/delete", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.DeleteHub(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestUpdateHubHandler(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	body := []byte(`{"id": "hub-001", "name": "Новое имя"}`)
	req := httptest.NewRequest("PUT", "/api/hub/update", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.UpdateHub(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	// Пустой ID — 400
	body = []byte(`{"id": ""}`)
	req = httptest.NewRequest("PUT", "/api/hub/update", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.UpdateHub(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for empty hub id, got %d", w.Result().StatusCode)
	}
}

// ==================== Queen handler tests ====================

func TestCreateQueen(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")

	// Успешное создание
	body := []byte(`{"name": "Матка-1", "start_date": "2024-06-01"}`)
	req := httptest.NewRequest("POST", "/api/queen/create", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.CreateQueen(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	var response Response
	if err := json.NewDecoder(w.Result().Body).Decode(&response); err != nil {
		t.Fatalf("Failed to decode response: %v", err)
	}
	if response.Status != "ok" {
		t.Errorf("Expected status ok, got %s", response.Status)
	}

	// Пустое имя — 400
	body = []byte(`{"name": "", "start_date": "2024-06-01"}`)
	req = httptest.NewRequest("POST", "/api/queen/create", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.CreateQueen(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for empty queen name, got %d", w.Result().StatusCode)
	}

	// Неверный формат даты — 400
	body = []byte(`{"name": "Матка-2", "start_date": "01-06-2024"}`)
	req = httptest.NewRequest("POST", "/api/queen/create", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.CreateQueen(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for invalid date format, got %d", w.Result().StatusCode)
	}
}

func TestGetQueens(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	req := httptest.NewRequest("GET", "/api/queens", nil)
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.GetQueens(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}
}

func TestGetQueen(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")

	// Успешный запрос
	req := httptest.NewRequest("GET", "/api/queen?name=%D0%9C%D0%B0%D1%82%D0%BA%D0%B0-1", nil)
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.GetQueen(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	// Без параметра name — 400
	req = httptest.NewRequest("GET", "/api/queen", nil)
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.GetQueen(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for missing name param, got %d", w.Result().StatusCode)
	}
}

func TestUpdateQueen(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	newName := "Матка-обновленная"
	body, _ := json.Marshal(httpType.UpdateQueen{
		OldName: "Матка-1",
		NewName: &newName,
	})
	req := httptest.NewRequest("PUT", "/api/queen/update", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.UpdateQueen(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	// Пустое old_name — 400
	body, _ = json.Marshal(httpType.UpdateQueen{OldName: ""})
	req = httptest.NewRequest("PUT", "/api/queen/update", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.UpdateQueen(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for empty old_name, got %d", w.Result().StatusCode)
	}
}

func TestDeleteQueen(t *testing.T) {
	logger := zerolog.Nop()
	mockDB := &MockDB{}
	h := &Handler{logger: logger, db: mockDB}

	ctx := context.WithValue(context.Background(), "email", "test@example.com")
	body := []byte(`{"name": "Матка-1"}`)
	req := httptest.NewRequest("DELETE", "/api/queen/delete", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w := httptest.NewRecorder()

	h.DeleteQueen(w, req)

	if w.Result().StatusCode != http.StatusOK {
		t.Errorf("Expected 200, got %d", w.Result().StatusCode)
	}

	// Пустое имя — 400
	body = []byte(`{"name": ""}`)
	req = httptest.NewRequest("DELETE", "/api/queen/delete", bytes.NewBuffer(body))
	req = req.WithContext(ctx)
	w = httptest.NewRecorder()

	h.DeleteQueen(w, req)

	if w.Result().StatusCode != http.StatusBadRequest {
		t.Errorf("Expected 400 for empty queen name, got %d", w.Result().StatusCode)
	}
}
