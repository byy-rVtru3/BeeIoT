package interfaces

import (
	"BeeIOT/internal/domain/types/httpType"
	"context"
)

type ConfirmSender interface {
	SendConfirmationCode(toEmail, code string) error
}

type DB interface {
	Registration(ctx context.Context, registration httpType.Registration) error
	IsExistUser(ctx context.Context, email string) (bool, error)
	Login(ctx context.Context, login httpType.Login) (string, error)
	ChangePassword(ctx context.Context, user httpType.ChangePassword) error
	DeleteUser(ctx context.Context, email string) error
}

type InMemoryDB interface {
	SetNotification(ctx context.Context, email string, note httpType.NotificationData) error
	GetNotifications(ctx context.Context, email string) ([]httpType.NotificationData, error)
	SetJwt(ctx context.Context, email, token string) error
	ExistJwt(ctx context.Context, email, jwtId string) (bool, error)
	DeleteJwt(ctx context.Context, email, jwtId string) error
	DeleteAllJwts(ctx context.Context, email string) error
}
