package confirm

import (
	"BeeIOT/internal/domain/interfaces"
	"BeeIOT/internal/domain/passwords"
	"math/rand"
	"sync"
	"time"
)

type userEmail = string
type userCode = string

type confirmUser struct {
	code     userCode
	password string
}

type Confirm struct {
	rand           *rand.Rand
	confirmMutex   sync.Mutex
	confirmCodeMap map[userEmail]confirmUser
	Sender         interfaces.ConfirmSender
}

func NewConfirm(sender interfaces.ConfirmSender) (*Confirm, error) {
	return &Confirm{rand: rand.New(rand.NewSource(time.Now().UnixNano())),
		confirmCodeMap: make(map[userEmail]confirmUser),
		Sender:         sender}, nil
}

func (conf *Confirm) NewCode(email, password string) (string, error) {
	code := conf.generateConfirmationCode()
	pswd, err := passwords.HashPassword(password)
	if err != nil {
		return "", err
	}
	conf.confirmMutex.Lock()
	conf.confirmCodeMap[email] = confirmUser{code: code, password: pswd}
	conf.confirmMutex.Unlock()
	go conf.endTimerOfCode(email, code)
	return code, nil
}

func (conf *Confirm) Verify(email, code string) (string, bool) {
	conf.confirmMutex.Lock()
	val, ok := conf.confirmCodeMap[email]
	if !ok || val.code != code {
		conf.confirmMutex.Unlock()
		return "", false
	}
	pswd := val.password
	delete(conf.confirmCodeMap, email)
	conf.confirmMutex.Unlock()
	return pswd, true
}

func (conf *Confirm) generateConfirmationCode() string {
	data := []rune("01234567890123456789")
	conf.rand.Shuffle(len(data), func(i, j int) {
		data[i], data[j] = data[j], data[i]
	})
	return string(data[:6])
}

func (conf *Confirm) endTimerOfCode(email, code string) {
	time.Sleep(5 * time.Minute)
	conf.confirmMutex.Lock()
	val, ok := conf.confirmCodeMap[email]
	if !ok || val.code != code {
		conf.confirmMutex.Unlock()
		return
	}
	delete(conf.confirmCodeMap, email)
	conf.confirmMutex.Unlock()
}
