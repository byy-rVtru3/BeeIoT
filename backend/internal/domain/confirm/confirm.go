package confirm

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/passwords"
	"context"
	"math/rand"
	"time"
)

const timeLiveCode = 5 * time.Minute

type userEmail = string
type UserCode = string
type UserPassword = string

type confirmUser struct {
	code     UserCode
	password string
}

type Confirm struct {
	rand       *rand.Rand
	confirmMap interfaces.PasswordKeeper
	Sender     interfaces.ConfirmSender
}

func NewConfirm(sender interfaces.ConfirmSender, confirmMap interfaces.PasswordKeeper) (*Confirm, error) {
	return &Confirm{rand: rand.New(rand.NewSource(time.Now().UnixNano())),
		confirmMap: confirmMap,
		Sender:     sender}, nil
}

func (conf *Confirm) NewCode(email, password string) (UserCode, error) {
	code := conf.generateConfirmationCode()
	pswd, err := passwords.HashPassword(password)
	if err != nil {
		return "", err
	}
	ctx, cancel := context.WithTimeout(context.Background(), timeLiveCode)
	defer cancel()
	err = conf.confirmMap.AddCode(ctx, email, code, pswd, timeLiveCode)
	return code, err
}

func (conf *Confirm) Verify(email, code string) (UserPassword, bool) {
	ctx, cancel := context.WithTimeout(context.Background(), timeLiveCode)
	defer cancel()
	confirmCode, password, err := conf.confirmMap.GetPassword(ctx, email)
	if err != nil || confirmCode != code {
		return "", false
	}
	return password, true
}

func (conf *Confirm) generateConfirmationCode() string {
	data := []rune("01234567890123456789")
	conf.rand.Shuffle(len(data), func(i, j int) {
		data[i], data[j] = data[j], data[i]
	})
	return string(data[:6])
}
