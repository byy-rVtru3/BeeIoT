package notification

import (
	"context"
	"encoding/base64"
	"errors"
	"os"

	firebase "firebase.google.com/go/v4"
	"firebase.google.com/go/v4/messaging"
	"github.com/rs/zerolog"
	"google.golang.org/api/option"
)

type Notification struct {
	fcmClient *messaging.Client
	logger    zerolog.Logger
}

type Data struct {
	Title     string
	Body      string
	Data      map[string]string
	Tokens    []string
	Important bool
}

func (n *Notification) feelImportant(isImportant bool) string {
	if isImportant {
		return "CRITICAL"
	}
	return "REGULAR"
}

var ErrInvalidTokens = errors.New("invalid tokens")

func NewNotification(ctx context.Context, logger zerolog.Logger) (*Notification, error) {
	data := os.Getenv("FIREBASE_DATA")
	if data == "" {
		return nil, errors.New("FIREBASE_DATA environment variable is not set")
	}
	decoded, err := base64.StdEncoding.DecodeString(data)
	if err != nil {
		return nil, err
	}
	opt := option.WithCredentialsJSON(decoded)
	app, err := firebase.NewApp(ctx, nil, opt)
	if err != nil {
		return nil, err
	}
	fcmClient, err := app.Messaging(ctx)
	if err != nil {
		return nil, err
	}
	return &Notification{
		fcmClient: fcmClient,
		logger:    logger,
	}, nil
}

func (n *Notification) SendNotification(ctx context.Context, data Data) ([]string, error) {
	data.Data["mobile_notification_type"] = n.feelImportant(data.Important)
	message := &messaging.MulticastMessage{
		Data:   data.Data,
		Tokens: data.Tokens,
		Notification: &messaging.Notification{
			Title: data.Title,
			Body:  data.Body,
		},
	}
	response, err := n.fcmClient.SendEachForMulticast(ctx, message)
	if err != nil {
		return nil, err
	}
	if response.FailureCount > 0 {
		return n.deleteInvalidTokens(response.Responses, data.Tokens)
	}
	return nil, nil
}

func (n *Notification) deleteInvalidTokens(response []*messaging.SendResponse, tokens []string) ([]string, error) {

	var badTokens []string
	for i, resp := range response {
		if !resp.Success {
			if messaging.IsUnregistered(resp.Error) || messaging.IsInvalidArgument(resp.Error) {
				badTokens = append(badTokens, tokens[i])
			} else {
				n.logger.Warn().Err(resp.Error).Msg("Failed to send notification to token")
			}
		}
	}

	if len(badTokens) > 0 {
		return badTokens, ErrInvalidTokens
	}
	return nil, nil
}
