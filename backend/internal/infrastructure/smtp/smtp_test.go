package smtp

import (
	"os"
	"testing"
)

func TestNewSMTP_MissingEnv(t *testing.T) {
	old := map[string]string{
		"SMTP_USER": os.Getenv("SMTP_USER"),
		"SMTP_PASS": os.Getenv("SMTP_PASS"),
		"SMTP_HOST": os.Getenv("SMTP_HOST"),
		"SMTP_PORT": os.Getenv("SMTP_PORT"),
	}
	defer func() {
		_ = os.Setenv("SMTP_USER", old["SMTP_USER"])
		_ = os.Setenv("SMTP_PASS", old["SMTP_PASS"])
		_ = os.Setenv("SMTP_HOST", old["SMTP_HOST"])
		_ = os.Setenv("SMTP_PORT", old["SMTP_PORT"])
	}()
	_ = os.Unsetenv("SMTP_USER")
	_ = os.Unsetenv("SMTP_PASS")
	_ = os.Unsetenv("SMTP_HOST")
	_ = os.Unsetenv("SMTP_PORT")

	_, err := NewSMTP()
	if err == nil {
		t.Fatalf("expected error when smtp env vars missing")
	}
}

func TestSendConfirmationCode_ConnectFail(t *testing.T) {
	old := map[string]string{
		"SMTP_USER": os.Getenv("SMTP_USER"),
		"SMTP_PASS": os.Getenv("SMTP_PASS"),
		"SMTP_HOST": os.Getenv("SMTP_HOST"),
		"SMTP_PORT": os.Getenv("SMTP_PORT"),
	}
	defer func() {
		_ = os.Setenv("SMTP_USER", old["SMTP_USER"])
		_ = os.Setenv("SMTP_PASS", old["SMTP_PASS"])
		_ = os.Setenv("SMTP_HOST", old["SMTP_HOST"])
		_ = os.Setenv("SMTP_PORT", old["SMTP_PORT"])
	}()

	_ = os.Setenv("SMTP_USER", "noreply@example.com")
	_ = os.Setenv("SMTP_PASS", "pass")
	_ = os.Setenv("SMTP_HOST", "127.0.0.1")
	_ = os.Setenv("SMTP_PORT", "2525")

	s, err := NewSMTP()
	if err != nil {
		t.Fatalf("NewSMTP failed: %v", err)
	}

	err = s.SendConfirmationCode("to@example.com", "1234")
	if err == nil {
		t.Fatalf("expected error when sending to unavailable SMTP server")
	}
}
