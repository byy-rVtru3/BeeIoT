package passwords

import (
	"testing"
)

func TestHashPassword(t *testing.T) {
	password := "secret123"
	hash, err := HashPassword(password)
	if err != nil {
		t.Fatalf("HashPassword failed: %v", err)
	}

	if hash == password {
		t.Error("Hash should not be equal to the password")
	}

	if len(hash) == 0 {
		t.Error("Hash should not be empty")
	}
}

func TestCheckPasswordHash(t *testing.T) {
	password := "secret123"
	hash, _ := HashPassword(password)

	match := CheckPasswordHash(password, hash)
	if !match {
		t.Error("CheckPasswordHash failed for correct password")
	}

	match = CheckPasswordHash("wrongpassword", hash)
	if match {
		t.Error("CheckPasswordHash succeeded for wrong password")
	}
}
