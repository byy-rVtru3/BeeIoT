package interfaces

import (
	"BeeIOT/internal/domain/types/httpType"
	"context"
)

type ConfirmSender interface {
	SendConfirmationCode(toEmail, code string) error
	New() error
}

type DB interface {
	Registration(ctx context.Context, registration httpType.Registration) error
	IsExistUser(ctx context.Context, email string) (bool, error)
	Login(ctx context.Context, login httpType.Login) (string, bool, error)
	ChangePassword(ctx context.Context, user httpType.ChangePassword) error
	DeleteUser(ctx context.Context, email string) error
}
