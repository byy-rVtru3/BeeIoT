package interfaces

import (
	"BeeIOT/internal/domain/types/http"
	"context"
)

type DB interface {
	Registration(ctx context.Context, registration http.Registration) error
	Login(ctx context.Context, login http.Login) (string, bool, error)
	ChangePassword(ctx context.Context, user http.ChangePassword) error
	DeleteUser(ctx context.Context, email string) error
}
