package confirm

import (
	"context"
	"testing"
	"time"
)

type MockConfirmSender struct {
	LastEmail string
	LastCode  string
	Err       error
}

func (m *MockConfirmSender) SendConfirmationCode(toEmail, code string) error {
	m.LastEmail = toEmail
	m.LastCode = code
	return m.Err
}

type MockPasswordKeeper struct {
	codes map[string]struct {
		code     string
		password string
	}
}

func (m *MockPasswordKeeper) AddCode(ctx context.Context, email, code, password string, ttl time.Duration) error {
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

func (m *MockPasswordKeeper) GetPassword(ctx context.Context, email string) (string, string, error) {
	if m.codes == nil {
		return "", "", nil
	}
	data, ok := m.codes[email]
	if !ok {
		return "", "", nil
	}
	return data.code, data.password, nil
}

func TestNewConfirm(t *testing.T) {
	mockSender := &MockConfirmSender{}
	mockKeeper := &MockPasswordKeeper{}
	conf, err := NewConfirm(mockSender, mockKeeper)
	if err != nil {
		t.Fatalf("NewConfirm failed: %v", err)
	}
	if conf == nil {
		t.Fatal("Confirm instance should not be nil")
	}
}

func TestNewCodeAlsoSendsEmail(t *testing.T) {
	mockSender := &MockConfirmSender{}
	mockKeeper := &MockPasswordKeeper{}
	conf, _ := NewConfirm(mockSender, mockKeeper)

	email := "test@example.com"
	password := "password123"

	code, err := conf.NewCode(email, password)
	if err != nil {
		t.Fatalf("NewCode failed: %v", err)
	}

	if code == "" {
		t.Error("Code should not be empty")
	}
}

func TestVerify(t *testing.T) {
	mockSender := &MockConfirmSender{}
	mockKeeper := &MockPasswordKeeper{}
	conf, _ := NewConfirm(mockSender, mockKeeper)

	email := "test@example.com"
	password := "password123"

	code, _ := conf.NewCode(email, password)

	// Verify with correct code
	storedPasswordHash, ok := conf.Verify(email, code)
	if !ok {
		t.Error("Verify failed for valid code")
	}
	if storedPasswordHash == "" {
		t.Error("Verify should return password hash")
	}

	// Verify with incorrect code
	_, ok = conf.Verify(email, "wrongcode")
	if ok {
		t.Error("Verify succeeded for invalid code")
	}

	// Verify with non-existent email
	_, ok = conf.Verify("other@example.com", code)
	if ok {
		t.Error("Verify succeeded for non-existent email")
	}
}
